package ozone.owf.grails.controllers

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.model.WidgetDefinitionServiceModel

class WidgetDefinitionController extends BaseOwfRestController {

	def accountService
	def groupService
	def imageCacheService
	def widgetDefinitionService
	def modelName = 'widgetDefinition'

	def cachedImage = { WidgetCachedImageCommand cmd ->
		if (!cmd.hasErrors()) {
			def widgetDefinition = WidgetDefinition.findByWidgetGuid(cmd.widgetGuid)
			def image
			switch (cmd.name) {
				case "imageUrlSmall":
					image = imageCacheService.getImage(widgetDefinition.imageUrlSmall)
					break

				case "imageUrlLarge":
					image = imageCacheService.getImage(widgetDefinition.imageUrlLarge)
					break
			}
			if (!image) {
				def target = (cmd.name == 'imageUrlSmall') ? widgetDefinition.imageUrlSmall : widgetDefinition.imageUrlLarge
				redirect(url: target)
			}
			else {
				response.contentType = image.contentType
				response.outputStream << image.content
				response.outputStream.flush()
			}
		}
		else {
			return [status: 500]
		}
	}

	def createOrUpdate = { WidgetCreateOrUpdateCommand cmd ->
		def jsonResult
		try {
			// TODO:  WDC3 -- This first path should be factored out into its own method and the UrlMappings class adjusted.  The only path
			// through this code should be the "else" clause.
			if (params?.addExternalWidgetsToUser) {
				def result = widgetDefinitionService.addExternalWidgetsToUser(params)
				jsonResult = [msg: result as JSON, status: 200]
			}
			else {
				def savedWidgets = []
				if (!accountService.getLoggedInUserIsAdmin()) {
					throw new OwfException(message: 'You are not authorized to manage widgets.', exceptionType: OwfExceptionTypes.Authorization)
				}
				if (cmd.hasErrors()) {
					throw new OwfException(message: 'Create or update command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
				}

				if (cmd.tab == null) {
					cmd.parsedWidgetJson.each {
						WidgetDefinition wd = WidgetDefinition.findByWidgetGuid(it.widgetGuid)
						if (wd == null && !it.isNewWidget) {
							throw new OwfException(message: "The requested widget, guid ${it.widgetGuid}, was not found.", exceptionType: OwfExceptionTypes.NotFound)
						}
						if (wd == null) {
							wd = new WidgetDefinition()
						}

						// The widget type name is filled in by the parser.  Could technically grab the ID and jam it a different way, possibly.
						WidgetType wt = WidgetType.findByName(it.widgetTypeName)
						widgetDefinitionService.updateWidget(it, wd, wt)
						savedWidgets << wd
					}
				}
				else {
					WidgetDefinition wd = WidgetDefinition.findByWidgetGuid(cmd.widget_id)
					if (wd == null) {
						throw new OwfException(message: "The requested widget, guid ${cmd.widget_id}, was not found.", exceptionType: OwfExceptionTypes.NotFound)
					}

					JSONArray arrWidgets = new JSONArray()
					JSONObject objWidget = new JSONObject(widgetGuid: cmd.widget_id)
					arrWidgets.add(objWidget)

					if (cmd.tab == 'users') {
						// TODO: Work this to reuse code that's already present in the service layer.
						cmd.parsedPrincipalLikeJson.each {
							if (cmd.update_action == 'add') {
								//								widgetDefinitionService.addUsersToWidget(wd, cmd.parsedPrincipalLikeJson*.id)
							}
							else {
								//								widgetDefinitionService.removeUsersFromWidget(wd, cmd.parsedPrincipalLikeJson*.id)
							}
						}
					}
					else {
						cmd.parsedPrincipalLikeJson.each {
							// This is the right call, but waiting until we've got good test coverage before turning this loose.
							//							groupService.createOrUpdateWidgetsForGroup(arrWidgets, it.id, cmd.update_action == 'add')
						}
					}
				}

				def result = [:]
				if (savedWidgets.size() == 0) {
					// FIXME: Legacy code that can be removed as the above-mapped paths become reality.
					def widgets = []
					//json encoded params inside data
					if (params.data && !params.tab) {
						def json = JSON.parse(params.data)

						if (json instanceof List) {
							widgets = json
						}
						else {
							widgets << json
						}
					}
					else {
						// FIXME:  This simply doesn't occur.  Remove!!
						//no embedded json data assume one widget to be updated it's params are directly on the params
						widgets << params
					}

					// FIXME:  What the...???
					//create each group
					def results = widgets.collect {
						widgetDefinitionService.updateWidget(it)
					}
					result = [success: true, data: results.flatten()]
				}
				else {
					result = [success: true, data: savedWidgets.each { serviceModelService.createServiceModel(it) }]
				}

				jsonResult = [msg: result as JSON, status: 200]
			}
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def delete = {
		// TODO: To save some noise in what follows, a command object with parsing would be nice here.  See what was done for the
		// createOrUpdate command object for an example of what the parsing and normalizing of data might look like.
		def jsonResult

		try {
			// TODO: Eventually we'll want to retire this call in favor of simply using the accountService directly.  See how this
			// is done in the createOrUpdate call.
			widgetDefinitionService.ensureAdmin()

			def widgets = []
			if (params.data) {
				def json = JSON.parse(params.data)
				widgets = json*.id
			}
			else {
				widgets = params.list('id').collect { it }
			}
			// TODO: We'll want to guard against an empty widgets list here.  The integration tests will pass just fine
			// but if you try an empty list at runtime w/ MySQL, you'll get a syntax exception.  Likely that the above
			// parsing will become part of the command object and if we don't have widgets, we should fail a validation
			// and never reach this point anyway.
			widgetDefinitionService.delete(widgets)
			def result = [success: true, data: widgets.collect { [id: it] }]
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def dependents = { WidgetDependentsCommand cmd ->
		def jsonResult

		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Dependents command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			List<WidgetDefinition> wdResult = widgetDefinitionService.getDependents(cmd.ids, cmd.recurse)
			List<WidgetDefinitionServiceModel> processedWidgets = []

			// One noteworthy item here:  formerly, this API would also query to find out how many users and groups were mapped onto
			// the widgets we're returning.  This was a modestly expensive call.  It appears, however, that the JavaScript which calls
			// this API doesn't actually do anything with the user and group count information (it's not displayed).  Therefore we've
			// omitted the calls to locate that information in the name of better performance.
			if (!wdResult.isEmpty()) {
				processedWidgets = wdResult.collect {
					serviceModelService.createServiceModel(it)
				}
			}
			def result = [success: true, data: processedWidgets]
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def export = { WidgetExportCommand cmd ->
		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to export widgets.', exceptionType: OwfExceptionTypes.Authorization)
			}

			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Export command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			WidgetDefinition widgetDefinition = WidgetDefinition.findByWidgetGuid(cmd.id)
			if (widgetDefinition == null) {
				throw new OwfException(message: 'Widget ' + cmd.id + ' was not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map mp = widgetDefinition.asExportableMap()
			String widgetData = (mp as JSON).toString(true)

			response.setContentType("application/x-unknown")
			response.setHeader("Content-disposition", "attachment; filename=" + cmd.filename + ".json")
			response.outputStream.write(widgetData.getBytes("UTF-8"))
			response.outputStream.flush()
		}
		catch (Exception e) {
			//Set content-disposition back to text to relay the error
			response.setHeader("Content-disposition", "")

			def result = handleError(e)
			renderResult(result)
		}
	}

	def list = {
		// TODO:  WDC1 -- Single-point story to create a command object that can accommodate the following data scenarios, plus the unit
		// test required to verify the operation of the constraints we'd like to place against the data.  Add the command object to the
		// declaration of the list closure as shown in other controllers (e.g. Dashboard, Group).  Scenarios are as follows:
		//
		// Scenario 1: occurs when opening the 'Widgets' admin widget and (optionally) using the search box.  Min values for max, offset
		// and the order and sort must be present and one of the enumerated values.  If filters are present, they must parse into JSON
		// and the filterOperator must be present.
		//		filterOperator	OR
		//		filters	[{"filterField":"displayName","filterValue":"Document"},{"filterField":"universalName","filterValue":"Document"}]
		//		max	50
		//		offset	0
		//		order	ASC | DESC
		//		sort	name | url | universalName | widgetGuid | version
		//
		// Scenario 2: occurs using the 'Widgets' admin widget and highlighting a widget with dependents.  Id(s) must match the pattern
		// for a GUID, cannot be blank.
		//		id	d42b5084-7524-4cc7-b499-487e9e2b2ea3
		//		id	fc47def0-ab38-43eb-bc88-81fb9960ef6a
		//		id	56ddb9f3-f65e-4d4d-a893-a9932365a8cf
		//
		// Scenario 3: occurs when opening the 'Widget Editor' admin widget.  Similar validations as scenario 2.
		//		id	efdad860-3179-4e30-822f-846ff7485ddb
		//
		// Scenario 4: occurs when selecting the 'Widgets' tab of the 'Group Editor' admin widget and (optionally) using the search box.
		// Similar validations as for scenario 1, but with the added constraint that the 'tab' field must not be blank and must be
		// 'widgets', plus the requirement for a group_id > 0
		//		filterOperator	OR
		//		filters	[{"filterField":"displayName","filterValue":"Document"},{"filterField":"universalName","filterValue":"Document"}]
		//		group_id	1
		//		max	25
		//		offset	0
		//		order	ASC | DESC
		//		sort	name | url | universalName | widgetGuid | version
		//		tab	widgets
		//
		// Scenario 5: occurs when selecting the 'Widgets' tab of the 'User Editor' admin widget and (optionally) using the search box.
		// Same validation requirements as scenario 4, but substitute user_id for group_id.
		//		filterOperator	OR
		//		filters	[{"filterField":"displayName","filterValue":"Document"},{"filterField":"universalName","filterValue":"Document"}]
		//		max	25
		//		offset	0
		//		order	ASC
		//		sort	name | url | universalName | widgetGuid | version
		//		tab	widgets
		//		user_id	20002

		def statusCode
		def jsonResult

		try {
			// TODO:  WDC2 -- Two-point story to rewrite this code path to verify that the command object has no errors (throw a validation
			// exception if it does) and then branch on the basis of what's in the command object, delegating to the new functions created
			// in the widget definition service to support various data scenarios.  Specifically:
			// 		if there's a group_id, delegate to the group list function;
			//		if there's a user_id, delegate to the user list function;
			//		if the list of ids is not empty, delegate to the list function that takes a list of GUIDs;
			//		otherwise delegate to the 'listWidgetsPagedAndFiltered' function.
			// This ticket includes creating a bunch of integration tests to verify each code path, including the passing of non-existent
			// users, groups, GUIDs and invalid command object data.  Each result should be tested for the data that is returned, plus a
			// 200-level status code (unless the desired behavior is to provoke a data validation problem with the command object, which
			// returns 500).  This ticket must come AFTER the WDS1.x tickets are done, AFTER the WDS2 ticket is done and AFTER the WDC1
			// ticket is done.
			def result = widgetDefinitionService.list(params)
			statusCode = 200
			jsonResult = result as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def saveWidgetLoadTime = {
		def jsonResult

		try {
			def result = widgetDefinitionService.saveWidgetLoadTime(params)
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def show = {
		def statusCode
		def jsonResult
		try {
			def widgetDefinition = WidgetDefinition.findByWidgetGuid(params.widgetGuid)
			if (widgetDefinition == null) {
				throw new OwfException(message: 'Widget Definition ' + params.widgetGuid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
			def result = [success: true, widgetDefinition: widgetDefinition]
			statusCode = 200
			def widgetDefinitionList = result.widgetDefinition.collect { serviceModelService.createServiceModel(it) }
			jsonResult = [success: result.success, data: widgetDefinitionList] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during show: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}
}

class WidgetCachedImageCommand {
	String widgetGuid
	String name

	static constraints = {
		widgetGuid blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/
		name blank: false, inList: ['imageUrlSmall', 'imageUrlLarge']
	}
}

class WidgetCreateOrUpdateCommand {
	def widgetDefinitionService

	// Raw input
	String data
	String tab
	String update_action
	String widget_id

	// What we'll use in the controller.
	JSONArray parsedWidgetJson
	JSONArray parsedPrincipalLikeJson

	static constraints = {
		tab nullable: true, blank: false, inList: ['users', 'groups'], validator: { val, obj ->
			if (val) { obj.update_action && obj.widget_id }
		}
		update_action nullable: true, blank: false, inList: ['add', 'remove'], validator: { val, obj ->
			if (val) { obj.tab && obj.widget_id }
		}
		widget_id nullable: true, blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/, validator: { val, obj ->
			if (val) { obj.tab && obj.update_action }
		}
		data blank: false, validator: { val, obj ->
			def retVal = true
			if (obj.tab == null) {
				JSONArray parsedJson = obj.widgetDefinitionService.parseIncomingJsonDataAsWidgets(val)
				obj.parsedWidgetJson = parsedJson

				retVal = retVal && (obj.widgetDefinitionService.validateJsonDataAsWidgets(parsedJson))
			}
			else {
				// We actually don't care what kind of data it is, as long as there's an ID attribute that's non-negative.
				def pData = JSON.parse(val)
				obj.parsedPrincipalLikeJson = pData
				pData.each {
					retVal = retVal && (it.id >= 1)
				}
			}

			return retVal
		}
	}
}

class WidgetDependentsCommand {
	List<String> ids
	boolean noRecurse
	boolean recurse

	static constraints = {
		ids validator: { val, obj ->
			// Pardon the double-negative here about "not no recursive" but it simplifies the service layer API.  Basically
			// masking an unfortunate choice when the JavaScript API was designed.
			obj.recurse = !obj.noRecurse
			if (val.isEmpty()) return false
			def retVal = true
			val.each { guid ->
				if (!guid.matches(/^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/)) {
					retVal = false
				}
			}
			return retVal
		}
	}
}

class WidgetExportCommand {
	String filename
	String id

	static constraints = {
		filename blank: false
		id blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/
	}
}
