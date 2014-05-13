package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes

class PersonWidgetDefinitionController extends BaseOwfRestController {

	def modelName = 'personWidgetDefinition'
	def accountService
	def personWidgetDefinitionService

	def approvePersonWidgetDefinitions = {
		//PersonWidgetDefinitionApproveCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.approveForAdminByTags(params)
			statusCode = 200
			jsonResult = result as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during approvePersonWidgetDefinitionsByTags: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def bulkDelete = {
		//PersonWidgetDefinitionBulkDeleteCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.bulkDelete(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkDelete: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def bulkDeleteAndUpdate = {
		//PersonWidgetDefinitionBulkDeleteAndUpdateCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.bulkDeleteAndUpdate(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkDeleteAndUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def bulkUpdate = {
		//PersonWidgetDefinitionBulkUpdateCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.bulkUpdate(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def create = {
		//PersonWidgetDefinitionCreateCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.create(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during create: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def delete = {
		//PersonWidgetDefinitionDeleteCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.delete(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during delete: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def dependents = {
		//PersonWidgetDefinitionGetDependentsCommand cmd ->
		def jsonResult

		try {
			def result = personWidgetDefinitionService.getDependents(params)
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def list = {
		//PersonWidgetDefinitionListCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.list(params)
			statusCode = 200
			jsonResult = result.personWidgetDefinitionList as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def listPersonWidgetDefinitions = {
		//PersonWidgetDefinitionListPendingCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.listForAdminPendingWidgets(params)
			statusCode = 200
			jsonResult = [success: result.success, results: result.results, data: result.data] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during listPersonWidgetDefinitionsByTags: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def listUserAndGroupWidgets = { PersonWidgetDefinitionListUserAndGroupWidgetsCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()

		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'List user and group widgets command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def widgets = personWidgetDefinitionService.listWidgetsForUser(liU, cmd.widgetGuid, cmd.widgetName)
			def result = widgets.collect { serviceModelService.createServiceModel(it, [totalUsers: 1, totalGroups: 0]) }
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

	def show = {
		//PersonWidgetDefinitionShowCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.show(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during show: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def update = {
		//PersonWidgetDefinitionUpdateCommand cmd ->
		def statusCode
		def jsonResult

		try {
			def result = personWidgetDefinitionService.update(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during update: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def widgetList = {
		//PersonWidgetDefinitionListCommand cmd ->
		def jsonResult
		def statusCode

		try {
			def result = personWidgetDefinitionService.list(params)
			statusCode = 200
			jsonResult = [success: result.success, results: result.count, rows: result.personWidgetDefinitionList] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during widgetList: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}
}

class PersonWidgetDefinitionApproveCommand {
}

class PersonWidgetDefinitionBulkDeleteAndUpdateCommand {
}

class PersonWidgetDefinitionBulkDeleteCommand {
}

class PersonWidgetDefinitionBulkUpdateCommand {
}

class PersonWidgetDefinitionCreateCommand {
	String displayName
	String guid
	String name
	Number personId
}

class PersonWidgetDefinitionDeleteCommand {
}

class PersonWidgetDefinitionGetDependentsCommand {
}

class PersonWidgetDefinitionListCommand {
	List<String> customWidgetName
	List<String> customWidgetNameOrDesc
	Boolean disabled
	List<Number> groupIds
	Number max
	Number offset
	Boolean visible
	String widgetGuid
	List<String> widgetName
	List<String> widgetTypes
	String widgetVersion
}

class PersonWidgetDefinitionListPendingCommand {
}

class PersonWidgetDefinitionListUserAndGroupWidgetsCommand {
	String widgetGuid
	String widgetName

	static constraints = {
		widgetGuid nullable: true, blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/, validator: { val, obj ->
			if (val) { obj.widgetName == null }
		}
		widgetName nullable: true, blank: false, validator: { val, obj ->
			if (val) { obj.widgetGuid == null }
		}
	}
}

class PersonWidgetDefinitionShowCommand {
	String guid
	String universalName
}

class PersonWidgetDefinitionUpdateCommand {
	// RULES:
	// 1. One of 'displayName' or 'name' *MUST* be present, never both.
	// 2. Both 'guid' and 'personId' *MUST* be present.
	// 3. Other attributes ('pwdPosition' and 'visible') are optional.
	String displayName
	String guid
	String name
	Number personId
	Number pwdPosition = null
	Boolean visible = null
}
