package ozone.owf.grails.services

import grails.converters.JSON
import grails.gorm.DetachedCriteria
import groovy.sql.Sql

import org.hibernate.CacheMode

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition

class PersonWidgetDefinitionService {

	def accountService
	def domainMappingService
	def serviceModelService
	def sessionFactory

	def approveForAdminByTags(params) {
		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to list admin personal widget definitions.', exceptionType: OwfExceptionTypes.Authorization)
		}

		JSON.parse(params.toApprove).each { w ->
			def pwd = PersonWidgetDefinition.withCriteria {
				createAlias('person', 'person')
				createAlias('widgetDefinition', 'widgetDefinition')
				cache(true)

				if (w.id != null) {
					eq('id', w.id)
				}
				eq('person.username', w.userId)
				eq('widgetDefinition.widgetGuid', w.widgetGuid)
			}
			if (pwd != null && pwd.size() > 0) {
				pwd[0].disabled = false
			}
			else {
				//create mapping so the user can use the widget
				Person person = Person.findByUsername(w.userId, [cache:true])
				if (person != null) {
					def widgetParams = [
								guid: w.widgetGuid,
								personId: person.id
							]
					this.create(widgetParams)
				}
			}
		}

		JSON.parse(params.toDelete).each { w ->
			def pwd = PersonWidgetDefinition.withCriteria {
				createAlias('person', 'person')
				createAlias('widgetDefinition', 'widgetDefinition')
				cache(true)

				if (w.id != null) {
					eq('id', w.id)
				}
				eq('person.username', w.userId)
				eq('widgetDefinition.widgetGuid', w.widgetGuid)
			}
			if (pwd != null && pwd.size() > 0) {
				pwd[0].person?.removeFromPersonWidgetDefinitions(pwd[0])
				pwd[0].widgetDefinition?.removeFromPersonWidgetDefinitions(pwd[0])
				pwd[0].delete()
			}
		}

		return [success:true]
	}

	def listForAdminPendingWidgets(params) {
		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to list admin personal widget definitions.', exceptionType: OwfExceptionTypes.Authorization)
		}

		def opts = [:]
		def pwds = []

		//handle paging parameters
		if (params?.offset) opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		if (params?.max) opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)

		pwds = PersonWidgetDefinition.createCriteria().list(opts) {
			createAlias('person', 'person')
			createAlias('widgetDefinition', 'widgetDefinition')

			if (params?.pwds) {
				or {
					JSON.parse(params.pwds).each { p ->
						and {
							eq('person.username', p.userId)
							eq('widgetDefinition.widgetGuid', p.widgetGuid)
						}
					}
				}
			}

			//search for any disabled pwds
			eq('disabled', true)

			//filter by passed filter params - all fields are matched via st
			if (params.filters) {
				if (params.filterOperator?.toUpperCase() == 'OR') {
					or {
						JSON.parse(params.filters).each {
							ilike(convertJsonParamToDomainField(it.filterField), '%' + it.filterValue + '%')
						}
					}
				}
				else {
					JSON.parse(params.filters).each {
						ilike(convertJsonParamToDomainField(it.filterField), '%' + it.filterValue + '%')
					}
				}
			}

			//sort
			if (params?.sort) {
				order(convertJsonParamToDomainField(params.sort), params?.order?.toLowerCase() ?: 'asc')
			}

		}

		return [success: true, results: pwds.totalCount, data: pwds.collect{ serviceModelService.createServiceModel(it) }]
	}

	private def convertJsonParamToDomainField(jsonParam) {
		switch (jsonParam) {
			case ['widgetGuid']:
				return 'widgetDefinition.widgetGuid'
			case ['name']:
				return 'widgetDefinition.displayName'
			case ['userId']:
				return 'person.username'
			case ['userRealName']:
				return 'person.username'
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class WidgetDefinition has not been mapped in WidgetDefinitionService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: WidgetDefinition",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}

	/**
	 * Assigns group widgets to a given user.
	 *
	 * @param person the user.
	 */
	public void setGroupWidgetsForLoggedInUser(Person person) {
		// Find all the candidate widgets based on the person's group assignments
		// The desired SQL (which could be executed via Groovy SQL if needed):
		//		SELECT DISTINCT dm.dest_id AS wdID FROM domain_mapping dm, owf_group_people gp WHERE
		//			gp.person_id = ? AND
		//			gp.group_id = dm.src_id AND
		//			dm.src_type = ? AND
		//			dm.relationship_type = ? AND
		//			dm.dest_type = ?
		def gpCriteria = new DetachedCriteria(Group).build {
			people {
				eq 'id', person.id
			}
			projections {
				property 'id'
			}
		}
		def dmCriteria = new DetachedCriteria(DomainMapping).build {
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', WidgetDefinition.TYPE
			inList 'srcId', gpCriteria.list()
			projections {
				distinct 'destId'
			}
		}
		def groupWidgetIds = dmCriteria.list()

		if (!groupWidgetIds.isEmpty()) {
			def wdBase = new DetachedCriteria(WidgetDefinition).build {
				inList 'id', groupWidgetIds
			}
			def base = new DetachedCriteria(PersonWidgetDefinition).build {
				eq 'person', person
				eq 'groupWidget', true
				not {
					inList 'widgetDefinition', wdBase.list()
				}
			}

			// Wipe out any group widgets associated with the user that are not in the list of widgets they should have.
			def delWidgets = base.build {
				eq 'userWidget', false
			}
			delWidgets.deleteAll()

			// Update any user widgets which are in the list and remove the group association.
			def updateWidgets = base.build {
				eq 'userWidget', true
			}
			updateWidgets.updateAll(groupWidget: false)
		}

		// Now, find any widgets in the above list the user does not already possess and add them.
		// Start with the widgets the user already possesses.
		// The desired SQL (which could be executed via Groovy SQL if needed):
		//		SELECT DISTINCT widget_definition_id AS pwdID FROM person_widget_definition WHERE person_id = ?
		def pwdCriteria = new DetachedCriteria(PersonWidgetDefinition).build {
			eq 'person', person
			projections {
				property 'widgetDefinition.id'
			}
		}
		def assignedWidgetIds = pwdCriteria.list()

		// Any leftovers are new widgets.  Doing this with Groovy SQL because the WidgetDefinition is marked as the owning side of
		// the relationship, which means that any adds to PersonWidgetDefinition also involve contention for the parent widget
		// definition object.  It's a hack, yes, but hopefully a good one. ;-)
		groupWidgetIds.removeAll(assignedWidgetIds)
		if (!groupWidgetIds.isEmpty()) {
			def qMaxPosition = PersonWidgetDefinition.withCriteria {
				eq 'person', person
				projections {
					max 'pwdPosition'
				}
			}
			def maxPosition = qMaxPosition[0] ?: -1

			def sqlInsertNewGroupWidgets = new Sql(sessionFactory.currentSession.connection())
			sqlInsertNewGroupWidgets.withTransaction {
				sqlInsertNewGroupWidgets.withBatch(20, """
				INSERT INTO person_widget_definition
					(version, person_id, visible, pwd_position, widget_definition_id, group_widget, favorite, disabled, user_widget)
				VALUES
					(:version, :person_id, :visible, :pwd_position, :widget_definition_id, :group_widget, :favorite, :disabled, :user_widget)""",
						{ preparedStatement ->
							groupWidgetIds.each { widget ->
								preparedStatement.addBatch([version: 0, person_id: person.id, visible: 1, pwd_position: ++maxPosition, widget_definition_id: widget, group_widget: 1, favorite: 0, disabled: 0, user_widget: 0])
							}
						})
			}
		}
	}

	/**
	 * Finds all widget definitions mapped to the supplied user; note that this will return *all* the personal widget
	 * definitions for the user (no paging), so exercise caution in how you use this API.
	 *
	 * @param person the user.
	 *
	 * @return a list of personal widget definitions for the given user, sorted by position in ascending order.
	 */
	public List<PersonWidgetDefinition> listWidgetsForLoggedInUser(Person person) {
		def pwdList = PersonWidgetDefinition.createCriteria().list() {
			eq 'person', person
			order 'pwdPosition', 'asc'
		}

		return pwdList
	}

	/**
	 * Find widgets for a given user, returning the base widget definitions, rather than the personal overrides of the
	 * definitions.  Optionally can be restricted to a single widget GUID if desired.
	 *
	 * @param p the user.
	 * @param guid [optional] the desired widget GUID.
	 * @param name [optional] the desired widget name.
	 *
	 * @return a list of widget defintions.
	 */
	public List<WidgetDefinition> listWidgetsForUser(Person p, String guid = null, String name = null) {
		def wdList = WidgetDefinition.withCriteria {
			personWidgetDefinitions {
				eq 'person', p
			}

			if (guid) {
				eq 'widgetGuid', guid
			}

			if (name) {
				eq 'displayName', name
			}
		}

		return wdList
	}

	def list(params) {
		def opts = [:]

		if (params?.offset != null) opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		if (params?.max != null) opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)

		def person = accountService.getLoggedInUser()

		def groups = Group.withCriteria {
			people {
				eq('id', person.id)
			}
			eq('status', 'active')
			cache(true)

			//turn cache mode to GET which means don't use instances from this query for the 2nd level cache
			//seems to be a bug where the people collection is cached with only one person due to the people association filter above
			cacheMode(CacheMode.GET)
		}

		def queryReturn = PersonWidgetDefinition.executeQuery("SELECT MAX(pwd.pwdPosition) AS retVal FROM PersonWidgetDefinition pwd WHERE pwd.person = ?", [person])
		def maxPosition = (queryReturn[0] != null) ? queryReturn[0] : -1

		//loop through groups and save group widgets for this user
		def groupWidgetsToTagsMap = [:]

		groups.each { group ->
			def groupWidgets = domainMappingService.getMappedObjects(group, RelationshipType.owns, WidgetDefinition.TYPE)
			//loop through all groupWidgets for the group and save to the map
			groupWidgets.each { widgetDef ->
				if (groupWidgetsToTagsMap[widgetDef.widgetGuid] == null) {
					groupWidgetsToTagsMap[widgetDef.widgetGuid] = []
				}
				//saving a list of group name and widget def pairs
				groupWidgetsToTagsMap[widgetDef.widgetGuid] << group
			}

		}

		def allGroupWidgetsToTagsMap = groupWidgetsToTagsMap.clone()

		//find all group person widget defs found in the map
		def groupPwds = PersonWidgetDefinition.withCriteria {
			eq('person', person)
			eq('groupWidget', true)
			cacheMode(CacheMode.GET)
		}

		//loop through and remove groupPwds that are not in the groupWidgetsToTagsMap
		groupPwds.each { pwd ->
			if (groupWidgetsToTagsMap[pwd.widgetDefinition.widgetGuid]) {
				//remove from the map because this groupPwd has been processed
				groupWidgetsToTagsMap.remove(pwd.widgetDefinition.widgetGuid)
			}
			else {
				// delete pwd if a group widget is no longer in a group and user is not
				// directly associated to it.
				if (!pwd.userWidget) {
					Map newParams = new HashMap()
					newParams.personWidgetDefinition = pwd
					newParams.guid = pwd.widgetDefinition.widgetGuid
					delete(newParams)
				}
				else {
					// Just remove the group association from the pwd
					pwd.groupWidget = false
					pwd.save(flush: true)
				}
			}
		}

		//loop through the groupWidgetsToTagsMap, anything left will be new group widgets
		groupWidgetsToTagsMap.each { guid, groupTags ->
			def widgetDef = WidgetDefinition.findByWidgetGuid(guid, [cache: true])

			//lookup pwd this may have been previously created
			def personWidgetDefinitionList = PersonWidgetDefinition.withCriteria {
				eq('person', person)
				eq('widgetDefinition', widgetDef)
				cacheMode(CacheMode.GET)
			}
			def personWidgetDefinition = !personWidgetDefinitionList.isEmpty() ?  personWidgetDefinitionList[0] : null

			//if the pwd does not exist create it
			if (personWidgetDefinition == null) {
				personWidgetDefinition = new PersonWidgetDefinition(
						person: person,
						widgetDefinition: widgetDef,
						pwdPosition: maxPosition++,
						visible: true,
						favorite: false,
						userWidget: false,
						//only this method will ever set this groupWidget flag to true
						groupWidget: true
						)

				personWidgetDefinition.validate()

				if (personWidgetDefinition.hasErrors()) {
					throw new OwfException(message: 'A fatal validation error occurred during the creation of a widget.' + personWidgetDefinition.errors.toString(),
					exceptionType: OwfExceptionTypes.Validation)
				}
				else if (!personWidgetDefinition.save()) {
					throw new OwfException(message: 'A fatal error occurred while trying to save a widget. Params: ' + params.toString(),
					exceptionType: OwfExceptionTypes.Database)
				}
			}
		}

		//filter by any groups passed first
		def groupFilteredIds = []
		if (params?.groupIds) {
			for (groupId in JSON.parse(params?.groupIds)) {
				def tempGroupFilteredIds = []
				def group = Group.get(groupId.toLong())
				if (group != null) {
					def mappings = domainMappingService.getMappings(group, RelationshipType.owns, WidgetDefinition.TYPE)
					mappings?.each { tempGroupFilteredIds << it.destId }
				}

				if (groupFilteredIds.isEmpty()) {
					groupFilteredIds = tempGroupFilteredIds
				}
				else {
					groupFilteredIds = groupFilteredIds.intersect(tempGroupFilteredIds)
				}

				//If groupFilteredIds empty now, return no results
				if (groupFilteredIds.isEmpty()) {
					return [success: true, personWidgetDefinitionList: [], count: 0]
				}
			}
		}

		def pwdList = PersonWidgetDefinition.createCriteria().list(opts) {
			eq('person', person)
			if (params.widgetName) {
				def names = params.list('widgetName')
				widgetDefinition {
					names.each { name ->
						ilike("displayName", name)
					}
				}
			}
			if (params.customWidgetName) {
				def names = params.list('customWidgetName')
				names.each { name ->
					or {
						ilike("displayName", name)
						and {
							widgetDefinition { ilike("displayName", name) }
							isNull("displayName")
						}
					}
				}
			}
			if (params.customWidgetNameOrDesc) {
				def names = params.list('customWidgetNameOrDesc')
				names.each { name ->
					or {
						or {
							ilike("displayName", name)
							and {
								widgetDefinition { ilike("displayName", name) }
								isNull("displayName")
							}
						}
						widgetDefinition { ilike("description", name) }
					}
				}
			}
			if (params.widgetVersion) {
				widgetDefinition {
					eq("widgetVersion", params.widgetVersion)
				}
			}
			if (params.widgetGuid) {
				widgetDefinition {
					eq("widgetGuid", params.widgetGuid)
				}
			}
			if (params.visible) {
				eq("visible", params.visible.toBoolean())
				widgetDefinition {
					eq("visible", params.visible.toBoolean())
				}
			}
			if (params.disabled) {
				eq("disabled", params.disabled.toBoolean())
			}
			def widgetTypeList = params.list('widgetTypes')
			if (widgetTypeList) {
				widgetDefinition {
					widgetType {
						inList 'name', widgetTypeList
					}
				}
			}
			if (!groupFilteredIds.isEmpty()) {
				widgetDefinition { inList('id', groupFilteredIds) }
			}
			order("pwdPosition", "asc")
			cache(false)
		}

		return [success: true, personWidgetDefinitionList: pwdList.collect {
				serviceModelService.createServiceModel(it, [groups:allGroupWidgetsToTagsMap[it.widgetDefinition.widgetGuid]])
			},
			count: pwdList.totalCount]
	}

	def show(params) {
		def personWidgetDefinition = null

		def widgetDef = null
		if (params.universalName) {
			widgetDef = WidgetDefinition.findByUniversalName(params.universalName, [cache: true])
		}
		else {
			widgetDef = WidgetDefinition.findByWidgetGuid(params.guid, [cache: true])
		}
		def personWidgetDefinitionList = PersonWidgetDefinition.withCriteria {
			eq('person', accountService.getLoggedInUser())
			eq('widgetDefinition', widgetDef)
			cache(true)
		}

		personWidgetDefinition = !personWidgetDefinitionList.isEmpty() ? personWidgetDefinitionList[0] : null
		if (personWidgetDefinition != null) {
			return [success: true, personWidgetDefinition: personWidgetDefinition]
		}
		else {
			if (params.universalName) {
				throw new OwfException(message:'Widget with universal name of '
				+ params.universalName + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
			else {
				throw new OwfException(message:'Widget with guid of ' + params.guid
				+ ' not found.', exceptionType: OwfExceptionTypes.NotFound)
			}
		}
	}

	def create(params) {
		if (!params.guid) {
			throw new OwfException(	message:'A fatal validation error occurred during the creation of a widget. No GUID param was provided. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}

		// Get past the integration test failure in PersonWidgetDefinitionControllerTest.groovy
		def person = accountService.getLoggedInUser()
		if (!person) {
			throw new OwfException(message: 'You are not authorized to create widgets for other users.',
			exceptionType: OwfExceptionTypes.Authorization)
		}

		//check if personId is specified
		if (params.personId != null) {
			// you must be an admin to create a PWD for another user
			if (!params.personId.toString().equals(person.id.toString())) {
				if (accountService.getLoggedInUserIsAdmin()) {
					person = Person.get(params.personId)
					if (person == null) {
						throw new OwfException(message: 'Person with id of ' + params.personId + ' not found while attempting to create a widget for a user.', exceptionType: OwfExceptionTypes.NotFound)
					}
				}
				else {
					throw new OwfException(message: 'You are not authorized to create widgets for other users.', exceptionType: OwfExceptionTypes.Authorization)
				}
			}
			//else you are allowed if you are creating a pwd for yourself
		}

		def queryReturn = PersonWidgetDefinition.executeQuery("SELECT MAX(pwd.pwdPosition) AS retVal FROM PersonWidgetDefinition pwd WHERE pwd.person = ?", [person])
		def maxPosition = (queryReturn[0] != null)? queryReturn[0] : -1
		maxPosition++

		def wd = WidgetDefinition.findByWidgetGuid(params.guid)
		def personWidgetDefinition = new PersonWidgetDefinition(person: person,
				widgetDefinition: wd,
				visible: true,
				favorite: false,
				userWidget: true,
				groupWidget: false,
				displayName: params.displayName ?: params.name,
				pwdPosition: maxPosition)
		personWidgetDefinition.validate()

		if (personWidgetDefinition.hasErrors()) {
			throw new OwfException(	message:'A fatal validation error occurred during the creation of a widget.' + personWidgetDefinition.errors.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}
		else if (!personWidgetDefinition.save()) {
			throw new OwfException (message: 'A fatal error occurred while trying to save a widget. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Database)
		}
		return [success: true, personWidgetDefinition: personWidgetDefinition]
	}

	def delete(params) {
		def personWidgetDefinition

		if (params.personWidgetDefinition != null) {
			personWidgetDefinition = params.personWidgetDefinition
		}
		else {
			if (accountService.getLoggedInUserIsAdmin() && (params.adminEnabled == true)) {
				if(params.username != null) {
					personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(params.guid, null, params.username)
				}
				else if(params.personId != null) {
					personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(params.guid, params.personId, null)
				}
			}
			else {
				personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(params.guid, null, null)
			}

		}

		if (personWidgetDefinition != null) {
			def person
			if (!(personWidgetDefinition.person.username.equals(accountService.getLoggedInUsername()))) {
				if (!(accountService.getLoggedInUserIsAdmin() && (params.adminEnabled == true))) {
					throw new OwfException(message:'You are not authorized to delete this widget.', exceptionType: OwfExceptionTypes.Authorization)
				}
				else {
					if (params.username != null) {
						person = Person.findByUsername(params.username)
						if (person == null) {
							throw new OwfException(message:'Person with username of ' + params.username + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
						}
					}
					else if(params.personId != null) {
						person = Person.get(params.personId)
						if (person == null) {
							throw new OwfException(message:'Person with id of ' + params.personId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
						}
					}
				}
			}
			else {
				person = Person.findByUsername(accountService.getLoggedInUsername())
			}

			def widDefD = WidgetDefinition.findByWidgetGuid(params.guid)
			widDefD.personWidgetDefinitions?.remove(personWidgetDefinition)
			person.personWidgetDefinitions?.remove(personWidgetDefinition)

			try {
				personWidgetDefinition.delete()
				return [success: true, personWidgetDefinition: personWidgetDefinition]
			}
			catch (e) {
				log.error(e)
				throw new OwfException (message: 'A fatal error occurred while trying to delete a widget. Params: ' + params.toString(),
				exceptionType: OwfExceptionTypes.Database)
			}

		}
		else {
			throw new OwfException(message:'Widget ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
	}

	def update(params) {
		def personWidgetDefinition

		if (params.personWidgetDefinition != null) {
			personWidgetDefinition = params.personWidgetDefinition
		}
		else {
			personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(params.guid, params.personId, null)
		}

		if (personWidgetDefinition == null) {
			throw new OwfException(message:'Widget ' + params.guid + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}

		if (accountService.getLoggedInUserIsAdmin() || personWidgetDefinition.person.username.equals(accountService.getLoggedInUsername())) {
			personWidgetDefinition.displayName =  params.displayName ?: params.name
			personWidgetDefinition.visible = params.visible != null? convertStringToBool(params.visible) : personWidgetDefinition.visible
			personWidgetDefinition.pwdPosition = params.pwdPosition != null ? params.pwdPosition as Integer : personWidgetDefinition.pwdPosition

			personWidgetDefinition.validate()
			if (personWidgetDefinition.hasErrors()) {
				throw new OwfException(	message:'A fatal validation error occurred during the updating of a person widget definition. Params: '
				+ params.toString() + personWidgetDefinition.errors().toString(),
				exceptionType: OwfExceptionTypes.Validation)
			}
			else if (!personWidgetDefinition.save()) {
				throw new OwfException (message: 'A fatal error occurred while trying to save a widget. Params: ' + params.toString(),
				exceptionType: OwfExceptionTypes.Database)
			}

			return [success: true, personWidgetDefinition: personWidgetDefinition]
		}
	}

	//Guarantee transactionality for both the delete and update operations.
	//All or nothing.
	def bulkDeleteAndUpdate(params) {
		bulkDelete(params)
		bulkUpdate(params)
		return [success:true]
	}

	def bulkUpdate(params) {
		if (params.widgetsToUpdate == null) {
			throw new OwfException(	message:'A fatal validation error occurred. WidgetsToUpdate param required. Params: ' + params.toString(),exceptionType: OwfExceptionTypes.Validation)
		}
		def position = 0
		JSON.parse(params.widgetsToUpdate).each {
			def personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(it.guid, params.personId, null)
			if (personWidgetDefinition == null) {
				throw new OwfException(message:'Widget ' + it.guid + ' not found during bulk update.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map newParams = new HashMap()
			newParams.guid = it.guid
			newParams.name = it.name
			newParams.visible = it.visible
			if ((params.updateOrder != null) && (convertStringToBool(params.updateOrder) == true)) {
				newParams.pwdPosition = "" + position //Update position...
			}

			newParams.personWidgetDefinition = personWidgetDefinition

			def result = update(newParams)
			position ++
		}
		return [success:true]
	}

	def bulkDelete(params) {
		if (params.widgetGuidsToDelete == null) {
			throw new OwfException(message:'A fatal validation error occurred. WidgetsToDelete param required. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}

		JSON.parse(params.widgetGuidsToDelete).each {
			def personWidgetDefinition = getPersonWidgetDefinitionByGuidForUser(it, params.personId, null)
			if (personWidgetDefinition == null) {
				throw new OwfException(message:'Widget ' + it + ' not found during bulk delete.', exceptionType: OwfExceptionTypes.NotFound)
			}
			Map newParams = new HashMap()
			newParams.guid = it
			newParams.personWidgetDefinition = personWidgetDefinition
			def result = delete(newParams)
		}
		return [success: true]
	}

	def bulkAssignMultipleWidgetsForSingleUser(user, shouldHave = [], shouldNotHave = []) {
		if (!user) { return }

		def toCreate = []
		def toDelete = []

		if (shouldHave) {
			def cPwdToAdd = PersonWidgetDefinition.createCriteria()
			def hasAlready = cPwdToAdd.list() {
				eq('person', user)
				'in'('widgetDefinition', shouldHave)
				projections {
					property('widgetDefinition')
				}
			}
			toCreate.addAll(shouldHave)
			toCreate.removeAll(hasAlready)
		}

		if (shouldNotHave) {
			def cPwdToAdd = PersonWidgetDefinition.createCriteria()
			toDelete.addAll(cPwdToAdd.list() {
				eq('person', user)
				'in'('widgetDefinition', shouldNotHave)
				projections {
					property('widgetDefinition')
				}
			})
		}

		def newParams = [personId: user.id, adminEnabled: false]
		toCreate.each {
			newParams.guid = it.widgetGuid
			create(newParams)
		}

		toDelete.each {
			newParams.guid = it.widgetGuid
			delete(newParams)
		}
	}

	private getPersonWidgetDefinitionByGuidForUser(widgetGuid, personId, username) {
		def person = accountService.getLoggedInUser()

		if (personId != null) {
			if (accountService.getLoggedInUserIsAdmin()) {
				person = Person.get(personId)
				if (person == null) {
					throw new OwfException(message:'Person with id of ' + personId + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else {
				throw new OwfException(message:'You are not authorized to modify widgets for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}
		else if (username != null) {
			if (accountService.getLoggedInUserIsAdmin()) {
				person = Person.findByUsername(username)
				if (person == null) {
					throw new OwfException(message:'Person with username of ' + username + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
				}
			}
			else {
				throw new OwfException(message:'You are not authorized to modify widgets for other users.', exceptionType: OwfExceptionTypes.Authorization)
			}
		}

		def widgetDefintion = WidgetDefinition.findByWidgetGuid(widgetGuid)
		def personWidgetDefinition = PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefintion, person)
		return personWidgetDefinition
	}

	private def convertStringToBool(stringToConvert) {
		//Let's be lenient with what we accept.
		if (stringToConvert instanceof java.lang.Boolean) {
			return stringToConvert
		}
		(stringToConvert == "true") ? true : false
	}

	private def convertJsonParamToDomainField(jsonParam, jsonOrderDirParam) {
		switch (jsonParam) {
			case 'value.namespace':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.displayName  " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'widgetDefinition.displayName', 'restrictValue': restrictValue]
			case 'path':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.widgetGuid  " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'widgetDefinition.widgetGuid', 'restrictValue': restrictValue]
			case 'value.url':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.widgetUrl  " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'widgetDefinition.widgetUrl', 'restrictValue': restrictValue]
			case 'value.width':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.width  " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'widgetDefinition.width', 'restrictValue': restrictValue]
			case 'value.height':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.height  " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true,  'domainField': 'widgetDefinition.height', 'restrictValue': restrictValue]
			case 'value.smallIconUrl':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.imageUrlSmall " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true,  'domainField': 'widgetDefinition.imageUrlSmall', 'restrictValue': restrictValue]
			case 'value.largeIconUrl':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${WidgetDefinition.name} wd where pwd.widgetDefinition.id = wd.id order by wd.imageUrlLarge " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'widgetDefinition.imageUrlLarge', 'restrictValue': restrictValue]
			case 'value.visible':
				return ['restrict': false, 'domainField': 'visible']
			case 'value.position':
				return ['restrict': false, 'domainField': 'pwdPosition']
			case 'value.userId':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${Person.name} p where pwd.person.id = p.id order by p.username " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'person.username', 'restrictValue': restrictValue]
			case 'value.userRealName':
				def restrictValue = "select pwd from ${PersonWidgetDefinition.name} pwd, ${Person.name} p where pwd.person.id = p.id order by p.userRealName " + (jsonOrderDirParam?.toLowerCase() ?: 'asc') + ""
				return ['restrict': true, 'domainField': 'person.userRealName', 'restrictValue': restrictValue]
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class PersonWidgetDefinition has not been mapped in PersonWidgetDefinitionService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: PersonWidgetDefinition",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}

	def getDependents(params) {
		def person = accountService.getLoggedInUser()

		def srcGuids = params.ids
		def widgetDefs

		widgetDefs = WidgetDefinition.withCriteria({
			inList('widgetGuid', srcGuids)
		})

		def srcIds = widgetDefs.collect { it.id }
		def depIds = null
		def accDepIds = []

		def recs = domainMappingService.getDependentWidgets(srcIds)

		while (recs) {
			depIds = recs.collect { it.srcId }
			depIds = depIds.minus(accDepIds)
			if (depIds && depIds.size() > 0) {
				accDepIds.addAll(depIds)
				recs = domainMappingService.getDependentWidgets(depIds)
			}
			else {
				recs = null
			}
		}

		def widgetDefsFiltered = []
		def processedWidgets = []
		def pwd

		if (accDepIds && accDepIds.size() > 0) {
			widgetDefs = WidgetDefinition.withCriteria({
				inList('id', accDepIds)
			})

			widgetDefs.each { wd ->
				pwd = PersonWidgetDefinition.findByWidgetDefinitionAndPerson(wd, person)
				if (pwd) {
					widgetDefsFiltered.push(pwd)
				}
			}

			processedWidgets = widgetDefsFiltered.collect {
				serviceModelService.createServiceModel(it,[
							totalUsers: 0,
							totalGroups: 0
						])
			}
		}

		return [success: true, data: processedWidgets]
	}
}
