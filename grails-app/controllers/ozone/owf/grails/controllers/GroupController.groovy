package ozone.owf.grails.controllers

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes

class GroupController extends BaseOwfRestController {

	def groupService
	def accountService

	def list = { GroupListCommand cmd ->
		def statusCode
		def jsonResult

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to see a list of groups in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'List command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def result
			if (cmd.widget_id != null || cmd.dashboard_id != null) {
				result = groupService.listGroupsForWidgetOrDashboard(cmd)
			}
			else if (cmd.user_id != null) {
				result = groupService.listGroupsForSingleUser(cmd)
			}
			else if (cmd.id != null) {
				result = groupService.listSingleGroup(cmd)
			}
			else {
				result = groupService.list(cmd)
			}

			if (result.size() > 0) {
				def userCounts = groupService.countPeopleInGroups(result*.id)
				def widgetCounts = groupService.countGroupOwnedWidgets(result*.id)

				def processedResults = result.collect { g ->
					g.toServiceModel([totalUsers: userCounts[g.id], totalWidgets: widgetCounts[g.id]])
				}

				jsonResult = [data: processedResults, results: result.totalCount] as JSON
			}
			else {
				jsonResult = [data: [], results: 0] as JSON
			}
			statusCode = 200
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def createOrUpdate = { GroupModifyCommand cmd ->
		def jsonResult

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to edit groups in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Modify command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			// If there's a group ID, then we're updating a related collection of the group (so, adding users, widgets, dashboards or
			// removing the same).  Interpret the JSON payload, then, as containing the identifier (it actually has a lot more) of the
			// target collection entities.
			if (cmd.group_id) {
				def pData = JSON.parse(cmd.data)
				def resultsOfUpdate
				switch (cmd.tab) {
					case 'users':
						resultsOfUpdate = groupService.createOrUpdateUsersForGroup(pData, cmd.group_id, cmd.update_action == 'add')
						break

					case 'widgets':
						resultsOfUpdate = groupService.createOrUpdateWidgetsForGroup(pData, cmd.group_id, cmd.update_action == 'add')
						break

					case 'dashboards':
						resultsOfUpdate = groupService.createOrUpdateDashboardsForGroup(pData, cmd.group_id, cmd.update_action == 'add')
						break

					// Defensive: not strictly required because the validation would have caught this, but in case we let something by....
					default:
						throw new OwfException(message: "Unexpected data type for group collection: ${cmd.tab}", OwfExceptionTypes.InputValidation)
				}
				def processedResults = resultsOfUpdate.collect { serviceModelService.createServiceModel(it) }
				jsonResult = [msg: [success: true, data: processedResults] as JSON, status: 200]
			}
			// Otherwise, we're operating directly on the group(s) and the JSON payload in the "data" object should be interpreted as the
			// desired state of the group(s).
			else {
				def rawResults = []
				def pData = JSON.parse(cmd.data)
				if (!pData instanceof JSONArray) {
					pData = new JSONArray(pData)
				}
				pData.each {
					rawResults << groupService.createOrUpdateSpecificGroup(it)
				}

				def userCounts = groupService.countPeopleInGroups(rawResults*.id)
				def widgetCounts = groupService.countGroupOwnedWidgets(rawResults*.id)

				def processedResults = rawResults.collect { g ->
					g.toServiceModel([totalUsers: userCounts[g.id], totalWidgets: widgetCounts[g.id]])
				}
				jsonResult = [msg: [success: true, data: processedResults] as JSON, status: 200]
			}
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def delete = { GroupDeleteCommand cmd ->
		def jsonResult

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to delete groups in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Delete command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def pData = JSON.parse(cmd.data)
			Set<Long> groupIds = pData.collect { it.id as long }
			def result = groupService.deleteGroups(groupIds)

			jsonResult = [msg: [success: true, data: result] as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	def copyDashboard = { GroupCopyDashboardCommand cmd ->
		def jsonResult

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to delete groups in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Delete command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def pDashboardData = JSON.parse(cmd.dashboards)
			def pGroupData = JSON.parse(cmd.groups)

			def rawResults = groupService.copyDashboardsToGroup(pDashboardData, pGroupData, cmd.isGroupDashboard)
			def processedResults = rawResults.collect { d -> d.toServiceModel()	}

			jsonResult = [msg: [success: true, msg: processedResults] as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}
}

class GroupCopyDashboardCommand {
	def groupService
	def dashboardService

	String dashboards
	String groups
	Boolean isGroupDashboard

	static constraints = {
		dashboards blank: false, validator: { val, obj ->
			obj.dashboardService.validateJsonDataAsDashboards(val)
		}
		groups blank: false, validator: { val, obj ->
			obj.groupService.validateJsonDataAsGroups(val)
		}
	}
}

class GroupDeleteCommand {
	def groupService

	String data

	static constraints = {
		data blank: false, validator: { val, obj ->
			obj.groupService.validateJsonDataAsGroups(val)
		}
	}
}

class GroupModifyCommand {
	def groupService
	def dashboardService

	// Always used, always JSON, but the meaning changes.  If it's a create, the id will be zero.
	String data

	// When working on group relationships, the "group_id", "tab" and "update_action" attributes will be set.
	// The tab will tell the relationship (user, dashboard, widget) and the update_action
	// will indicate whether we're adding or subtracting.  The JSON data will contain the
	// attributes of the related element (e.g. the user we're adding/removing).
	Long group_id
	String tab
	String update_action

	static constraints = {
		data blank: false, validator: { val, obj ->
			if (obj.tab == null) {
				obj.groupService.validateJsonDataAsGroups(val)
			}
			else if (obj.tab == 'dashboards') {
				obj.dashboardService.validateJsonDataAsDashboards(val)
			}
		}
		group_id nullable: true, validator: { val, obj ->
			(val != null && obj.tab != null && obj.update_action != null) || val == null
		}
		tab nullable: true, blank: false, inList: ['users', 'widgets', 'dashboards'], validator: { val, obj ->
			(val != null && obj.group_id != null && obj.update_action != null) || val == null
		}
		update_action nullable: true, blank: false, inList: ['add', 'remove'], validator: { val, obj ->
			(val != null && obj.tab != null && obj.group_id != null) || val == null
		}
	}
}

class GroupListCommand {
	// Used when this is called from the Groups widget
	String filters
	String filterOperator
	Integer offset
	Integer max
	String sort
	String order

	// Used when this is called from the Group Editor widget
	Long id
	// Used when this is called from the User Editor widget
	Long user_id
	// Used when this is called from the Dashboard Editor widget
	String dashboard_id
	// Used when this is called from the Widget Editor widget
	String widget_id

	static constraints = {
		max nullable: false, min: 1
		offset nullable: false, min: 0
		sort nullable: false, blank: false
		order nullable: false, blank: false, inList: ['ASC', 'DESC']
		filterOperator nullable: true, blank: false, inList: ['AND', 'OR']

		// The numerics are validated here
		id nullable: true, validator: { val, obj ->
			(val != null && obj.dashboard_id == null && obj.widget_id == null && obj.user_id == null) || val == null
		}
		user_id nullable: true, validator: { val, obj ->
			(val != null && obj.dashboard_id == null && obj.widget_id == null && obj.id == null) || val == null
		}

		// These are the GUID-based identifiers
		widget_id nullable: true, blank: false,
				matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/,
				validator: { val, obj ->
					(val != null && obj.dashboard_id == null && obj.user_id == null && obj.id == null) || val == null
				}
		dashboard_id nullable: true, blank: false,
				matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/,
				validator: { val, obj ->
					(val != null && obj.widget_id == null && obj.user_id == null && obj.id == null) || val == null
				}
	}
}