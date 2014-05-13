package ozone.owf.grails.services

import grails.converters.JSON
import grails.gorm.DetachedCriteria

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.controllers.GroupListCommand
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition

class GroupService {

	public Map countPeopleInGroups(groupIds) {
		def results = Group.createCriteria().list {
			inList 'id', groupIds
			projections {
				groupProperty('id')
				rowCount()
			}
			people {}
		}

		def mp = [:]
		results.each { mp.put(it[0], it[1]) }
		mp
	}

	public Map countGroupOwnedWidgets(groupIds) {
		def results = DomainMapping.createCriteria().list {
			inList 'srcId', groupIds
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', WidgetDefinition.TYPE
			projections {
				groupProperty('srcId')
				rowCount()
			}
		}

		def mp = [:]
		results.each {
			mp.put(it[0], it[1])
		}
		mp
	}

	def listGroupsForWidgetOrDashboard(GroupListCommand glc) {
		def detached = new DetachedCriteria(DomainMapping).build {
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
		}

		def execDetached
		if (glc.widget_id != null) {
			def wId = WidgetDefinition.findByWidgetGuid(glc.widget_id)
			if (!wId) {
				throw new OwfException(message: 'Requested widget not found in database.', exceptionType: OwfExceptionTypes.NotFound)
			}

			execDetached = detached.build {
				eq 'destId', wId.id
				eq 'destType', WidgetDefinition.TYPE
			}
		}
		else if (glc.dashboard_id != null) {
			def dId = Dashboard.findByGuid(glc.dashboard_id)
			if (!dId) {
				throw new OwfException(message: 'Requested dashboard not found in database.', exceptionType: OwfExceptionTypes.NotFound)
			}

			execDetached = detached.build {
				eq 'destId', dId.id
				eq 'destType', Dashboard.TYPE
			}
		}
		def targetedIds = execDetached.list()*.srcId
		if (targetedIds.size() == 0) return targetedIds

		def results = Group.createCriteria().list([offset: glc.offset, max: glc.max]) {
			inList 'id', targetedIds
			order 'displayName', glc.order?.toLowerCase() ?: 'asc'
		}
		return results
	}

	def listGroupsForSingleUser(GroupListCommand glc) {
		def results = Group.createCriteria().list([offset: glc.offset, max: glc.max]) {
			people {
				eq 'id', glc.user_id
			}

			order 'displayName', glc.order?.toLowerCase() ?: 'asc'
		}
		return results
	}

	def listSingleGroup(GroupListCommand glc) {
		def results = Group.createCriteria().list([offset: glc.offset, max: glc.max]) {
			eq 'id', glc.id
		}
		return results
	}

	def list(GroupListCommand glc, person = null) {
		// If there's a person, add that to the criteria below.
		def criteria = Group.createCriteria()

		def createFilterCriteriaForGroupsAdminWidget = { filters ->
			JSON.parse(filters).each {
				ilike it.filterField, "%${it.filterValue}%"
			}
		}

		def results = criteria.list([offset: glc.offset, max: glc.max]) {
			if (glc.filters) {
				createFilterCriteriaForGroupsAdminWidget.delegate = delegate
				if (glc.filterOperator.toUpperCase() == 'OR') {
					or {
						createFilterCriteriaForGroupsAdminWidget(glc.filters)
					}
				}
				else {
					createFilterCriteriaForGroupsAdminWidget(glc.filters)
				}
			}

			// sorting -- only single sort
			if (glc.sort) {
				order glc.sort, glc.order?.toLowerCase() ?: 'asc'
			}
			else {
				// default sort
				order 'displayName', 'asc'
			}
		}
		return results
	}

	public List<Dashboard> copyDashboardsToGroup(JSONArray dashboards, JSONArray groups, boolean isGroupDashboard) {
		def groupDashboards = []

		dashboards.each {
			def dash = Dashboard.findByGuid(it.guid)
			if (dash != null) {
				def dashConfig = [:]
				dashConfig.guid = java.util.UUID.randomUUID().toString()
				dashConfig.isdefault = dash.isdefault
				dashConfig.dashboardPosition = it.dashboardPosition
				dashConfig.name = dash.name
				dashConfig.description = dash.description
				dashConfig.locked = dash.locked
				dashConfig.layoutConfig = dash.layoutConfig
				dashConfig.cloned = true
				dashConfig.isGroupDashboard = isGroupDashboard

				def newDb = new Dashboard()
				newDb.properties = dashConfig
				newDb.save()
				groupDashboards << newDb
			}
		}

		List<Long> groupIds = groups.collect { it.id as long }
		def grpRecords = Group.createCriteria().list {
			inList 'id', groupIds
			projections {
				property 'id'
			}
		}

		grpRecords.each { grpId ->
			groupDashboards.each { dash ->
				new DomainMapping(srcId: grpId, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: dash.id, destType: Dashboard.TYPE).save()
			}
		}

		return groupDashboards
	}

	public Group createOrUpdateSpecificGroup(JSONObject mpData) {
		Group grpToSave
		if (mpData.id > 0) {
			grpToSave = Group.get(mpData.id)
			if (!grpToSave) {
				throw new OwfException(message: 'Group ' + mpData.id + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
			grpToSave.properties = mpData
			grpToSave.save()
		}
		else {
			grpToSave = new Group(mpData as Map).save()
		}

		return grpToSave
	}

	public List<WidgetDefinition> createOrUpdateWidgetsForGroup(JSONArray widgetData, long groupId, boolean isAdd) {
		Group grpToModify = Group.read(groupId)
		if (!grpToModify) {
			throw new OwfException(message: 'Group ' + groupId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		// To maintain the API, we have to find all the WidgetDefinitions that match and create service
		// models.  Ack!
		def wdCriteria = WidgetDefinition.createCriteria()
		def widgets = wdCriteria.list {
			inList 'widgetGuid', widgetData*.widgetGuid
		}

		if (isAdd) {
			def existing = generateGroupCollectionCriteria(WidgetDefinition.TYPE, widgets*.id, groupId).list()
			def widgetsToAdd = widgets*.id as List
			widgetsToAdd.removeAll(existing*.destId)
			widgetsToAdd.each {
				new DomainMapping(srcId: groupId, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destType: WidgetDefinition.TYPE, destId: it).save()
			}
		}
		else {
			generateGroupCollectionCriteria(WidgetDefinition.TYPE, widgets*.id, groupId).deleteAll()
		}

		return widgets
	}

	public List<Dashboard> createOrUpdateDashboardsForGroup(JSONArray dashboardData, long groupId, boolean isAdd) {
		Group grpToModify = Group.read(groupId)
		if (!grpToModify) {
			throw new OwfException(message: 'Group ' + groupId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		// To maintain the API, we have to find all the Dashboards that match and create service
		// models.  Ack!
		def dCriteria = Dashboard.createCriteria()
		def dashboards = dCriteria.list {
			inList 'guid', dashboardData*.guid
		}

		if (isAdd) {
			def existing = generateGroupCollectionCriteria(Dashboard.TYPE, dashboards*.id, groupId).list()
			def dashboardsToAdd = dashboards*.id as List
			dashboardsToAdd.removeAll(existing*.destId)
			dashboardsToAdd.each {
				new DomainMapping(srcId: groupId, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destType: Dashboard.TYPE, destId: it).save()
			}
		}
		else {
			generateGroupCollectionCriteria(Dashboard.TYPE, dashboards*.id, groupId).deleteAll()
		}

		return dashboards
	}

	public List<Person> createOrUpdateUsersForGroup(JSONArray userData, long groupId, boolean isAdd) {
		Group grpToModify = Group.read(groupId)
		if (!grpToModify || grpToModify.automatic) {
			throw new OwfException(message: 'Group ' + groupId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		def pCriteria = Person.createCriteria()
		def users = pCriteria.list {
			inList 'username', userData*.username
		}

		// Crummy, yes, but you can't call the add or remove and pass a collection.  You could
		// drop to SQL here and write out the query to do the add/remove in one shot, but that's
		// kinda ugly, too, and may play hob with cache.
		if (isAdd) {
			users.each { grpToModify.addToPeople(it) }
		}
		else {
			users.each { grpToModify.removeFromPeople(it) }
		}

		return users
	}

	public List<Group> deleteGroups(Set<Long> groupIds) {
		def groups = Group.createCriteria().list {
			inList 'id', groupIds
		}

		def dmCriteria = new DetachedCriteria(DomainMapping).build {
			inList 'srcId', groupIds
			eq 'srcType', Group.TYPE
		}
		dmCriteria.deleteAll()

		groups*.people = []
		groups*.save()

		// This is why you don't name you domains using SQL reserved words.  You
		// can't do what you really want, which is a detached criteria and a deleteAll()
		// as shown above, because the Hibernate syntax parser gets all turned around
		// on itself and complains about a query syntax problem.
		groups.each {
			it.delete()
		}
		return groups
	}

	public boolean validateJsonDataAsGroups(String jsonData) {
		def pData = JSON.parse(jsonData)
		if (!pData instanceof JSONArray) return false

		boolean retVal = true
		pData.each {
			Group g = new Group(it as Map)
			retVal = retVal & g.validate()
		}

		retVal
	}

	private DetachedCriteria generateGroupCollectionCriteria(domainClazzStaticType, domainIds, groupId) {
		return new DetachedCriteria(DomainMapping).build {
			eq 'srcId', groupId
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', domainClazzStaticType
			inList 'destId', domainIds
		}
	}
}
