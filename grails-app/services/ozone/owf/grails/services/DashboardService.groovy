package ozone.owf.grails.services

import javax.annotation.PostConstruct;


import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.CacheMode

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.Stack
import ozone.owf.grails.domain.WidgetDefinition

class DashboardService extends BaseService {

	final def uniqueIdRegex = /"uniqueId"\s*:\s*"[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}"/ // /"uniqueId"\s*\:\s*"[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}"/
	final def guidRegex = /[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}/

	def domainMappingService
	def serviceModelService
	def widgetDefinitionService

	def addOrRemove(params) {
		def returnValue = [:]

		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to update dashboards assocations.', exceptionType: OwfExceptionTypes.Authorization)
		}

		def dashboard = Dashboard.findByGuid(params.guid,[cache:true])
		if (dashboard == null) {
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		if (params.update_action != null && params.update_action != '' && 'groups'==params.tab) {
			def updatedGroups = []
			def groups = JSON.parse(params.data);

			groups.each { it ->
				def group = Group.findById(it.id,[cache:true])

				if (group) {
					if (params.update_action == 'add') {
						domainMappingService.createMapping(group, RelationshipType.owns, dashboard)
					}
					else if (params.update_action == 'remove') {
						domainMappingService.deleteMapping(group, RelationshipType.owns, dashboard)
					}
					updatedGroups << group
				}
			}
			if (!updatedGroups.isEmpty()) {
				returnValue = updatedGroups.collect{ serviceModelService.createServiceModel(it) }
			}
		}


		return [success:true, data:returnValue]
	}

	private def processGroupDashboards(groups,user) {
		def privateGroupDashboardToGroupsMap = [:]

		def maxPosition = 0
		if (user != null) {
			maxPosition = getMaxDashboardPosition(user)
		}
		if (maxPosition < 0) maxPosition = 0;

		//loop through group dashboards
		def bulkMappings = domainMappingService.getBulkMappings(groups, RelationshipType.owns, Dashboard.TYPE)
		bulkMappings.each { dm ->

			//if there is no user then there is no need to create private user copies
			if (user != null) {
				//check if this group dashboard already has a private copy for this user
				def privateGroupDashboards = getUserPrivateDashboards(user, dm.destId)

				//create private copy of the group dashboard for the user if they don't have one
				if (privateGroupDashboards.isEmpty()) {
					def args = [:]
					def groupDash = Dashboard.get(dm.destId)
					if (groupDash != null) {
						//use a new guid
						args.guid = java.util.UUID.randomUUID().toString()

						args.isdefault = groupDash.isdefault
						args.dashboardPosition = maxPosition + (groupDash.dashboardPosition ?: 0)
						args.name = groupDash.name
						args.description = groupDash.description
						args.locked = groupDash.locked
						args.layoutConfig = groupDash.layoutConfig
						args.stack = groupDash.stack

						def privateDash = deepClone(args,user.id)

						//save mapping
						domainMappingService.createMapping(privateDash.dashboard,RelationshipType.cloneOf,[id:dm.destId,TYPE:dm.destType])

						//save privateGroupDashboardInfo
						addGroupToDashboardToGroupsMap(dm.srcId, privateGroupDashboardToGroupsMap, privateDash.dashboard.id)
					}
				}
				else {
					//loop through each private group dashboard and save
					privateGroupDashboards.each {
						//save privateGroupDashboardInfo
						addGroupToDashboardToGroupsMap(dm.srcId, privateGroupDashboardToGroupsMap, it.id)
					}
				}
			}

			//Save Group for Group Dashboard as well
			addGroupToDashboardToGroupsMap(dm.srcId, privateGroupDashboardToGroupsMap, dm.destId)
		}
		return privateGroupDashboardToGroupsMap
	}

	private def addGroupToDashboardToGroupsMap(def groupId, def groupDashboardToGroupsMap, def mapKey) {
			if (groupDashboardToGroupsMap[mapKey] == null) {
				groupDashboardToGroupsMap[mapKey] = []as Set
			}
			Group group = Group.get(groupId)
			if (group != null) {
				groupDashboardToGroupsMap[mapKey] << group
			}
	}

	private def listDashboards(params) {
		def dashboardServiceModelList = []
		def opts = [:]

		//handle paging and sorting params
		if (params?.offset != null) {
			opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		}
		else if (params?.start != null) {
			params.offset = params.start
			opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		}

		if (params?.max != null) {
			opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)
		}
		else if (params?.limit != null) {
			params.max = params.limit
			opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)
		}

		if (params?.dir != null && params?.order == null) {
			params.order = params.dir
		}

		if (params.guid == null && params.id != null) {
			params.guid = params.id
		}

		if (params.user_id != null) {
			def person = Person.get(params.user_id)
			if (person) {
				params.user = person
			}
		}

		// Get stack default groups associated through by group association or direct association.
		def stackDefaultGroups = []
		if (params.user != null) {

			def stacks = Stack.withCriteria {
				groups {
					people {
						eq('id',params.user.id)
					}
				}
				cache(true)
				cacheMode(CacheMode.GET)
			}

			stackDefaultGroups = stacks.collect { it.findStackDefaultGroup() }
		}

		//get groups
		def groups = Group.withCriteria {


			if (params.user != null) {
				people {
					eq('id',params.user.id)
				}
			}

			eq('status','active')
			cache(true)

			//turn cache mode to GET which means don't use instances from this query for the 2nd level cache
			//seems to be a bug where the people collection is cached with only one person due to the people association filter above
			cacheMode(CacheMode.GET)
		}

		def groupDashboardIds = []as Set
		if (params.group_id != null) {
			Group group = Group.get(params.group_id)
			if (group != null) {
				domainMappingService.getMappings(group,RelationshipType.owns,Dashboard.TYPE).each { groupDashboardIds << it.destId }
			}
		}
		else if (params.isGroupDashboard == true || params.isGroupDashboard == 'true') {
			//      domainMappingService.getBulkMappings(groups,RelationshipType.owns,Dashboard.TYPE).each {
			//        groupDashboardIds << it.destId
			//      }
			groupDashboardIds = Dashboard.withCriteria {
				isNull('user')
				if (params.isStackDashboard == false || params.isStackDashboard == 'false') {
					isNull('stack')
				}
			}.id
		}

		//if group params are set then only group dashboards should be returned, if none are found an empty list should be returned
		if ((params.group_id != null || params.isGroupDashboard?.toString()?.toBoolean()) && groupDashboardIds.isEmpty()) {
			return [success: true, dashboardList: [], count: 0]
		}

		def stackDashboardIds = [] as Set
		if (params.stack_id != null) {
			Stack stack = Stack.get(params.stack_id)
			if (stack != null){
				def defaultGroup = stack.findStackDefaultGroup()

				// Added a fix here to adjust for the possibility that the group doesn't exist.
				if (defaultGroup) {
					domainMappingService.getMappings(defaultGroup,RelationshipType.owns,Dashboard.TYPE).each { stackDashboardIds << it.destId }
				}
			}
		}

		//if stack param is set then only stack default group dashboards should be returned, if none are found an empty list should be returned
		if((params.stack_id != null) && stackDashboardIds.isEmpty()) {
			return [success: true, dashboardList: [], count: 0]
		}

		//process any groupDashboards for this user
		def privateGroupDashboardToGroupsMap = [:]
		if (!stackDefaultGroups.isEmpty()) {
			privateGroupDashboardToGroupsMap = processGroupDashboards((groups << stackDefaultGroups).flatten(),params.user)
		} else {
			privateGroupDashboardToGroupsMap = processGroupDashboards(groups,params.user)
		}

		//get group dashboards
		def criteria = Dashboard.createCriteria()
		def userDashboardList = criteria.list(opts) {

			//filter by fields
			if (params.filters) {
				if (params.filterOperator?.toUpperCase() == 'OR') {
					or {
						JSON.parse(params.filters).each { filter ->
							ilike(filter.filterField, '%' + filter.filterValue + '%')
						}
					}
				}
				else {
					JSON.parse(params.filters).each { filter ->
						ilike(filter.filterField, '%' + filter.filterValue + '%')
					}
				}
			}

			if (params.user != null) {
				eq('user',params.user)
			}

			if (params.guid) {
				eq('guid', params.guid)
			}

			//filter by group dashboard ids
			if (groupDashboardIds != null && !groupDashboardIds.isEmpty()) {
				inList('id',groupDashboardIds)
			}

			//filter by stack dashboard ids
			if (stackDashboardIds != null && !stackDashboardIds.isEmpty()) {
				inList('id',stackDashboardIds)
			}

			if (params.isdefault != null) {
				eq('isdefault', convertStringToBool(params.isdefault) )
			}

			if (params.layout != null) {
				eq('layout', params.layout)
			}

			if (params.locked != null) {
				eq('locked', convertStringToBool(params.locked) )
			}

			if (params?.sort != null) {
				order(convertJsonParamToDomainField(params.sort), params?.order?.toLowerCase() ?: 'asc')
			}
			else {
				//default sort
				order('dashboardPosition', params?.order?.toLowerCase() ?: 'asc')
			}
			cache(true)
		}

		//loop through each dashboard and get group info and turn into json
		userDashboardList.each {
			def args = [:]

			//         if (params.group_id != null || (params.isGroupDashboard?.toString()?.toBoolean())) {
			//           args['isGroupDashboard'] = true
			//         }
			//         else {
			//           args['isGroupDashboard'] = false
			//         }

			args['isGroupDashboard'] = it.user == null ? true : false

			if (privateGroupDashboardToGroupsMap[it.id] != null) {
				args['groups'] = privateGroupDashboardToGroupsMap[it.id]
			}

			dashboardServiceModelList << serviceModelService.createServiceModel(it, args)
		}

		return [success: true, dashboardList: dashboardServiceModelList, count: userDashboardList.totalCount]

	}

	def listForAdmin(params) {
		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message: 'You are not authorized to list Admin dashboards.', exceptionType: OwfExceptionTypes.Authorization)
		}
		return listDashboards(params)
	}

	def listForUser(params) {
		params.user = accountService.getLoggedInUser()
		return listDashboards(params)
	}

	def showForUser(params) {
		if (params.guid == null && params.id != null) {
			params.guid = params.id
		}
		def dashboard = Dashboard.findByGuid(params.guid,[cache:true])
		if (dashboard == null) {
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
		if (accountService.getLoggedInUserIsAdmin() || dashboard.user.username.equals(accountService.getLoggedInUsername())) {

			//should only be one
			def groupDashboardMappings = domainMappingService.getMappings(dashboard,RelationshipType.cloneOf,Dashboard.TYPE)
			def groups = []
			if (groupDashboardMappings[0] != null) {
				def groupDash = Dashboard.get(groupDashboardMappings[0].destId)
				if (groupDash != null) {
					groups = domainMappingService.getMappedObjects(groupDash,RelationshipType.owns,Group.TYPE,[:],null,'dest')
				}
			}

			def dash = serviceModelService.createServiceModel(dashboard, ['groups':groups,isGroupDashboard:true])

			return [success:true, dashboard: dash]
		}
		else {
			throw new OwfException(message:'You are not authorized to view this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
		}
	}

	private def updateOldDefault(params){
		def isDefault = (params.isdefault == true || params.isdefault == "true" || params.isdefault== "on")
		if ( !isDefault || !isDefaultDashboardExists(params))
		{
			return
		}
		def oldDefault = null;
		try
		{
			oldDefault = getDefault(params).dashboard
		}
		catch (e){
			//Swallow, it's ok
		}
		if (oldDefault != null)
		{
			update([name: oldDefault.name, guid: oldDefault.guid, locked: oldDefault.locked, stack: oldDefault.stack, personId: params.personId, isdefault: false, doNotEnsureDefault: true])
		}
	}

	def createOrUpdate(params) {
		if (params.guid != null) {
			if (Dashboard.findByGuid(params.guid) == null) {
				params.cloned = true
				create(params)
			}
			else {
				update(params)
			}
		}
	}

	def create(params) {
		updateOldDefault(params)
		//default person to current user
		def person = accountService.getLoggedInUser()

		if (params.personId != null || params.user_id != null) {
			def personId = params.personId ?: params.user_id
			if (person.id != (personId as Integer) && !accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to create dashboards for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
			else {
				person = Person.get(personId);
			}
		}

		def maxPosition = getMaxDashboardPosition(person)
		maxPosition++

		def universalNameToOldGuidMap = [:]
		//Get a map between each widget's universalName and widgetGuid in the layoutConfig
		params.layoutConfig && params.layoutConfig instanceof String && getUniversalNameToGuidMap(universalNameToOldGuidMap, JSON.parse(params.layoutConfig))

		//If map isn't empty update all the widgetGuid's base on their universalName to
		//ensure that an imported dashboard maintains widgets that exist
		if(!universalNameToOldGuidMap.isEmpty()) {
			//Get all the widgets that match the universalNames given in the layoutConfig
			def universalNameMatches = WidgetDefinition.withCriteria({
				'in'("universalName", universalNameToOldGuidMap.keySet())
			})

			//For each widget replace its old guid with the current guid in the layoutConfig, using universalName to identify them
			universalNameMatches.each() { widget ->
				if(universalNameToOldGuidMap[widget.universalName] != widget.widgetGuid) {
					params.layoutConfig = params.layoutConfig.replace(universalNameToOldGuidMap[widget.universalName], widget.widgetGuid)
				}
			}
		}

		def dashboard = new Dashboard(
				name: params.name,
				guid: (params.cloned)? ((Dashboard.findByGuid(params.guid) != null)? java.util.UUID.randomUUID().toString() : params.guid) : params.guid,
				isdefault: convertStringToBool(params.isdefault),
				dashboardPosition: params.dashboardPosition != null ? params.dashboardPosition : maxPosition,
				description: JSONObject.NULL.equals(params.description) ? null : params.description,
				layoutConfig: params.layoutConfig.toString() ?: "",
				stack: params.stack != null ? Stack.get(params.stack.id.toLong()) : null,
				locked: params.locked != null ? params.locked : false)

		//if this is not a group dashboard then assign it to the specified user
		//otherwise group dashboards are not associated with any user
		if (!params.isGroupDashboard?.toString()?.toBoolean()) {
			dashboard.user = person
		}

		// Now handle the ids and configs.
		if (params.cloned) {
			regenerateAllConfigs(dashboard)
		}

		dashboard.validate()
		if (dashboard.hasErrors()) {
			throw new OwfException(	message:'A fatal validation error occurred during the creating of a dashboard. Params: ' + params.toString() + ' Validation Errors: ' + dashboard.errors.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}

		//explicit flush here so subsequent calls will see the new dashboard
		if (!dashboard.save(flush:true)) {
			throw new OwfException (message: 'A fatal error occurred while trying to save a dashboard. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Database)
		} else {
			return [success: true, dashboard: dashboard]
		}
	}

	def deleteForAdmin(params)
	{
		if (!accountService.getLoggedInUserIsAdmin())
		{
			throw new OwfException(message:'You are not authorized to delete Admin dashboards.', exceptionType: OwfExceptionTypes.Authorization)
		}
		def dashboard = Dashboard.findByGuid(params.guid)
		if (dashboard == null)
		{
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
		params.dashboard = dashboard
		deleteForUser(params)
	}

	def deleteForUser(params)
	{
		def dashboard = null
		if(params.dashboard != null){
			dashboard = params.dashboard
		}else{
			if (accountService.getLoggedInUserIsAdmin() && (params.adminEnabled == true)){
				dashboard = Dashboard.findByUserAndGuid(params.personId, params.guid)
			}else {
				dashboard = Dashboard.findByUserAndGuid(accountService.getLoggedInUser(), params.guid)
			}
		}

		if (dashboard == null)
		{
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		if (dashboard.user && !(dashboard.user.username.equals(accountService.getLoggedInUsername()))) {
			if (!(accountService.getLoggedInUserIsAdmin() && (params.adminEnabled == true)))
			{
				throw new OwfException(message:'You are not authorized to delete this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}

		try
		{

			// TODO: see if we need to delete all private copies of this dashboard

			//delete all mappings for this dashboard
			domainMappingService.purgeAllMappings(dashboard)

			dashboard.delete(flush:true)
			ensureDefault(dashboard.user)
			return [success: true, dashboard: dashboard]
		}
		catch (e)
		{
			log.error(e)
			throw new OwfException (message: 'A fatal error occurred while trying to delete a dashboard. Params: ' + params.toString(),exceptionType: OwfExceptionTypes.Database)
		}
	}

	def bulkDeleteAndUpdate(params){
		bulkDelete(params)
		bulkUpdate(params)
		return [success:true]
	}

	def bulkDeleteForAdmin(params){
		if (!accountService.getLoggedInUserIsAdmin())
		{
			throw new OwfException(message:'You are not authorized to bulkDelete Admin dashboards.', exceptionType: OwfExceptionTypes.Authorization)
		}
		if (params.viewGuidsToDelete == null){
			throw new OwfException(	message:'A fatal validation error occurred. ViewGuidsToDelete param required. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}
		JSON.parse(params.viewGuidsToDelete).each {
			def dashboard = Dashboard.findByGuid(it)
			if (dashboard == null)
			{
				throw new OwfException(message:'Dashboard ' + it + ' not found during bulk delete.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map newParams = new HashMap()
			newParams.guid = it
			newParams.dashboard = dashboard
			newParams.adminEnabled = true
			def result = delete(newParams)
		}
		return [success: true]
	}


	def bulkDelete(params){
		if (params.viewGuidsToDelete == null){
			throw new OwfException(	message:'A fatal validation error occurred. ViewGuidsToDelete param required. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}
		JSON.parse(params.viewGuidsToDelete).each {
			def dashboard = findByGuidForUser(it,null)
			if (dashboard == null)
			{
				throw new OwfException(message:'Dashboard ' + it + ' not found during bulk delete.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map newParams = new HashMap()
			newParams.guid = it
			def result = delete(newParams)
		}
		return [success: true]
	}

	def updateForAdmin(params) {
		if (!accountService.getLoggedInUserIsAdmin())
		{
			throw new OwfException(message:'You are not authorized to update Admin dashboards.', exceptionType: OwfExceptionTypes.Authorization)
		}

		def person = getPersonByParams(params)
		if(person == null && params.isGroupDashboard == false){
			throw new OwfException(message:'Person not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		def dashboard = Dashboard.findByGuid(params.guid)
		if (dashboard == null) {
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
		if (dashboard.user && !(dashboard.user.username.equals(accountService.getLoggedInUsername()))) {
			if (!(accountService.getLoggedInUserIsAdmin() && (params.adminEnabled == true)))
			{
				throw new OwfException(message:'You are not authorized to update this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}
		def retVal = updateDashboard(params, person, dashboard)
		return retVal
	}

	def updateForUser(params) {
		// look for a specified dashboard in db
		def dashboard = findByGuidForUser(params.guid, params.personId)

		if (!dashboard)
		{
			//dashboard was not present in db could be either auth or db
			if (Dashboard.findByGuid(params.guid))
			{
				throw new OwfException(message:'The requested dashboard was not found. Params: ' + params.toString(),
				exceptionType: OwfExceptionTypes.Authorization)
			}
			else
			{
				throw new OwfException(message:'The requested dashboard was not found. Params: ' + params.toString(),
				exceptionType: OwfExceptionTypes.NotFound)
			}
		}

		def retVal = updateDashboard(params, accountService.getLoggedInUser(), dashboard)
		return retVal
	}

	private def updateDashboard(params, person, dashboard){
		/*
		 * Do we need to set an old dashboard to no longer default?
		 * This can be (and was originally) done with one giant if statement.
		 * The problem was it was incomprehensible.
		 */
		if (params.isdefault != null)
		{
			if ( params.isdefault.toString().equalsIgnoreCase("true") || params.isdefault.toString().equalsIgnoreCase("on") )
			{
				def defaultResult = null
				try {defaultResult = getDefault(params, person)}catch (e){}
				if (defaultResult != null && defaultResult.success == true)
				{
					def defaultDashboard = defaultResult.dashboard
					def guidsMatch = defaultResult.dashboard?.guid.toString().equals(params.guid.toString())
					if (defaultDashboard != null && !guidsMatch)
					{
						defaultDashboard.isdefault = false
						defaultDashboard.save(flush:true)
					}
				}
			}
		}
		// update values of user defined dashboard and
		// validate prior to save

		dashboard.guid = params.guid ?: dashboard.guid
		dashboard.name = params.name ? params.name : dashboard.name
		dashboard.isdefault = params.isdefault != null ? convertStringToBool(params.isdefault) : dashboard.isdefault
		dashboard.dashboardPosition = params.dashboardPosition != null ? params.dashboardPosition as Integer : dashboard.dashboardPosition
		if (params.description) {
			dashboard.description = JSONObject.NULL.equals(params.description) ? null : params.description
		}

		dashboard.layoutConfig = params.layoutConfig ?: dashboard.layoutConfig
		// If no stack is provided, set the stack to null.
		// dashboard.stack =  params.stack != null ? Stack.get(params.stack.id.toLong()) : null
		dashboard.locked = params.locked instanceof Boolean ? params.locked : params.locked == "true"

		// TODO: Consider renaming the regenerateStateIds param.  This controls regenerating widget instance id's which are encapsulated in layout_config now instead of a state table.
		if (params.regenerateStateIds) {
			regenerateAllConfigs(dashboard);
		}

		dashboard.validate()

		if (dashboard.hasErrors()) {
			throw new OwfException(	message:'A fatal validation error occurred during the updating of a dashboard. Params: ' + params.toString() + ' Validation Errors: ' + dashboard.errors.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}
		if (!dashboard.save(flush:true)) {
			throw new OwfException (message: 'A fatal error occurred while trying to save a dashboard. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Database)
		} else {
			try
			{
				// On a successful save, if this is a group dashboard, cascade the inclusion of any new widgets to owning groups
				// if this is a group dashboards.
				if (dashboard.user == null) {
					// Get all the groups that own this dashboard
					def mappings = domainMappingService.getMappings(dashboard, RelationshipType.owns, Group.TYPE, 'dest');
					mappings?.each {
						// Get the actual group object.
						def group = Group.get(it.srcId)
						if (group != null) {
							if (dashboard.stack == null) {
								widgetDefinitionService.reconcileGroupWidgetsFromDashboards(group);
							}
							else {
								widgetDefinitionService.reconcileGroupWidgetsFromDashboards(group, false);
							}
						}
					}
				}
				if (params.doNotEnsureDefault == null || params.doNotEnsureDefault == false)
				{
					ensureDefault(dashboard.user)
				}


			}
			catch(e)
			{
				throw new OwfException (message: 'A fatal error occurred while trying to ensure dashboard defaults after a save. Changes have been backed out. Params: ' + params.toString(), exceptionType: OwfExceptionTypes.Database)
			}
			return [success: true, dashboard: dashboard]
		}

	}

	def restore(params) {

		def dashboard = Dashboard.findByGuid(params.guid,[cache:true])
		if (dashboard == null) {
			throw new OwfException(message:'Dashboard ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
		if (accountService.getLoggedInUserIsAdmin() || dashboard.user.username.equals(accountService.getLoggedInUsername())) {

			//should only be one
			def groupDashboardMappings = domainMappingService.getMappings(dashboard,RelationshipType.cloneOf,Dashboard.TYPE)
			def groups = []
			if (groupDashboardMappings[0] != null) {
				def groupDash = Dashboard.get(groupDashboardMappings[0].destId)
				if (groupDash != null) {
					groups = domainMappingService.getMappedObjects(groupDash,RelationshipType.owns,Group.TYPE,[:],null,'dest')

					def args = [:]
					args.guid = dashboard.guid

					args.isdefault = groupDash.isdefault
					args.name = groupDash.name
					args.description = groupDash.description
					if (params.isdefault != null) {
						args.isdefault = params.isdefault
					}
					args.locked = groupDash.locked

					//need to regenerate when updating an existing dash
					args.regenerateStateIds = true

					args.layoutConfig = groupDash.layoutConfig
					//args.stack = (groupDash.stack != null) ? ['id': groupDash.stack.id] : null

					updateDashboard(args,accountService.getLoggedInUser(),dashboard)
				}
			}

			return [success:true, data: [
					serviceModelService.createServiceModel(dashboard, ['groups':groups])
				]]
		}
		else {
			throw new OwfException(message:'You are not authorized to restore this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
		}
	}

	def deepClone(params){
		return deepClone(params,accountService.getLoggedInUser().id)
	}

	def deepClone(params, userid){
		if (userid != accountService.getLoggedInUser().id && !accountService.getLoggedInUserIsAdmin())
		{
			throw new OwfException(message:'You are not authorized to clone dashboards for other users.',
			exceptionType: OwfExceptionTypes.Authorization)
		}

		params.personId = userid
		params.cloned = true
		return create(params)
	}

	def bulkUpdate(params){
		if (params.viewsToUpdate == null){
			throw new OwfException(	message:'A fatal validation error occurred. viewsToUpdate param required. Params: ' + params.toString(),exceptionType: OwfExceptionTypes.Validation)
		}
		def position = 0
		JSON.parse(params.viewsToUpdate).each {
			def dashboard = findByGuidForUser(it.guid, params.personId)
			if (dashboard == null)
			{
				throw new OwfException(message:'Dashboard ' + it.guid + ' not found during bulk update.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map newParams = new HashMap()
			newParams.guid = it.guid
			newParams.name =  it.name //Updateable...
			newParams.isdefault = it.isdefault //Updatable...
			newParams.doNotEnsureDefault = true
			newParams.locked = it.locked //Updatable...
			if((params.updateOrder != null) && (convertStringToBool(params.updateOrder) == true))
			{
				newParams.dashboardPosition = "" + position //Update position...
			}
			else
			{
				newParams.dashboardPosition = null
			}
			def result = update(newParams)
			position ++
		}
		def person = Person.get(params.personId)
		ensureDefault(person)
		return [success:true]
	}

	private def ensureDefault(user) {
		def dashboard = Dashboard.findByUserAndIsdefault(user, true)
		if (dashboard != null)
		{
			return
		}
		def dashboards = Dashboard.createCriteria().list{
			and{eq('user',user)}
			order("dashboardPosition","asc")
		}
		if (dashboards.size() != 0)
		{
			dashboard = dashboards.toArray()[0]
			dashboard.isdefault = true
			dashboard.save()
		}
	}

	def getDefault(params, personParam=null) {
		// look for a user's default dashboard
		def person = personParam != null? personParam : getPersonByParams(params)
		def dashboard = Dashboard.findByUserAndIsdefault(person, true)

		// dashboard was present
		if (dashboard) {
			return [success: true, dashboard: dashboard]
		}
		// dashboard was not present
		else {
			throw new OwfException(message:'No default dashboard found. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.NotFound)
		}

	}

	private def isDefaultDashboardExists(params) {
		// found a default?
		try
		{
			return getDefault(params).success;
		}
		catch (e)
		{
			return false
		}
	}

	private def getPersonByParams(params, returnSelf=true){
		def person = null

		if (params.personId != null)
		{
			if (accountService.getLoggedInUserIsAdmin())
			{
				person = Person.get(params.personId)
				if (person == null)
				{
					throw new OwfException(message:'Person with id of ' + params.personId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else
			{
				throw new OwfException(message:'You are not authorized to modify dashboards for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}else if (params.username != null){

			if (accountService.getLoggedInUserIsAdmin())
			{
				person = Person.findByUsername(params.username)
				if (person == null)
				{
					throw new OwfException(message:'Person with username of ' + params.username + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else
			{
				throw new OwfException(message:'You are not authorized to modify dashboards for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}else if(returnSelf == true){
			person = accountService.getLoggedInUser()
		}
		return person
	}

	private def convertStringToBool(stringToConvert) {
		//Let's be lenient with what we accept.
		if (stringToConvert instanceof java.lang.Boolean)
		{
			return stringToConvert
		}

		(stringToConvert == "true" || stringToConvert == "on") ? true : false
	}

	private def findByGuidForUser(guid,userid) {
		Map newParams = new HashMap()
		newParams.personId = userid
		def person = getPersonByParams(newParams)
		def dashboard = Dashboard.findByGuidAndUser(guid, person)
		return dashboard
	}

	//  TODO: refactor this out when we have time.  I don't like this logic here
	//      potentially a createListCriteriaFromJSONParams or something in the Service
	//      or a static translation of json param to database fields in the domain
	private def convertJsonParamToDomainField(jsonParam) {
		switch(jsonParam) {
			case 'name':
				return 'name'
			case 'guid':
				return 'guid'
			case 'isdefault':
				return 'isdefault'
			case 'dashboardPosition':
				return 'dashboardPosition'
			//	        case 'state':
			//	        	return 'guid'
			case 'user.userId':
				return 'user'
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class Preference has not been mapped in PreferenceService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: Preference",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}

	private def findDashboardsByUserAndDashBoardName(username, dashboardname) {
		def person = Person.createCriteria().list {
			and {
				eq('username', username)
				dashboards { eq('name', dashboardname) }
			}
			cache(true)
		}
		return person.dashboards
	}

	private def regenerateAllConfigs( dashboard ) {

		def uniqueIdRegex = /\"uniqueId\"\:\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/;
		def widgetInstanceIdMatcher = ( dashboard.layoutConfig =~ uniqueIdRegex );

		widgetInstanceIdMatcher.each {
			//println 'REPLACEING .... ' + it[1]
			def oldInstanceId = it[1];
			def newInstanceId = java.util.UUID.randomUUID().toString();

			dashboard.layoutConfig = dashboard.layoutConfig?.replaceAll( oldInstanceId, newInstanceId );
		}

		def jsonLayoutConfig = JSON.parse(dashboard.layoutConfig)

		removeLaunchData(jsonLayoutConfig);

		dashboard.layoutConfig = jsonLayoutConfig.toString()
	}

	private def removeLaunchData(cfg) {

		if(cfg?.items?.size() == 0) {
			cfg.widgets?.each {
				//println 'deleteing launchData' + it.launchData;
				it.launchData = null;
			}
		}
		else {
			cfg?.items?.each { removeLaunchData(it); }
		}
	}

	//Generates a map between the universalName and wigetGuid of all widgets in a dashboard's layoutConfig
	private def getUniversalNameToGuidMap(universalNameToGuidMap, layoutConfig) {
		def widgets = layoutConfig.widgets
		for(def i = 0; i < widgets?.size(); i++) {
			if(widgets[i].universalName instanceof String) {
				universalNameToGuidMap[widgets[i].universalName] = widgets[i].widgetGuid
			}
		}

		def items = layoutConfig.items
		for(def i = 0; i < items?.size(); i++) {
			//Nested layoutConfig inside this pane, repeat the loop with it
			getUniversalNameToGuidMap(universalNameToGuidMap, items[i])
		}
	}

	private def getMaxDashboardPosition(person) {
		def queryReturn
		if(!person) {
			queryReturn = Dashboard.executeQuery("SELECT MAX(d.dashboardPosition) AS retVal FROM Dashboard d WHERE d.user = null")
		}
		else {
			queryReturn = Dashboard.executeQuery("SELECT MAX(d.dashboardPosition) AS retVal FROM Dashboard d WHERE d.user = ?", [person])
		}
		return (queryReturn[0] != null)? queryReturn[0] : -1
	}

	// NOTE:  The following methods are new and relate to solving a performance
	// problem when assigning a shared group dashboard to a group resource that
	// has many users (think built-in groups).  The result is a large number of
	// clones when only one of those is germane for any given user.

	/**
	 * Replacement for the pathological call into the domain mapping service to
	 * find cloned dashboards based off a group dashboard which the user has
	 * as a result of group membership.
	 *
	 * @param user the user object (typically, the logged in user).
	 * @param dmParentId the parent dashboard id (which the user has possibly
	 * cloned)
	 *
	 * @return all matching domain mappings.
	 */
	private def getUserPrivateDashboards(user, dmParentId) {

			// This problem is somewhat cumbersome because there's no direct
			// linkage between dashboard and domain mapping, from either
			// direction (there can't be).  This lends itself to several
			// possible answers:
			//
			//	1)  get all the dashboards which belong to the user (just ID)
			//		and filter domain mappins using an "in" clause to get just
			//		those which descend from a group dashboard, then re-query
			// 		to pull just those dashboards are descendants (at least
			//		two queries in there, possibly three).
			//	2)	go with the cross-product (should be small) and then walk
			//		to filter out everything except the dashboard instances.
			//
			// Picking the latter because it's fewer queries, even though we
			// throw away some stuff.

			// This produces object array, first index has a domain mapping,
			// dashboard
			def colQuery = DomainMapping.findAll("\
							FROM DomainMapping dm, Dashboard d \
							WHERE \
								dm.destId = ? AND \
								dm.relationshipType = ? AND \
								dm.srcId = d.id AND \
								d.user = ?", [dmParentId, RelationshipType.cloneOf.strVal, user])

			def col = []
			colQuery[0].each {
				if (it instanceof Dashboard) {
					col.add(it)
				}
			}

			return col
	}
}
