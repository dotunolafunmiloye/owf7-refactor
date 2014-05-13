package ozone.owf.grails.services

import grails.converters.JSON
import grails.gorm.DetachedCriteria
import groovy.sql.Sql

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.controllers.DashboardListCommand
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.utils.DataMapping

// FIXME: Eliminate the dependency on BaseService.  It doesn't add value as the tests it performs
// should be handled by the controller.
class DashboardService extends BaseService {

	final def uniqueIdRegex = /"uniqueId"\s*:\s*"[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}"/
	final def guidRegex = /[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}/

	def sessionFactory
	def domainMappingService
	def serviceModelService

	// FIXME: This needs to be pretty much removed for the principal work, since there's no concept of
	// user or group, only the principal.  The whole domain mapping could be removed and replaced, if
	// needed, by a reference in a dashboard instance to a "parent" to which the dashboard should be
	// aligned.  MAYBE.  BIG MAYBE....  Not sure that's really required since group dashboards will not
	// have parents and users are free to mess around with their own dashboards.
	private def processGroupDashboards(groups, user) {
		def privateGroupDashboardToGroupsMap = [:]

		def maxPosition = 0
		if (user != null) {
			maxPosition = getMaxDashboardPosition(user)
		}
		if (maxPosition < 0) maxPosition = 0

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

						def privateDash = deepClone(args, user.id)

						//save mapping
						domainMappingService.createMapping(privateDash.dashboard, RelationshipType.cloneOf, [id: dm.destId, TYPE: dm.destType])

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
			groupDashboardToGroupsMap[mapKey] = [] as Set
		}
		Group group = Group.get(groupId)
		if (group != null) {
			groupDashboardToGroupsMap[mapKey] << group
		}
	}

	/**
	 * Creates a re-usable detached criteria for any filter box entries from the user (user
	 * types a value into the "Search" box on an admin widget).
	 *
	 * @param dlc the input data from the user.
	 *
	 * @return a detached criteria for the user-supplied filter data.
	 */
	private DetachedCriteria createDashboardFilterCriteria(DashboardListCommand dlc) {
		def createFilterCriteriaForDashboards = { filters ->
			JSON.parse(filters).each {
				ilike it.filterField, "%${it.filterValue}%"
			}
		}

		def criteria = new DetachedCriteria(Dashboard).build {
			createFilterCriteriaForDashboards.delegate = delegate
			if (dlc.filterOperator == 'OR') {
				or {
					createFilterCriteriaForDashboards(dlc.filters)
				}
			}
			else {
				createFilterCriteriaForDashboards(dlc.filters)
			}
		}

		return criteria
	}

	public void deleteUserDashboardsByGuids(Set<String> guids) {
		def dListCriteria = new DetachedCriteria(Dashboard).build {
			inList 'guid', guids
			projections {
				property 'id'
			}
		}

		def dmCriteria = new DetachedCriteria(DomainMapping).build {
			inList 'srcId', dListCriteria.list()
			eq 'srcType', Dashboard.TYPE
			eq 'relationshipType', RelationshipType.cloneOf.strVal
		}
		dmCriteria.deleteAll()

		def dDeleteCriteria = new DetachedCriteria(Dashboard).build {
			inList 'guid', guids
		}
		dDeleteCriteria.deleteAll()
	}

	public void deleteGroupDashboardByGuid(String guid) {
		// Get the ID of the matching dashboard.
		def dListCriteria = new DetachedCriteria(Dashboard).build {
			eq 'guid', guid
			projections {
				property 'id'
			}
		}
		def dashboardIds = dListCriteria.list()

		// Now, go demolish all domain mappings of type "cloneOf" for the
		// dashboard.
		def dmListClonesCriteria = new DetachedCriteria(DomainMapping).build {
			inList 'destId', dashboardIds
			eq 'destType', Dashboard.TYPE
			eq 'relationshipType', RelationshipType.cloneOf.strVal
		}
		def cloneDashboardIds = dmListClonesCriteria.list()*.srcId as Set
		dmListClonesCriteria.deleteAll()

		// Kill off the domain mappings of type "own" for the dashboard.
		def dmDeleteOwnsCriteria = new DetachedCriteria(DomainMapping).build {
			inList 'destId', dashboardIds
			eq 'destType', Dashboard.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
		}
		dmDeleteOwnsCriteria.deleteAll()

		// Last, remove the actual dashboard, plus all the clones of it,
		// from the dashboard table.  There may not be any clones, but we'd like to
		// get them all in one sweep if there are.
		def dDeleteCriteria = new DetachedCriteria(Dashboard).build {
			if (!cloneDashboardIds.isEmpty()) {
				or {
					inList 'id', dashboardIds
					inList 'id', cloneDashboardIds
				}
			}
			else {
				inList 'id', dashboardIds
			}
		}
		dDeleteCriteria.deleteAll()
	}

	/**
	 * Determines if the given user has enough "remaining" dashboards following a proposed delete
	 * to allow the delete to proceed.  Typically, you'd call this without the second parameter if
	 * you're deleting just one dashboard and you want to know there's at least two belonging to the
	 * user.  Otherwise, assume an administrator is removing a whole bunch (the second parameter) and
	 * make sure they haven't gone a little too delete-happy.
	 *
	 * @param p the user.
	 * @param dashboardsToDelete (Optional) the list of dashboard (GUIDs) an administrator would like to
	 * delete for the user.
	 *
	 * @return true if the delete can proceed, false otherwise.
	 */
	public boolean isUserMappedToMoreThanOneDashboard(Person p, Set<String> dashboardsToDelete = []) {
		def base = new DetachedCriteria(Dashboard).build {}
		def neededCount = 2

		if (!dashboardsToDelete.isEmpty()) {
			neededCount = 1
			base = new DetachedCriteria(Dashboard).build {
				not {
					inList 'guid', dashboardsToDelete
				}
			}
		}
		def criteria = base.build {
			eq 'user', p
		}

		// By testing for 2+ dashboards, we know we can safely delete any one dashboard.  If we
		// know the dashboards we desire to delete, we can then just test for 1 remaining and
		// safely delete all desired.
		return criteria.count() >= neededCount
	}

	/**
	 * Find all the user IDs which would have zero dashboards were it not for the
	 * one dashboard we'd like to delete.
	 *
	 * @param guid the dashboard we'd like to delete.
	 *
	 * @return a list of longs (record IDs).
	 */
	public List<Long> findPersonIdsWithZeroDashboardsAfterDelete(String guid) {
		def sql = new Sql(sessionFactory.currentSession.connection())
		def usersWithoutDashboards = sql.rows("""
			SELECT has_db.user_id FROM
				(SELECT user_id, count(1) AS has_selected_dashboard FROM
					dashboard d, domain_mapping dm
				WHERE
					dm.dest_id = (SELECT id FROM dashboard WHERE guid = ?) AND
					dm.dest_type = ? AND
					dm.relationship_type = ? AND
					dm.src_type = ? AND
					dm.src_id = d.id
				GROUP BY user_id) AS has_db,
				(SELECT user_id, count(1) AS total_number_dashboard FROM dashboard
				GROUP BY user_id HAVING total_number_dashboard = 1) AS count_db
			WHERE
				has_db.user_id = count_db.user_id""", [guid, Dashboard.TYPE, RelationshipType.cloneOf.strVal, Dashboard.TYPE])
		return usersWithoutDashboards*.user_id
	}

	/**
	 * Count how many groups directly own a given dashboard, grouping by dashboard.
	 *
	 * @param dashboardIds the dashboards to consider.
	 *
	 * @return a mapping of dashboard id -> count of groups which own it.
	 */
	public Map<Long, Long> countGroupsAssociatedWithDashboards(dashboardIds) {
		def results = DomainMapping.createCriteria().list {
			inList 'destId', dashboardIds
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', Dashboard.TYPE
			projections {
				groupProperty('destId')
				rowCount()
			}
		}

		def mp = [:]
		results.each {
			mp.put(it[0], it[1])
		}
		mp
	}

	/**
	 * Count how many groups indirectly own a given "child" dashboard, grouped by the parent dashboard IDs.
	 *
	 * @param childDashboardId the child dashboard to consider.
	 *
	 * @return a mapping of parent dashboard id -> count of groups which own it.
	 */
	public Dashboard findParentDashboard(childDashboardId) {
		def parent = DomainMapping.createCriteria().get() {
			eq 'srcId', childDashboardId
			eq 'srcType', Dashboard.TYPE
			eq 'relationshipType', RelationshipType.cloneOf.strVal
			eq 'destType', Dashboard.TYPE
			projections {
				property 'destId'
			}
		}

		if (parent) {
			return Dashboard.get(parent)
		}
		return null
	}

	/**
	 * Primarily used by the Group Dashboards admin widget to load dashboards into the UI.
	 *
	 * @param dlc the input user data, fairly sparse.
	 *
	 * @return a list of dashboards that match the (often not) defined search criteria and
	 * paging requirements.
	 */
	public Map<DataMapping, Object> listGroupDashboards(DashboardListCommand dlc) {
		def filter = new DetachedCriteria(Dashboard).build {}
		if (dlc.filters) {
			filter = createDashboardFilterCriteria(dlc)
		}

		// These have to be handled separately because the count() mechanism expects no sorting.
		def critUser = filter.build {
			isNull 'user'
		}

		def criteria = critUser.build {
			order dlc.sort, dlc.order.toLowerCase()
		}

		def results = [:]
		results.put(DataMapping.DATA, criteria.list([offset: dlc.offset, max: dlc.max]))
		results.put(DataMapping.RECORDCOUNT, critUser.count())
		return results
	}

	/**
	 * Locate a single dashboard using its GUID.
	 *
	 * @param guid the GUID to locate.
	 *
	 * @return a dashboard.
	 */
	public Dashboard listSingleDashboardByGuid(String guid) {
		def filter = new DetachedCriteria(Dashboard).build {
			eq 'guid', guid
		}

		return filter.get()
	}

	/**
	 * Similar to another method, but adds additional filtering and paging, rather than returning a raw dump
	 * of every dashboard associated with the user.
	 *
	 * @param dlc the user input from the UI.
	 * @param p the person of interest.
	 *
	 * @return a listing, paged and filtered according to the input, for the supplied user.
	 */
	public Map<DataMapping, Object> listDashboardsForSingleUserPagedAndFiltered(DashboardListCommand dlc, Person p) {
		def filter = new DetachedCriteria(Dashboard).build {}
		if (dlc.filters) {
			filter = createDashboardFilterCriteria(dlc)
		}

		// These have to be handled separately because the count() mechanism expects no sorting.
		def dashUser = filter.build {
			eq 'user', p
		}

		def dash = dashUser.build {
			if (dlc.sort) {
				order dlc.sort, dlc.order.toLowerCase()
			}
		}

		def results = [:]
		results.put(DataMapping.DATA, dash.list([offset: dlc.offset, max: dlc.max]))
		results.put(DataMapping.RECORDCOUNT, dashUser.count())
		return results
	}

	/**
	 * Find dashboards associated with a specific group.
	 *
	 * @param dlc the user input.
	 *
	 * @return a mapping of the paged matching dashboards, plus a total count of matching dashboards.
	 */
	public Map<DataMapping, Object> listDashboardsForSingleGroup(DashboardListCommand dlc) {
		def results = [:]

		def dmDef = new DetachedCriteria(DomainMapping).build {
			eq 'srcType', Group.TYPE
			eq 'srcId', dlc.group_id
			eq 'relationshipType', RelationshipType.owns.toString()
			eq 'destType', Dashboard.TYPE
			projections {
				property 'destId'
			}
		}
		def dbIds = dmDef.list()

		if (!dbIds.isEmpty()) {
			// These have to be handled separately because the count() mechanism expects no sorting.
			def dashIdList = new DetachedCriteria(Dashboard).build {
				inList 'id', dbIds
			}

			def dash = dashIdList.build {
				if (dlc.sort) {
					order dlc.sort, dlc.order.toLowerCase()
				}
			}

			results.put(DataMapping.DATA, dash.list([offset: dlc.offset, max: dlc.max]))
			results.put(DataMapping.RECORDCOUNT, dashIdList.count())
		}
		else {
			results.put(DataMapping.DATA, [])
			results.put(DataMapping.RECORDCOUNT, 0)
		}

		return results
	}

	/**
	 * Find group dashboards which the named user has not already cloned.
	 *
	 * @param p the user.
	 *
	 * @return a set of dashboards (the id column of the Dashboard entity) which should be cloned.
	 */
	public Set<Long> listUnclonedGroupDashboardsForSingleUser(Person p) {
		// This is a particularly nasty query that doesn't really lend itself to a criteria, even detached.
		// Dropping to raw SQL here and running a single query rather than up to four queries, even though
		// the resulting query is much more complicated.  Trading network time against optimizer (hopefully)
		// on the database.
		//
		// And for the sharp-eyed observer, yes the choice of the DISTINCT keyword is a little obsessive
		// given the set semantics of the return.  But if at some point the set were relaxed to more common
		// list, you'd want that DISTINCT to avoid some truly unfortunate and hard-to-explain results.
		def sql = new Sql(sessionFactory.currentSession.connection())
		def dashboardIdsWhichNeedCloning = sql.rows("""
			SELECT DISTINCT dm.dest_id FROM
				domain_mapping dm, owf_group_people gp
			WHERE
				gp.person_id = ? AND
				gp.group_id = dm.src_id AND
				dm.src_type = ? AND
				dm.relationship_type = ? AND
				dm.dest_type = ? AND
				dm.dest_id NOT IN (
					SELECT dmi.dest_id FROM
						dashboard dash, domain_mapping dmi
					WHERE
						dash.user_id = ? AND
						dash.id = dmi.src_id AND
						dmi.src_type = ? AND
						dmi.relationship_type = ? AND
						dmi.dest_type = ?)""",
				[p.id, Group.TYPE, RelationshipType.owns.strVal, Dashboard.TYPE, p.id, Dashboard.TYPE, RelationshipType.cloneOf.strVal, Dashboard.TYPE])
		return dashboardIdsWhichNeedCloning as Set
	}

	/**
	 * Creates clones of the supplied dashboards.
	 *
	 * @param dashboardsToClone the dashboards to copy.
	 * @param p the user who should be assigned to the copies.
	 */
	public void cloneGroupDashboardsForSingleUser(Set<Long> dashboardsToClone, Person p) {
		def maxPosition = getMaxDashboardPosition(p)
		dashboardsToClone.each { groupDashboardIdToClone ->
			def dbParent = Dashboard.read(groupDashboardIdToClone.dest_id)

			if (dbParent) {
				def dbClone = new Dashboard()
				dbClone.properties = dbParent.properties
				dbClone.guid = UUID.randomUUID().toString()
				dbClone.dashboardPosition = (maxPosition++) + (dbParent.dashboardPosition ?: 0)
				dbClone.user = p

				if (dbClone.layoutConfig) {
					String baseLayoutConfig = dbClone.layoutConfig
					String desiredLayoutConfig = (maskLayout(JSON.parse(baseLayoutConfig), dbClone.guid)).toString()
					dbClone.layoutConfig = desiredLayoutConfig
				}

				dbClone.save()

				new DomainMapping(srcId: dbClone.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.toString(),
						destId: dbParent.id, destType: Dashboard.TYPE).save()
			}
		}
	}

	/**
	 * All dashboards for the user, no limits on sorting, max number of returned objects, etc.  See
	 * the IndexController for where this gets used.
	 *
	 * @param p the user.
	 *
	 * @return a mess of dashboards.
	 */
	public List<Dashboard> listDashboardsForSingleUser(Person p) {
		def results = Dashboard.createCriteria().list() {
			eq 'user', p
		}
		return results
	}

	/**
	 * All dashboards for the user, with any limits or other filtering criteria via the list
	 * command object.  Typically used by the administration widgets.
	 *
	 * @param dlc the list command object, representing data from the browser.
	 * @param p the user.
	 *
	 * @return a sorted mess of dashboards.
	 */
	public List<Dashboard> listDashboardsForSingleUser(DashboardListCommand dlc, Person p) {
		def results = Dashboard.createCriteria().list([offset: dlc.offset, max: dlc.max]) {
			eq 'user', p
		}
		return results
	}

	private Map mapWidgetUniversalNameToWidgetGuid(JSONObject layoutConfig) {
		def mpReturn = [:]

		def widgets = layoutConfig.widgets
		widgets?.each { widget ->
			if (widget.universalName) {
				mpReturn.put(widget.universalName, widget.widgetGuid)
			}
		}

		def items = layoutConfig.items
		items?.each { item ->
			mpReturn.putAll(mapWidgetUniversalNameToWidgetGuid(item))
		}

		return mpReturn
	}

	public Dashboard createNewDashboard(JSONObject parsedDashboardData, Person user, boolean mask = false) {
		// Set all current dashboards for the user to not be default.
		if (parsedDashboardData.isdefault && user) {
			def dSetNotDefault = new DetachedCriteria(Dashboard).build {
				eq 'user', user
			}
			dSetNotDefault.updateAll(isdefault: false)
		}

		// Create the new dashboard using the parsedDashboardData
		def d = new Dashboard()
		d.properties = parsedDashboardData

		// Regardless of whether the parsed JSON includes these attributes, we're creating a new dashboard so
		// we're assigning some properties and overriding whatever may have been supplied.
		d.dashboardPosition = user ? getMaxDashboardPosition(user) + 1 : 1
		d.user = user

		// Take a look at the widgets that are part of the layoutConfig.  Make sure these exist and that we have
		// the right GUID for them.
		def layout = parsedDashboardData.layoutConfig.toString()
		if (layout && layout != 'undefined') {
			Map mpUniversalNameToGuid = mapWidgetUniversalNameToWidgetGuid(new JSONObject(layout))
			if (!mpUniversalNameToGuid.isEmpty()) {
				def widgets = WidgetDefinition.withCriteria {
					inList 'universalName', mpUniversalNameToGuid.keySet()
				}

				widgets.each { widget ->
					if (mpUniversalNameToGuid[widget.universalName] != widget.widgetGuid) {
						layout = layout.replaceAll(mpUniversalNameToGuid[widget.universalName], widget.widgetGuid)
					}
				}
			}
			d.layoutConfig = layout

			if (mask) {
				String baseLayoutConfig = d.layoutConfig
				String desiredLayoutConfig = (maskLayout(JSON.parse(baseLayoutConfig), d.guid)).toString()
				d.layoutConfig = desiredLayoutConfig
			}
		}

		d.save()

		return d
	}

	public void updateDashboard(JSONObject dbProperties, Dashboard db, boolean mask = false) {
		db.properties = dbProperties
		if (mask) {
			String baseLayoutConfig = db.layoutConfig
			String desiredLayoutConfig = (maskLayout(JSON.parse(baseLayoutConfig), db.guid)).toString()
			db.layoutConfig = desiredLayoutConfig
		}

		if (!db.save()) {
			if (db.hasErrors()) {
				throw new OwfException(message: 'A fatal validation error occurred during the updating of a dashboard.  Validation Errors: ' + db.errors.toString(),
				exceptionType: OwfExceptionTypes.Validation)
			}
			throw new OwfException(message: 'A fatal error occurred while trying to save a dashboard.', exceptionType: OwfExceptionTypes.Database)
		}

		if (db.user == null) {
			setGroupOwnershipOverGroupDashboardWidgets(db)
		}
	}

	private void setGroupOwnershipOverGroupDashboardWidgets(Dashboard dashboard) {
		// Before embarking on this adventure, let's see if there are widgets for us to own.
		def widgetGuids = []
		def js = JSON.parse(dashboard.layoutConfig)
		widgetGuids << inspectForWidgetGuids(js)
		widgetGuids = widgetGuids.flatten()
		widgetGuids.unique()

		if (!widgetGuids.isEmpty()) {
			// First, find the owning group record IDs
			def groupIds = DomainMapping.withCriteria {
				eq 'srcType', Group.TYPE
				eq 'relationshipType', RelationshipType.owns.strVal
				eq 'destType', Dashboard.TYPE
				eq 'destId', dashboard.id
				projections {
					distinct 'srcId'
				}
			}

			// Also, find the widget definition record IDs for the widgets under consideration
			def widgetIds = WidgetDefinition.withCriteria {
				inList 'widgetGuid', widgetGuids
				projections {
					property 'id'
				}
			}

			// Now, let's find all the existing domain mappings where any given group owns the
			// widgets and create any that are missing.  Yes, this is an each with an embedded query and
			// yes, that makes the performance-minded person shudder.  Might be able to make this run better
			// by dropping to straight SQL and generating the needed group-by clause to get a mapping of
			// group ID to owned widget ID and then fill in the holes.  If that's really an issue, then cross
			// the bridge when you can prove that this particular segment of code is *the* piece really slowing
			// things down....
			groupIds.each { groupId ->
				// In order for this to work over multiple iterations, we need a copy of the original list
				def tmpWidgetIds = []
				tmpWidgetIds.addAll(widgetIds)

				def alreadyMappedIds = DomainMapping.withCriteria {
					eq 'srcId', groupId
					eq 'srcType', Group.TYPE
					eq 'relationshipType', RelationshipType.owns.strVal
					eq 'destType', WidgetDefinition.TYPE
					inList 'destId', widgetIds
					projections {
						property 'destId'
					}
				}

				tmpWidgetIds.removeAll(alreadyMappedIds)
				tmpWidgetIds.each { widgetToAdd ->
					// Could perhaps get some more speed by stuffing these onto an array and then *.save() at the end.
					// Here again, if that's a demonstrated need, cross the bridge.  Suspect there are other fish to fry....
					new DomainMapping(srcId: groupId, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal,
							destType: WidgetDefinition.TYPE, destId: widgetToAdd).save()
				}
			}
		}
	}

	public def oldUpdateDashboard(params, dashboard) {
		// TODO:  DBS9.2 -- Single-point story here to eliminate the separate call to .validate() as the call to .save() will do
		// that for us.  Also, eliminate the flush: true from the save() call.  Next, as part of the same ticket, if the save does
		// fail, we should be able to interrogate the dashboard object and, if errors are present, throw the validation exception
		// back to the caller, otherwise throw the database exception.  Finally, we should then be able to factor out the group
		// dashboard work into a separate, private, function.
		try {
			if (dashboard.user == null) {
				// It's a group dashboard and we're going to need to make sure we've got group mappings for ownership
				// for the widgets on the dashboard, for each group that owns the dashboard.
			}
		}
		catch (e) {
			throw new OwfException(message: 'A fatal error occurred while trying to ensure dashboard defaults after a save. Changes have been backed out. Params: ' + params.toString(), exceptionType: OwfExceptionTypes.Database)
		}
		return [success: true, dashboard: dashboard]
	}

	private JSONObject maskLayout(JSONObject layoutConfig, String dbGuid) {
		def maskable = layoutConfig

		def items = maskable?.items
		items?.each { itm ->
			itm = maskLayout(itm, dbGuid)
		}

		def widgets = maskable?.widgets
		widgets?.each { w ->
			if (w.launchData) {
				w.launchData = ""
			}
			w.uniqueId = UUID.randomUUID().toString()
			w.dashboardGuid = dbGuid
		}

		return maskable
	}

	// Looks through the nested layoutConfig of a dashboard and grabs
	// all of its widgetGuids
	private List<String> inspectForWidgetGuids(layoutConfig) {
		def widgetGuids = []

		def widgets = layoutConfig.widgets
		widgets?.each { widget ->
			widgetGuids << widget?.widgetGuid
		}

		def items = layoutConfig.items
		items?.each { item ->
			widgetGuids.addAll(inspectForWidgetGuids(item))
		}

		return widgetGuids
	}

	public Dashboard restore(Dashboard dashboard, Boolean isdefault) {
		// Locate the parent dashboard.
		def parentDashboard = findParentDashboard(dashboard.id)

		// Now the restore....
		if (parentDashboard) {
			// You cannot use the shortcut ?: here; false is a legitimate value!
			dashboard.isdefault = isdefault != null ? isdefault : parentDashboard.isdefault
			dashboard.name = parentDashboard.name
			dashboard.description = parentDashboard.description
			dashboard.locked = parentDashboard.locked

			// Now for the really nasty part -- dealing with the layout.
			if (parentDashboard.layoutConfig) {
				String baseLayoutConfig = parentDashboard.layoutConfig
				String desiredLayoutConfig = (maskLayout(JSON.parse(baseLayoutConfig), dashboard.guid)).toString()
				dashboard.layoutConfig = desiredLayoutConfig
			}

			dashboard.save()
		}
		return dashboard
	}

	/**
	 * Establishes a default dashboard for the user, either the GUID supplied or the first in the user's
	 * dashboard sort order.
	 *
	 * @param p the user.
	 * @param guid [Optional] the GUID of the desired default dashboard.
	 */
	public void setUserDefault(Person p, String guid = null) {
		if (!p) {
			return
		}

		def setFalse = new DetachedCriteria(Dashboard).build {
			eq 'user', p
		}
		setFalse.updateAll(isdefault: false)

		if (guid) {
			def setTrue = new DetachedCriteria(Dashboard).build {
				and {
					eq 'user', p
					eq 'guid', guid
				}
			}
			setTrue.updateAll(isdefault: true)
		}
		else {
			def setRandomTrue = new DetachedCriteria(Dashboard).build {
				eq 'user', p
				order 'dashboardPosition', 'asc'
			}
			def randomTrue = setRandomTrue.list([max: 1])[0]
			randomTrue.isdefault = true
			randomTrue.save()
		}
	}

	public boolean validateJsonDataAsDashboards(String jsonData) {
		def pData = JSON.parse(jsonData)
		if (!pData instanceof JSONArray) return false

		boolean retVal = true
		pData.each {
			it.alteredByAdmin = false
			it.createdDate = null
			it.editedDate = null
			it.guid = UUID.randomUUID().toString()
			Dashboard d = new Dashboard(it as Map)

			retVal = retVal & d.validate()
		}

		retVal
	}

	private def getPersonByParams(params, returnSelf = true) {
		def person = null

		if (params.personId != null) {
			if (accountService.getLoggedInUserIsAdmin()) {
				person = Person.get(params.personId)
				if (person == null) {
					throw new OwfException(message:'Person with id of ' + params.personId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else {
				throw new OwfException(message:'You are not authorized to modify dashboards for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}
		else if (params.username != null) {
			if (accountService.getLoggedInUserIsAdmin()) {
				person = Person.findByUsername(params.username)
				if (person == null) {
					throw new OwfException(message:'Person with username of ' + params.username + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else {
				throw new OwfException(message:'You are not authorized to modify dashboards for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}
		else if (returnSelf == true) {
			person = accountService.getLoggedInUser()
		}
		return person
	}

	// FIXME: This is really a utility function and should be moved to a utility class as it's likely found
	// elsewhere among the services.
	private def convertStringToBool(stringToConvert) {
		// Let's be lenient with what we accept.
		if (stringToConvert instanceof java.lang.Boolean) {
			return stringToConvert
		}

		(stringToConvert == "true" || stringToConvert == "on") ? true : false
	}

	private def findByGuidForUser(guid, userid) {
		Map newParams = new HashMap()
		newParams.personId = userid
		def person = getPersonByParams(newParams)
		def dashboard = Dashboard.findByGuidAndUser(guid, person)
		return dashboard
	}

	private def convertJsonParamToDomainField(jsonParam) {
		switch (jsonParam) {
			case 'name':
				return 'name'
			case 'guid':
				return 'guid'
			case 'isdefault':
				return 'isdefault'
			case 'dashboardPosition':
				return 'dashboardPosition'
			case 'user.userId':
				return 'user'
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class Preference has not been mapped in PreferenceService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: Preference",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}

	//Generates a map between the universalName and wigetGuid of all widgets in a dashboard's layoutConfig
	private def getUniversalNameToGuidMap(universalNameToGuidMap, layoutConfig) {
		def widgets = layoutConfig.widgets
		for (def i = 0; i < widgets?.size(); i++) {
			if (widgets[i].universalName instanceof String) {
				universalNameToGuidMap[widgets[i].universalName] = widgets[i].widgetGuid
			}
		}

		def items = layoutConfig.items
		for (def i = 0; i < items?.size(); i++) {
			//Nested layoutConfig inside this pane, repeat the loop with it
			getUniversalNameToGuidMap(universalNameToGuidMap, items[i])
		}
	}

	private def getMaxDashboardPosition(person) {
		def result = Dashboard.withCriteria {
			if (person) {
				eq 'user', person
			}
			else {
				isNull 'user'
			}
			projections {
				max 'dashboardPosition'
			}
		}
		result[0] ?: -1
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
