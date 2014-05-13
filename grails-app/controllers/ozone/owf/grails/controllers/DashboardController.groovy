package ozone.owf.grails.controllers

import grails.converters.JSON
import grails.gorm.DetachedCriteria

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.Person
import ozone.owf.grails.utils.DataMapping

class DashboardController extends BaseOwfRestController {

	def accountService
	def dashboardService
	def groupService
	def modelName = 'dashboard'

	def bulkDeleteAndUpdate = { DashboardPrefBulkDeleteUpdateCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()
		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Bulk management command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def dfltDb = null
			JSON.parse(cmd.viewsToUpdate).eachWithIndex { dbJson, i ->
				Dashboard dashboard = Dashboard.findByGuid(dbJson.guid)
				if (dashboard == null) {
					throw new OwfException(message: 'Dashboard ' + dbJson.guid + ' is invalid.', exceptionType: OwfExceptionTypes.NotFound)
				}
				if (dashboard.user != liU) {
					throw new OwfException(message: 'Dashboard user of ' + dbJson.guid + ' does not match login user.', exceptionType: OwfExceptionTypes.Authorization)
				}

				if (i == 0) {
					dfltDb = dbJson.guid
				}
				dashboard.dashboardPosition = (i + 1)
				dashboard.save()
			}

			dashboardService.setUserDefault(liU, dfltDb)
			statusCode = 200
			jsonResult = getJsonResult([success:true], modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkDeleteAndUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def create = { DashboardPrefCreateOrUpdateCommand cmd ->
		def statusCode
		def jsonResult
		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Create or update command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			// The map is what marries the data coming in from the front-end onto the actual domain
			// class names we'll use.  Allows us to tighten down the service interface into something
			// that's directly mappable to domain, akin to "domain.properties = <JSONObject>."
			def db = dashboardService.createNewDashboard(new JSONObject([
						name: cmd.name, guid: cmd.guid, description: cmd.description,
						isdefault: cmd.isdefault, layoutConfig: cmd.layoutConfig, locked: cmd.locked]), accountService.getLoggedInUser())

			def result = [success: true, dashboard: db]
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

	def createOrUpdate = { DashboardCreateOrUpdateCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()
		try {
			if (cmd.adminEnabled && !accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to manage dashboards for another user.', exceptionType: OwfExceptionTypes.Authorization)
			} else if (liU.id != cmd.user_id && !accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to manage dashboards for another user.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Create or update command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def results = []
			def defaultGuid = null

			if (!cmd.tab) {
				// If we're an admin, impersonate the desired user
				if (cmd.adminEnabled && cmd.user_id) {
					liU = Person.read(cmd.user_id)
				}

				cmd.jsonArray.each {
					def serviceResults

					if (Dashboard.findByGuid(it.guid) == null) {
						// Similar to the above create end point, we map the supplied data into a JSONObject
						// to constrain the service API and provide an easy mapping onto the backing domain.
						// The new wrinkle here is that the command object could be creating a group dashboard
						// or a user-level dashboard.  The presence of the isGroupDashboard flag is what
						// determines -- the isGroupDashboard and user_id attributes are mutually exclusive.
						// The only other change is we always regenerate UUIDs in this case, not sure why,
						// but we include that to preserve compatibility with older baselines.
						if (!cmd.isGroupDashboard && it.isdefault && !defaultGuid) {
							defaultGuid = it.guid
						}

						def d = dashboardService.createNewDashboard(new JSONObject([
									name: it.name, guid: it.guid, description: it.description,
									isdefault: it.isdefault, layoutConfig: it.layoutConfig,
									locked: it.locked]), cmd.isGroupDashboard ? null : liU, true)

						// For now, this function expects a map like: [success: true, dashboard: dashboard]
						// to be returned.  The createNewDashboard only returns the dashboard object.
						serviceResults = [success: true, dashboard: d]
					}
					else {
						def db = Dashboard.findByGuid(it.guid)
						if (!db) {
							throw new OwfException(message: "The requested dashboard, guid ${it.guid} was not found.", exceptionType: OwfExceptionTypes.NotFound)
						}
						if (!cmd.adminEnabled && db.user != liU) {
							throw new OwfException(message: 'You are not authorized to update this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
						}

						def args = new JSONObject(name: it.name, isdefault: it.isdefault, locked: it.locked, description: it.description,
								layoutConfig: it.layoutConfig?.toString(), isGroupDashboard: cmd.isGroupDashboard,
								dashboardPosition: cmd.isGroupDashboard ? 1 : it.dashboardPosition)
						if (!cmd.isGroupDashboard && it.isdefault && !defaultGuid) {
							defaultGuid = it.guid
						}

						dashboardService.updateDashboard(args, db, true)
						serviceResults = [success: true, dashboard: db]
					}

					results << serviceResults.dashboard.toServiceModel([isGroupDashboard: cmd.isGroupDashboard?.toString()?.toBoolean()])
				}

				if (!cmd.isGroupDashboard) {
					if (defaultGuid) {
						dashboardService.setUserDefault(liU, defaultGuid)
					}
					else {
						dashboardService.setUserDefault(liU)
					}
				}
			}
			else {
				// Unusual case, which appears to be reached only when editing a group dashboard and attempting to add a group
				// to said dashboard.  From a functional perspective, this is also addressed by editing a group and adding
				// dashboards to that group.  So, we're going to delegate over to that code branch.
				def dashboardJson = new JSONArray([['guid': cmd.dashboard_id]])

				def rawResults = []
				cmd.jsonArray.each { grpJson ->
					def db = groupService.createOrUpdateDashboardsForGroup(dashboardJson, grpJson.id, cmd.update_action == 'add')
					rawResults.addAll(db)
				}
				results = rawResults.collect { dashboard ->
					dashboard.toServiceModel()
				}
			}
			statusCode = 200
			jsonResult = [success: true, data: results] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during createOrUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def delete = { DashboardDeleteCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()
		try {
			if (cmd.missingDashboards) {
				throw new OwfException(message: 'One or more dashboards was not found.', exceptionType: OwfExceptionTypes.NotFound)
			}

			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Delete command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			if (cmd.adminEnabled && !accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to delete dashboards for another user.', exceptionType: OwfExceptionTypes.Authorization)
			} else if (!accountService.getLoggedInUserIsAdmin()) {
				// Could be demolishing a bunch of dashboards (likely not, but possible).  Before proceeding on any, make sure we
				// can demolish all.
				def allowed = true
				cmd.dashboardsToDelete.each { dbGuid ->
					def dbDelete = Dashboard.findByGuid(dbGuid)
					allowed = allowed && (dbDelete.user == liU)
				}
				if (!allowed) {
					throw new OwfException(message: 'You are not authorized to delete dashboards for another user.', exceptionType: OwfExceptionTypes.Authorization)
				}
			}

			if (!cmd.adminEnabled) {
				if (dashboardService.isUserMappedToMoreThanOneDashboard(liU)) {
					dashboardService.deleteUserDashboardsByGuids(cmd.dashboardsToDelete)
				}
				else {
					throw new OwfException(message: 'All users must have at least one dashboard.', exceptionType: OwfExceptionTypes.LimitConditionExceeded)
				}
			}
			else {
				if (cmd.user_id) {
					def user = Person.read(cmd.user_id)
					if (user) {
						if (dashboardService.isUserMappedToMoreThanOneDashboard(user, cmd.dashboardsToDelete)) {
							dashboardService.deleteUserDashboardsByGuids(cmd.dashboardsToDelete)
						}
						else {
							throw new OwfException(message: 'All users must have at least one dashboard.', exceptionType: OwfExceptionTypes.LimitConditionExceeded)
						}
					}
					else {
						throw new OwfException(message: 'Could not locate user in the database.', exceptionType: OwfExceptionTypes.NotFound)
					}
				} else if (cmd.isGroupDashboard) {
					// We need to verify that, for each dashboard, we won't be removing a user's last dashboard (delete the dashboard,
					// plus the user-level clones, plus all the domain mappings).  Block on any delete that would remove the user's last and return
					// a message like "Can't delete dashboard (name) because it's the last remaining dashboard for user (names)"
					def errors = []
					cmd.dashboardsToDelete.each { dashboardToDelete ->
						def userIdsToCheck = dashboardService.findPersonIdsWithZeroDashboardsAfterDelete(dashboardToDelete)

						if (userIdsToCheck.isEmpty()) {
							dashboardService.deleteGroupDashboardByGuid(dashboardToDelete)
						}
						else {
							errors << dashboardToDelete
						}
					}
					if (!errors.isEmpty()) {
						def strVal = errors.join(', ')
						throw new OwfException(message: "Cannot delete dashboards with GUID(s): ${strVal} as doing so would leave some users without a dashboard.", exceptionType: OwfExceptionTypes.LimitConditionExceeded)
					}
				}
			}

			jsonResult = [success: true, data: []] as JSON
			statusCode = 200
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during delete: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def list = { DashboardListCommand cmd ->
		def statusCode
		def jsonResult

		try {
			if (cmd.adminEnabled && !accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to see a list of dashboards in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'List command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			// Note: it appears that this overall API is only used by administrators inside the admin widgets.  Nevertheless, because there
			// is a possibility it could be used in other ways, we keep a fallback path for an ordinary user to retrieve a list
			// of his/her own dashboards.
			Map<DataMapping, Object> mpDashboards
			def result = [:]
			if (cmd.adminEnabled) {
				if (cmd.id) {
					Dashboard d = dashboardService.listSingleDashboardByGuid(cmd.id)

					result.dashboardList = [d]
					result.count = 1
					result.success = true
				}
				else if (cmd.isGroupDashboard) {
					mpDashboards = dashboardService.listGroupDashboards(cmd)
					def lstDashboards = mpDashboards.get(DataMapping.DATA)
					Map<Long, Long> dashboardGroups = dashboardService.countGroupsAssociatedWithDashboards(lstDashboards*.id)
					def smLstDashboards = lstDashboards.collect { dashboard ->
						dashboard.toServiceModel([groupCount: dashboardGroups[dashboard.id] ?: 0])
					}

					result.dashboardList = smLstDashboards
					result.count = mpDashboards.get(DataMapping.RECORDCOUNT)
					result.success = true
				}
				else if (cmd.group_id && cmd.tab == 'dashboards') {
					mpDashboards = dashboardService.listDashboardsForSingleGroup(cmd)
				}
				else if (cmd.user_id && cmd.tab == 'dashboards') {
					def person = Person.read(cmd.user_id)
					mpDashboards = dashboardService.listDashboardsForSingleUserPagedAndFiltered(cmd, person)
				}
			}
			else {
				def person = accountService.getLoggedInUser()
				mpDashboards = dashboardService.listDashboardsForSingleUserPagedAndFiltered(cmd, person)
			}

			if (!result.count) {
				result.dashboardList = mpDashboards.get(DataMapping.DATA)
				result.count = mpDashboards.get(DataMapping.RECORDCOUNT)
				result.success = true
			}

			statusCode = 200
			jsonResult = [success: result.success, results: result.count, data: result.dashboardList] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def restore = { DashboardRestoreCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()
		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Restore command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def dbRestore = Dashboard.findByGuid(cmd.guid)
			if (dbRestore == null) {
				throw new OwfException(message: 'Dashboard ' + cmd.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}

			// Because the command object is so sparse, reversing the order here of checking for administrative privileges.
			if (!accountService.getLoggedInUserIsAdmin()) {
				if (liU != dbRestore.user) {
					throw new OwfException(message: 'You are not authorized to restore dashboards for another user.', exceptionType: OwfExceptionTypes.Authorization)
				}
			}

			dbRestore = dashboardService.restore(dbRestore, cmd.isdefault)
			Dashboard dbParent = dashboardService.findParentDashboard(dbRestore.id)
			def gMap = [:]
			if (dbParent) {
				gMap = dashboardService.countGroupsAssociatedWithDashboards([dbParent.id])
			}
			def gCount = 0
			if (!gMap.keySet().isEmpty()) {
				gCount = gMap.get(dbParent.id)
			}

			dashboardService.setUserDefault(dbRestore.user, dbRestore.guid)

			def result = [success: true, data: [ dbRestore.toServiceModel([groupCount: gCount]) ]]
			statusCode = 200
			jsonResult = result as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during restore: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def update = { DashboardPrefCreateOrUpdateCommand cmd ->
		def statusCode
		def jsonResult
		def liU = accountService.getLoggedInUser()
		try {
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'Create or update command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def db = Dashboard.findByGuid(cmd.guid)
			if (!db) {
				throw new OwfException(message: "The requested dashboard, guid ${cmd.guid} was not found.", exceptionType: OwfExceptionTypes.NotFound)
			}
			if (db.user != liU) {
				throw new OwfException(message: 'You are not authorized to update this dashboard.', exceptionType: OwfExceptionTypes.Authorization)
			}

			def jsonNewData = new JSONObject(description: cmd.description, isdefault: cmd.isdefault, layoutConfig: cmd.layoutConfig,
					locked: cmd.locked, name: cmd.name, state: cmd.state)
			dashboardService.updateDashboard(jsonNewData, db)

			if (cmd.isdefault) {
				dashboardService.setUserDefault(db.user, db.guid)
			}
			else {
				dashboardService.setUserDefault(db.user)
			}

			def result = [success: true, dashboard: db]
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

}

class DashboardCreateOrUpdateCommand {
	boolean adminEnabled
	String dashboard_id
	def dashboardService
	String data
	boolean isGroupDashboard
	JSONArray jsonArray
	String tab
	String update_action
	Long user_id

	static constraints = {
		isGroupDashboard validator: { val, obj ->
			if (val == true) { obj.user_id == null }
		}
		user_id nullable: true, min: 1l
		dashboard_id nullable: true, blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/, validator: { val, obj ->
			if (val) { obj.tab && obj.update_action }
		}
		tab nullable: true, blank: false, inList: ['groups'], validator: { val, obj ->
			if (val) { obj.dashboard_id && obj.update_action }
		}
		update_action nullable: true, blank: false, inList: ['add', 'remove'], validator: { val, obj ->
			if (val) { obj.tab && obj.dashboard_id }
		}
		data blank: false, validator: { val, obj ->
			if (!obj.tab) {
				// The supplied data is dashboards.
				def isDashboards = obj.dashboardService.validateJsonDataAsDashboards(val)
				if (!isDashboards) {
					return false
				}
				obj.jsonArray = JSON.parse(val)
				return true
			}
			else {
				// The supplied data is groups.
				def pData
				try {
					pData = JSON.parse(val)
				} catch (all) {
					return false
				}
				if (!pData instanceof JSONArray) return false

				def retVal = true
				pData.each { grpJson ->
					if (!grpJson.id) { retVal = false }
				}
				obj.jsonArray = pData
				return retVal
			}
		}
	}
}

class DashboardDeleteCommand {
	boolean adminEnabled
	Set<String> dashboardsToDelete = [] as Set
	String data
	boolean isGroupDashboard
	boolean isStackDashboard
	boolean missingDashboards = true
	String tab
	String update_action
	Long user_id

	static constraints = {
		data blank: false, validator: { val, obj ->
			def dashboards
			try {
				dashboards = JSON.parse(val)
			} catch (all) {
				return false
			}
			obj.dashboardsToDelete.addAll(dashboards*.guid)

			if (obj.dashboardsToDelete.size() > 0) {
				// Purely defensive here, should never happen unless someone is poking.
				def dListCriteria = new DetachedCriteria(Dashboard).build {
					inList 'guid', obj.dashboardsToDelete
				}
				if (dListCriteria.list().size() == obj.dashboardsToDelete.size()) {
					obj.missingDashboards = false
				}
			}
			return !obj.missingDashboards
		}
		tab nullable: true, blank: false, inList: ['dashboards']
		update_action nullable: true, blank: false, inList: ['remove']
		user_id nullable: true, min: 1l
	}
}

class DashboardListCommand {
	Boolean adminEnabled
	String filterOperator
	String filters
	Long group_id
	String id
	boolean isGroupDashboard
	Integer max
	Integer offset
	String order
	String sort
	String tab

	Long user_id

	static constraints = {
		adminEnabled nullable: true, validator: { val, obj ->
			if (val != null) {
				obj.max != null && obj.offset != null && obj.order != null && obj.sort != null &&
						(obj.id != null || obj.isGroupDashboard || obj.group_id != null || obj.user_id != null)
			}
		}
		max nullable: true, min: 1, max: 50, validator: { val, obj ->
			if (val != null) { obj.offset != null }
		}
		offset nullable: true, min: 0, validator: { val, obj ->
			if (val != null) { obj.max != null }
		}
		order nullable: true, blank: false, inList: ['ASC', 'DESC'], validator: { val, obj ->
			if (val != null) { obj.sort != null }
		}
		sort nullable: true, blank: false, inList: ['dashboardPosition', 'name'], validator: { val, obj ->
			if (val != null) { obj.order != null }
		}
		id nullable: true, blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/, validator: { val, obj ->
			if (val != null) { obj.filterOperator == null && obj.filters == null }
		}
		tab nullable: true, blank: false, inList: ['dashboards']
		group_id nullable: true, min: 1l, validator: { val, obj -> if (val != null) { obj.tab != null && !obj.isGroupDashboard } }
		user_id nullable: true, min: 1l, validator: { val, obj -> if (val != null) { obj.tab != null && !obj.isGroupDashboard } }
		filterOperator nullable: true, blank: false, inList: ['OR'], validator: { val, obj ->
			if (val != null) { obj.filters != null }
		}
		filters nullable: true, blank: false, validator: { val, obj ->
			def retVal = true
			if (val != null) {
				retVal = retVal && (obj.filterOperator != null)
				try {
					def pData = JSON.parse(val)
					if (pData instanceof JSONArray && pData.isEmpty()) { retVal = false }
					pData.each {
						retVal = retVal && (it.filterField && it.filterValue)
					}
				} catch (Exception e) {
					retVal = false
				}
			}
			retVal
		}
	}
}

class DashboardPrefBulkDeleteUpdateCommand {
	boolean updateOrder
	String viewGuidsToDelete
	String viewsToUpdate

	static constraints = {
		viewGuidsToDelete blank: false
		viewsToUpdate blank: false, validator: { val, obj ->
			try {
				def pData = JSON.parse(val)
				def retVal = true
				pData.each {
					retVal = retVal && (it.guid != null && it.isdefault != null && it.name != null)
					retVal = retVal && (it.guid.toString().matches(/^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/))
				}
				retVal
			} catch (Exception e) {
				return false
			}
		}
	}
}

class DashboardPrefCreateOrUpdateCommand {
	boolean bypassLayoutRearrange
	String description
	String guid
	boolean isdefault
	String layoutConfig
	boolean locked
	String name
	String state

	static constraints = {
		guid blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/
		description nullable: true, blank: true
		layoutConfig blank: false
		state blank: false
		name blank: false
	}
}

class DashboardRestoreCommand {
	String guid
	Boolean isdefault

	static constraints = {
		guid blank: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/
		isdefault nullable: true
	}
}
