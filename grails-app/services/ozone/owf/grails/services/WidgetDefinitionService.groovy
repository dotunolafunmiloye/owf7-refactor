package ozone.owf.grails.services

import grails.converters.JSON
import grails.gorm.DetachedCriteria

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.CacheMode

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class WidgetDefinitionService {

	// FIXME: The APIs of this service should provide for explicit naming of users if applicable.  The admin checks should
	// be handled by the controller, not the service.  Long-term goal is to make this service not depend upon any other
	// service, including account service.
	def accountService

	// FIXME: Not sure of points, but remove all dependence on the domain mapping service.  Long-term goal is to delete
	// this service.
	def domainMappingService

	// FIXME: Retire this in favor of letting the controllers do this either by calling a method on the domain class itself
	// or if absolutely necessary, by calling the service.  Long-term goal is to delete this service.
	def serviceModelService

	// FIXME: Once the widget definition service is fixed, we should be able to collapse what happens in the MP service into
	// the widget definition service and remove the marketplace service.  This was the original intent, but the WD service
	// was so complex that the MP service was created to address the needs of widget sync w/ MP.
	def marketplaceService

	// FIXME: Chances are this can be removed as it's only used in one location and that to actually write records in the
	// database linking a person to widgets.  Should be able to write these relationships directly and not rely upon the
	// service at all.
	def personWidgetDefinitionService

	// FIXME: At least one reference to this object is to read the configuration for a MP location (the URL of a configured MP).
	// This usage should be retired
	def grailsApplication

	private def addExternalWidgetsToUser(params) {
		def mpSourceUrl
		log.debug "addExternalWidgetsToUser ${params}"
		if (params?.marketplaceUrl) {
			mpSourceUrl = params.marketplaceUrl
		}
		else {
			def configUrl = "${grailsApplication?.config?.owf?.marketplaceLocation?: ''}"
			if (configUrl.isAllWhitespace()) {
				throw new OwfException(message: 'Missing (or all whitespace) config value: owf.marketplaceLocation', exceptionType: OwfExceptionTypes.Configuration)
			}
			mpSourceUrl = configUrl
		}

		//add widgets to db also add pwd mappings to current user
		def widgetDefinitions = []
		def widgetsToProcess = []

		// Really, this should be happening in the controller, but there are so
		// many service interdependencies that it has to happen pretty much
		// anywhere we use JSON that could be coming from outside.
		if (params?.widgets) {
			def widgetsRaw = []
			try {
				widgetsRaw.addAll(JSON.parse(params.widgets))
				widgetsRaw.each {
					if (it instanceof JSONObject)
						widgetsToProcess.add(it)
					else
						widgetsToProcess.add(JSON.parse(it))
				}
			} catch (Throwable e) {
				throw new OwfException(message: 'Widget definition JSON string is malformed.', exceptionType: OwfExceptionTypes.Validation)
			}
		}

		// Assuming we're here, there's something for us to actually process
		// for the user.
		widgetsToProcess.each {
			def widgetDefinition = createWidgetDefinitionFromJSON(it, mpSourceUrl)
			widgetDefinitions.push(widgetDefinition)
		}

		// Now that we have all the widget definitions, create the user to
		// widget definition mappings (PersonWidgetDefinition) and update
		// any dependency mappings.
		//
		// DOING THIS THE HARD WAY BECAUSE THESE TWO SERVICES SHARE A MUTUAL
		// DEPENDENCY ON EACH OTHER (YUCK).
		if (personWidgetDefinitionService == null) {
			personWidgetDefinitionService = grailsApplication.mainContext.getBean("personWidgetDefinitionService")
		}
		if (widgetDefinitions.size > 0) {
			def user = accountService.getLoggedInUser()
			personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(user, widgetDefinitions)

			widgetsToProcess.each {
				if (it.directRequired != null) {
					marketplaceService.updateWidgetDomainMappings(it.widgetGuid, it.directRequired as Set)
				}
			}
		}

		return [success: true, data: widgetDefinitions]
	}

	// TODO:  WDS16 -- Single-point story to ensure that this code is executed (clean it up and inline it) in the updateWidget function that
	// was added by commit ID 739c82e.  Regrettably, the universal name is allowed blank and null, making the test a little more cumbersome,
	// but the real icky part is the findWhere.  Why not do findByUniversalNameAndNotWidgetGuid(widgetDef.universalName, widgetDef.widgetGuid)
	// and if you get an answer back, throw a validation exception.
	private def canUseUniversalName(widgetDef, name) {
		if (widgetDef && name && !name.equals(""))  {
			// Search for this universal name.  Trim the name to disallow variations with
			// leading and trailing whitespace.
			def widget = WidgetDefinition.findWhere(universalName: name.trim())
			if (widget != null && widget.id != widgetDef.id) {
				return false
			}
		}
		return true
	}

	// TODO:  WDS18 -- Single-point story to determine if "convertJsonParamToDomainField" can be chopped down a little bit
	// based on API usage.  This is only used in one location in this file and has to do with the field that query results
	// will be sorted upon.  Seems rather excessive as we only allow a limited number of options from the UI, but confirm
	// this and possibly reduce/inline this code.
	private def convertJsonParamToDomainField(jsonParam) {
		switch(jsonParam) {
			case ['name','displayName','value.namespace']:
				return 'displayName'
			case ['version','widgetVersion','value.widgetVersion']:
				return 'widgetVersion'
			case ['description', 'value.description']:
				return 'description'
			case ['widgetGuid','path']:
				return 'widgetGuid'
			case ['universalName']:
				return 'universalName'
			case ['url','widgetUrl','value.url']:
				return 'widgetUrl'
			case ['width','value.width']:
				return 'width'
			case ['height','value.height']:
				return 'height'
			case ['headerIcon','imageUrlSmall','value.smallIconUrl','value.headerIcon']:
				return 'imageUrlSmall'
			case ['image','imageUrlLarge','value.largeIconUrl','value.image']:
				return 'imageUrlLarge'
			case ['singleton','value.singleton']:
				return 'singleton'
			case ['visible','value.visible']:
				return 'visible'
			case ['background','value.background']:
				return 'background'
			case ['descriptorUrl']:
				return 'descriptorUrl'
			case ['widgetTypes', 'value.widgetTypes']:
				return 'widgetType.name'
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class WidgetDefinition has not been mapped in WidgetDefinitionService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: WidgetDefinition",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}


	/**
	 * An internal method which takes a JSON object and attempts to create a
	 * widget definition record from the same.
	 *
	 * @param raw the input object.
	 * @param mpSourceUrl the marketplace URL which served the input object.
	 *
	 * @return the resulting widget definition.
	 */
	// TODO:  WDS8 -- Single-point story to inline "createWidgetDefinitionFromJSON" as it's only used from one location
	// in this class.
	private WidgetDefinition createWidgetDefinitionFromJSON(JSONObject obj, String mpSourceUrl) {
		def widgetDefinition = WidgetDefinition.findByWidgetGuid(obj.widgetGuid, [cache: true])
		if (widgetDefinition == null) {
			log.debug "createWidgetDefinitionFromJSON couldn't find widget ${obj.widgetGuid}"

			// MP Synchronization
			// The default is to fetch a widget from a well-known MP.  If we can't do that
			// or if the fetch fails, then fall back to the original behavior, which reads
			// the supplied JavaScript and creates a widget from that.
			HashSet setWidgets = []
			try {
				HashSet stWidgetJson = marketplaceService.buildWidgetListFromMarketplace(obj.widgetGuid, mpSourceUrl)
				List<WidgetDefinition> lstWidgets = marketplaceService.addListingsToDatabase(stWidgetJson)
				setWidgets.addAll(lstWidgets)
			} catch (Exception e) {
				log.error "addExternalWidgetsToUser: unable to build widget list from Marketplace, message -> ${e.getMessage()}", e
			}

			if (setWidgets.isEmpty()) {
				// If set is empty, then call to MP failed.  Fallback path.
				log.debug("Importing from the JSON provided to us, since marketplace failed")
				List<WidgetDefinition> lstWidgets = marketplaceService.addListingsToDatabase([obj])
				setWidgets.addAll(lstWidgets)
			}
			// MP Synchronization
			// See comments on the MarketplaceService regarding what functionality should/could
			// be moved back into this service.
			widgetDefinition = setWidgets.find { it.widgetGuid == obj.widgetGuid }
		} else {
			log.debug "createWidgetDefinitionFromJSON found ${obj.widgetGuid}"
		}
		return widgetDefinition
	}

	// TODO:  WDS19 -- Two-point story to move the admin checks performed by "ensureAdmin" into the calling locations.  There are
	// three references to this function in this class, meaning that there are likely three calls in the controller (Widget or
	// WidgetDefinition) where we would need to make suitable changes.  However, that's not cast in concrete, so verify and make
	// the necessary changes.  Once the change is done, remove this function and all references to it.
	private def ensureAdmin() {
		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message: "You must be an admin", exceptionType: OwfExceptionTypes.Authorization)
		}
	}

	private def isNull(obj) {
		if (obj == null) {
			return true
		}
		else return obj.equals(null)
	}

	public void updateWidget(JSONObject wdProperties, WidgetDefinition wd, WidgetType wt) {
		wd.properties = wdProperties
		wd.widgetType = wt ?: WidgetType.findByName(WidgetType.STANDARD)
		if (!wd.save()) {
			if (wd.hasErrors()) {
				throw new OwfException(message: 'A validation error occurred while saving a widget; the data has not been persisted.  Validation Errors: ' + wd.errors.toString(),
				exceptionType: OwfExceptionTypes.Validation)
			}
			throw new OwfException(message: 'A validation error occurred while saving a widget; the data has not been persisted.', exceptionType: OwfExceptionTypes.Database)
		}
	}

	// TODO:  WDS5.2 -- Create stand-alone "setGroupsForWidget(JSONArray groups, WidgetDefinition wd, boolean isAdd = false)"
	// TODO:  WDS5.3 -- Create stand-alone "setUsersForWidget(JSONArray users, WidgetDefinition wd, boolean isAdd = false)"
	private def updateWidget(params) {
		def widgetDefinition
		def returnValue = null

		//check for id param if exists this is an update
		if (params.id || params.widget_id) {
			params.id = params.id ? params.id : params.widget_id
			widgetDefinition = WidgetDefinition.findByWidgetGuid(params.id, [cache: true])
			if (widgetDefinition == null) {
				throw new OwfException(message: 'WidgetDefinition ' + params.id + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
		}

		// FIXME: This part of the API is likely only exercised from the Widget Editor "Groups" tab and should
		// be split into a separate API call.  In fact, there's probably an existing bit of code over in the
		// GroupService class which can add widgets to groups.  Use that via delegation rather than re-do here.
		// TODO:  WDS6 -- Single-point story to confirm that widget assignment to groups can be delegated to
		// the group service for completion.
		if ('groups' == params.tab) {
			def updatedGroups = []
			def group_ids = []
			def groups = JSON.parse(params.data)

			groups.each { it ->
				def group = Group.findById(it.id, [cache: true])

				if (group) {
					if (params.update_action == 'add') {
						domainMappingService.createMapping(group, RelationshipType.owns, widgetDefinition)
					}
					else if (params.update_action == 'remove') {
						domainMappingService.deleteMapping(group, RelationshipType.owns, widgetDefinition)
					}
					updatedGroups << group
				}
			}
			if (!updatedGroups.isEmpty()) {
				returnValue = updatedGroups.collect { serviceModelService.createServiceModel(it) }
			}
		}
		// FIXME: Similar to above with groups, but now applied to people.  There is similar code executed in the
		// person widget definition service when a user logs in (the call chain begins with SecurityFilters).  Take
		// a look and it may be possible to delegate; if not, then it should be a jump start on how to implement this and
		// do so in a common way so that there's only one place to look for managing the person to widget relationship.
		// TODO:  WDS7 -- Single-point story to factor out widget assignment to individual users; possible to see a second
		// single-point story to extract the common parts of the algorithm and share it with the what happens in the
		// security filter code.
		else {
			def updatedPeople = []
			def users = JSON.parse(params.data)

			users?.eachWithIndex { it, i ->
				def person = Person.findById(it.id.toLong(), [cache: true])
				if (person) {
					def criteria = PersonWidgetDefinition.createCriteria()
					def results = criteria.list() {
						eq("person", person)
						eq("widgetDefinition", widgetDefinition)
					}
					if (params.update_action == 'add') {
						if (results.size() == 0) {
							def queryReturn = PersonWidgetDefinition.executeQuery("SELECT MAX(pwd.pwdPosition) AS retVal FROM PersonWidgetDefinition pwd WHERE pwd.person = ?", [person])
							def maxPosition = (queryReturn[0] != null) ? queryReturn[0] : -1
							maxPosition++

							def personWidgetDefinition = new PersonWidgetDefinition(
									person: person,
									widgetDefinition: widgetDefinition,
									userWidget: true,
									visible: true,
									pwdPosition: maxPosition)

							person.addToPersonWidgetDefinitions(personWidgetDefinition)
							widgetDefinition.addToPersonWidgetDefinitions(personWidgetDefinition)
						}
						else {
							results.each { result ->
								result.userWidget = true
								result.save(flush: true)
							}
						}
					}
					else if (params.update_action == 'remove') {
						results?.eachWithIndex { nestedIt, j ->
							if (!nestedIt.groupWidget) {
								// If widget is not assigned directly or via a group, remove the pwd.
								person.removeFromPersonWidgetDefinitions(nestedIt)
								widgetDefinition.removeFromPersonWidgetDefinitions(nestedIt)
							}
							else {
								// Otherwise, just un-flag the direct widget to user association.
								nestedIt.userWidget = false
								nestedIt.save(flush: true)
							}
						}
					}

					updatedPeople << person
				}
			}
			if (!updatedPeople.isEmpty()) {
				returnValue = updatedPeople.collect { serviceModelService.createServiceModel(it) }
			}
		}

		return returnValue
	}

	// TODO: Make this public, with a defined return type (void).
	def delete(List<String> widgets) {
		def dSomething = new DetachedCriteria(WidgetDefinition).build {
			inList 'widgetGuid', widgets
		}
		def PwdListCriteria = new DetachedCriteria(PersonWidgetDefinition).build {
			inList 'widgetDefinition', dSomething.list()
		}
		PwdListCriteria.deleteAll()

		def dListCriteria = new DetachedCriteria(WidgetDefinition).build {
			inList 'widgetGuid', widgets
			projections {
				property 'id'
			}
		}

		def dmCriteria = new DetachedCriteria(DomainMapping).build {
			or {
				and {
					inList 'srcId', dListCriteria.list()
					eq 'srcType', WidgetDefinition.TYPE
				}
				and {
					inList 'destId', dListCriteria.list()
					eq('destType', WidgetDefinition.TYPE)
				}
			}
		}
		dmCriteria.deleteAll()

		def dDeleteCriteria = new DetachedCriteria(WidgetDefinition).build {
			inList 'widgetGuid', widgets
		}
		dDeleteCriteria.deleteAll()
	}

	/**
	 * Search for widgets which are required by the supplied list of GUIDs, optionally doing so
	 * recursively.
	 *
	 * @param guids the base list of GUIDs.
	 * @param noRecurse whether to search exhaustively.
	 *
	 * @return a list of widget definitions.
	 */
	public List<WidgetDefinition> getDependents(List<String> guids, boolean recurse = false) {
		def getInnerDependents = { ids ->
			if (!ids.isEmpty()) { // This guard is necessary; integration tests pass just fine w/o, but MySQL complains at runtime.
				return DomainMapping.withCriteria {
					inList 'destId', ids
					eq 'destType', WidgetDefinition.TYPE
					eq 'srcType', WidgetDefinition.TYPE
					eq 'relationshipType', RelationshipType.requires.toString()
					projections {
						distinct 'srcId' // Probably not required 'distinct', better safe than sorry.
					}
				}
			}
			else {
				return []
			}
		}

		// Main execution begins here.
		Set<Long> wdDependentIds = [] as Set

		// Get the initial listing of dependent widgets.
		def dcWidget = new DetachedCriteria(WidgetDefinition).build {
			inList 'widgetGuid', guids
			projections {
				property 'id'
			}
		}
		def dcDomainMappingBase = new DetachedCriteria(DomainMapping).build {
			inList 'destId', dcWidget.list()
			eq 'destType', WidgetDefinition.TYPE
			eq 'srcType', WidgetDefinition.TYPE
			eq 'relationshipType', RelationshipType.requires.toString()
			projections {
				distinct 'srcId' // Probably not required 'distinct', better safe than sorry.
			}
		}
		wdDependentIds.addAll(dcDomainMappingBase.list())

		// Now, go shopping for any recursively-dependent widgets (if applicable).
		if (recurse) {
			def recursive = getInnerDependents(wdDependentIds)
			while (recursive.size() > 0) {
				def newAdds = recursive.minus(wdDependentIds)
				wdDependentIds.addAll(newAdds)
				recursive = getInnerDependents(newAdds)
			}
		}

		List<WidgetDefinition> processedWidgets = []
		if (wdDependentIds && wdDependentIds.size() > 0) {
			processedWidgets = WidgetDefinition.withCriteria({
				inList 'id', wdDependentIds
			})
		}

		return processedWidgets
	}

	/**
	 * Search for widgets the supplied list of GUIDs require, optionally doing so recursively.  Effectively the
	 * reciprocal of the getDependents API call in that the code is almost exactly the same but the search direction
	 * is exactly opposite.
	 *
	 * @param guids the base list of GUIDs.
	 * @param noRecurse whether to search exhaustively.
	 *
	 * @return a list of widget guids.
	 */
	public List<String> getRequirements(List<String> guids, boolean recurse = false) {
		def getInnerDependents = { ids ->
			if (!ids.isEmpty()) { // This guard is necessary; integration tests pass just fine w/o, but MySQL complains at runtime.
				return DomainMapping.withCriteria {
					inList 'srcId', ids
					eq 'srcType', WidgetDefinition.TYPE
					eq 'destType', WidgetDefinition.TYPE
					eq 'relationshipType', RelationshipType.requires.toString()
					projections {
						distinct 'destId' // Probably not required 'distinct', better safe than sorry.
					}
				}
			}
			else {
				return []
			}
		}

		// Main execution begins here.
		Set<Long> wdDependentIds = [] as Set

		// Get the initial listing of dependent widgets.
		def dcWidget = new DetachedCriteria(WidgetDefinition).build {
			inList 'widgetGuid', guids
			projections {
				property 'id'
			}
		}
		def dcDomainMappingBase = new DetachedCriteria(DomainMapping).build {
			inList 'srcId', dcWidget.list()
			eq 'srcType', WidgetDefinition.TYPE
			eq 'destType', WidgetDefinition.TYPE
			eq 'relationshipType', RelationshipType.requires.toString()
			projections {
				distinct 'destId' // Probably not required 'distinct', better safe than sorry.
			}
		}
		wdDependentIds.addAll(dcDomainMappingBase.list())

		// Now, go shopping for any recursively-dependent widgets (if applicable).
		if (recurse) {
			def recursive = getInnerDependents(wdDependentIds)
			while (recursive.size() > 0) {
				def newAdds = recursive.minus(wdDependentIds)
				wdDependentIds.addAll(newAdds)
				recursive = getInnerDependents(newAdds)
			}
		}

		List<String> processedWidgets = []
		if (wdDependentIds && wdDependentIds.size() > 0) {
			processedWidgets = WidgetDefinition.withCriteria {
				inList 'id', wdDependentIds
				projections {
					property 'widgetGuid'
				}
			}
		}

		return processedWidgets
	}

	// TODO:  WDS1.1 -- Single-point story to create a filtering mechanism for widget definitions similar to the 'createDashboardFilterCriteria'
	// that exists in the dashboard service.  The function should accept a parameter, which is a JSONArray corresponding to the filters provided
	// from the UI (in other words, the command object will receive a raw string as 'filters' from the browser and should parse that into a JSONArray)
	// and a second parameter which is optionally defined as the filter operator.  If no filter operator is supplied, assume that it is 'AND'.  There
	// is NO REQUIREMENT in this ticket to actually invoke the code -- the ticket and points just cover creating the function.  The return type is
	// a detached criteria object.
	//
	// TODO:  WDS1.2 -- Two-point story to create three functions.  First is 'listWidgetsPagedAndFiltered' that takes a command object from the controller
	// and executes a query against widget_definition, sorting, paging and filtering according to the contents of the command object.  For reference,
	// see the similar function 'listDashboardsForSingleUserPagedAndFiltered' in the dashboard service.  Second function is 'listWidgetsPagedAndFilteredForUser'
	// and takes the command object and a person object based on the supplied user id from the web browser.  The general aim is the same as the first function,
	// only with the added constraint that the resulting widgets should be related to the user (that is, there's a person widget definition).   Third function
	// is a clone, effectively, of the second function, only aimed at groups instead of users.  This time, the function will need to use the domain mapping table
	// to find widgets that the group 'owns' and then return those widgets, with any additional paging, sorting and filtering options applied as well.  There is
	// NO REQUIREMENT in this ticket to actually invoke the code -- the ticket and points just cover creating the functions.  The return type for all functions
	// is a map [:] containing the following elements:
	//		** success: true,
	// 		** results: count of widgets being returned,
	// 		** data: query results expressed as service model objects
	//
	// TODO:  WDS1.3 -- Single point story to create a single function that accepts a list of GUIDs and returns the widget definitions for those GUIDs.  There is
	// NO REQUIREMENT in this ticket to actually invoke the code -- the ticket and points just cover creating the function.  The return type is a map [:] containing
	// the following elements:
	//		** success: true,
	// 		** results: count of widgets being returned,
	// 		** data: query results expressed as service model objects
	def list(params) {
		def widgetDefinition = null
		def opts = [:]
		if (params?.offset) opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		if (params?.max) opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)

		// Either group_id or groupIds is passed, but not both
		if (params?.group_id) {
			def tempArr = []
			params.groupIds = "[" + params.group_id + "]"
		}

		//filter by any groups passed first
		def groupFilteredIds = []
		if (params?.groupIds) {
			for (groupId in JSON.parse(params?.groupIds)) {
				def tempGroupFilteredIds = []
				def group = Group.get(groupId.toLong())
				if (group != null) {
					def mappings = domainMappingService.getMappings(group, RelationshipType.owns, WidgetDefinition.TYPE)
					mappings?.each {
						tempGroupFilteredIds << it.destId
					}
				}

				if (groupFilteredIds.isEmpty()) {
					groupFilteredIds = tempGroupFilteredIds
				}
				else {
					groupFilteredIds = groupFilteredIds.intersect(tempGroupFilteredIds)
				}

				if (groupFilteredIds.isEmpty()) {
					return [success: true, results: 0, data: []]
				}
			}
		}

		widgetDefinition = WidgetDefinition.createCriteria().list(opts) {
			if (params?.id)
				inList("widgetGuid",params.list('id'))
			if (params?.sort)
				order(convertJsonParamToDomainField(params.sort), params?.order?.toLowerCase() ?: 'asc')
			if(params?.widgetGuid) like("widgetGuid", params.widgetGuid)
			if(params?.universalName) like("universalName", params.universalName)
			if(params?.widgetName) like("displayName", params.widgetName)
			if(params?.widgetVersion) like("widgetVersion", params.widgetVersion)

			// FIXME: To be addressed by WDS1.1 described above.  Preferred coding pattern is as follows:
			//
			//			def filter = new DetachedCriteria(WidgetDefinition).build {}
			//			if (cmd.preParsedFilters) {
			//				filter = createWidgetDefinitionFilterCriteria(cmd.preParsedFilters)
			//			}
			//
			//			def widgets = filter.build { ... }
			if(params?.filters) {
				if (params.filterOperator?.toUpperCase() == 'OR') {
					or {
						JSON.parse(params.filters).each {
							if (it.filterField == 'singleton') {
								if (it.filterValue) {
									eq('singleton', true)
								}
								else {
									or {
										eq('singleton', false)
										isNull('singleton')
									}
								}
							} else if (it.filterField == 'visible') {
								if (it.filterValue) {
									eq('visible', true)
								}
								else {
									or {
										eq('visible', false)
										isNull('visible')
									}
								}
							}
							else {
								ilike(it.filterField, '%' + it.filterValue + '%')
							}
						}
					}
				}
				else {
					JSON.parse(params.filters).each {
						if (it.filterField == 'singleton') {
							if (it.filterValue) {
								eq('singleton', true)
							}
							else {
								or {
									eq('singleton', false)
									isNull('singleton')
								}
							}
						} else if (it.filterField == 'visible') {
							if (it.filterValue) {
								eq('visible', true)
							}
							else {
								or {
									eq('visible', false)
									isNull('visible')
								}
							}
						}
						else {
							ilike(it.filterField, '%' + it.filterValue + '%')
						}
					}
				}
			}

			if (params?.user_id) {
				personWidgetDefinitions {
					person {
						eq('id',Long.parseLong(params.user_id))
					}

					//only list widgets that are explicitly assigned
					//to this user
					eq("userWidget", true)
				}
			}

			if (!groupFilteredIds.isEmpty()) {
				inList('id', groupFilteredIds)
			}

			cache(true)
			cacheMode(CacheMode.GET)
		}

		def processedWidgets = widgetDefinition.collect { w ->
			//calc user count
			def userCount = PersonWidgetDefinition.withCriteria {
				eq('widgetDefinition', w)
				projections {
					rowCount()
				}
			}
			serviceModelService.createServiceModel(w, [
						totalUsers: userCount[0],
						totalGroups: domainMappingService.countMappings(w, RelationshipType.owns, Group.TYPE, 'dest')
					])
		}
		return [success: true, results: widgetDefinition.totalCount, data: processedWidgets]
	}

	// TODO:  WDS10 -- Single-point story to tighten down the contract of "saveWidgetLoadTime" or eliminate it altogether and
	// place this bit of code up in the controller.  The whole point is to generate a log statement.
	def saveWidgetLoadTime(params) {
		def success = false
		def msg = ''

		//just log for now
		if (params?.id != null && params?.loadTime != null) {
			msg = "Widget ${params.id} loaded in ${params.loadTime} (ms)"
			success = true
		}
		else {
			msg = "saveWidgetLoadTime was called with missing id or loadTime params - ${params}"
			success = false
		}

		log.info msg
		[success: success, msg: msg]
	}

	/**
	 * Used by the widget definition controller when parsing user input data from (typically) the widget editor admin widget.
	 *
	 * @param jsonData the raw JSON data representing a widget(s).
	 *
	 * @return an array of parsed, translated JSON objects mapping the incoming fields onto the actual domain fields in the database.
	 */
	public JSONArray parseIncomingJsonDataAsWidgets(String jsonData) {
		def tmpJson = JSON.parse(jsonData)
		JSONArray arrReturn = new JSONArray()

		tmpJson.each { jsObject ->
			// Translate between front-end and back-end
			jsObject.widgetVersion = jsObject.version
			jsObject.displayName = jsObject.name
			jsObject.widgetUrl = jsObject.url
			jsObject.imageUrlSmall = jsObject.headerIcon
			jsObject.imageUrlLarge = jsObject.image
			jsObject.isNewWidget = (jsObject.id == '')
			def widgetTypes = jsObject.widgetTypes.collect { it.name }
			jsObject.widgetTypeName = widgetTypes.size() > 0 ? widgetTypes[0] : ''

			jsObject.remove('version')
			jsObject.remove('name')
			jsObject.remove('url')
			jsObject.remove('headerIcon')
			jsObject.remove('image')
			jsObject.remove('id')
			jsObject.remove('widgetTypes')

			arrReturn.add(jsObject)
		}
		return arrReturn
	}

	/**
	 * Used by the widget definition controller when parsing user input data from (typically) the widget editor admin widget to
	 * verify that the supplied data will actually save to the database.
	 *
	 * @param jsonData an array of pre-parsed JSON data representing widget definitions (either for update or create)
	 *
	 * @return true if the data can be saved.
	 */
	public boolean validateJsonDataAsWidgets(JSONArray jsonData) {
		boolean retVal = true
		jsonData.each { widgetObject ->
			WidgetDefinition wd = new WidgetDefinition()
			wd.properties = widgetObject
			wd.widgetGuid = UUID.randomUUID().toString()
			wd.widgetType = WidgetType.findByName(WidgetType.STANDARD)

			retVal = retVal && wd.validate()
		}
		return retVal
	}
}
