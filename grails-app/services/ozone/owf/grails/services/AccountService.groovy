package ozone.owf.grails.services

import grails.converters.JSON

import org.hibernate.CacheMode
import org.springframework.security.core.context.SecurityContextHolder as SCH

import ozone.owf.grails.AuditOWFWebRequestsLogger
import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.EDefaultGroupNames
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.OzonePrincipal
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.WidgetDefinition
import ozone.security.authentication.OWFUserDetails

/**
 * Service for account-related operations.
 */
class AccountService {

	def grailsApplication
	def userAuthService
	def loggingService = new AuditOWFWebRequestsLogger()
	def serviceModelService

	def getLoggedInUser() {
		return Person.findByUsername(getLoggedInUsername(), [cache: true])
	}

	def getLoggedInPrincipal() {
		return OzonePrincipal.findByCanonicalName(getLoggedInUsername(), [cache: true])
	}

	/*
	 * Grab the display name from spring security user detail object if the custom security module implemented it.
	 * If not just use user name.
	 */
	def getLoggedInUserDisplayName() {
		def p = SCH?.context?.authentication?.principal
		def displayName = p?.username ?: "unknown"
		if (p?.metaClass?.hasProperty(p, "displayName")) {
			displayName = p?.displayName ?: displayName
		}
		return displayName
	}

	def getLoggedInUsername() {
		return SCH?.context?.authentication?.principal?.username?.toUpperCase()
	}

	def getLoggedInUserIsAdmin() {
		for (role in getLoggedInUserRoles()) {
			if (role.authority.equals(ERoleAuthority.ROLE_ADMIN.strVal)) {
				return true
			}
		}
		return false
	}

	def getLoggedInUserIsUser() {
		for (role in getLoggedInUserRoles()) {
			if (role.authority.equals(ERoleAuthority.ROLE_USER.strVal)) {
				return true
			}
		}
		return false
	}

	def getLoggedInUserRoles() {
		return SCH?.context?.authentication?.principal?.authorities
	}

	def getLoggedInAutomaticUserGroups() {
		def user = SCH?.context?.authentication?.principal
		if (user instanceof OWFUserDetails) {
			// Defensive code. If the user details object was created without
			// any groups (as might happen with a non-person user used for
			// monitoring), make sure we don't return null to a consumer that
			// expects, at worst, an empty list.
			return user?.owfGroups ?: []
		}
		return []
	}

	/*
	 * Grab the email from spring security user detail object if the custom security module implemented it.
	 * If not, return an empty string.
	 */
	def getLoggedInUserEmail() {
		def p = SCH?.context?.authentication?.principal
		def email = ""
		if (p?.metaClass?.hasProperty(p, "email")) {
			email = p?.email ?: email
		}
		return email
	}

	def getAllUsers() {
		if (!getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to see a list of users in the system.', exceptionType: OwfExceptionTypes.Authorization)
		}
		return Person.listOrderByUsername()
	}

	// TODO: We'll want to constrain here on the basis of whether it's a group or user principal object, which
	// will require such a field on the domain object.  Suggest that this could be two method calls, like
	// getAllUserPrincipals and getAllGroupPrincipals
	def getAllPrincipals() {
		if (!getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to see a list of principals in the system.', exceptionType: OwfExceptionTypes.Authorization)
		}
		return OzonePrincipal.listOrderByCanonicalName()
	}

	// TODO: We'll need to find where this gets used in the code and then start chopping down the number of places.
	// If we need to, we'll need to introduce a faceted-style service method that works for the OzonePrincipal
	// domain.
	def getAllUsersByParams(params) {
		if (!getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to see a list of users in the system.', exceptionType: OwfExceptionTypes.Authorization)
		}
		def criteria = Person.createCriteria()
		def opts = [:]

		if (params?.offset) opts.offset = (params.offset instanceof String ? Integer.parseInt(params.offset) : params.offset)
		if (params?.max) opts.max = (params.max instanceof String ? Integer.parseInt(params.max) : params.max)

		// Adding a max here to keep from pulling every user in the database.  As the
		// number grows, the following code becomes pretty unmanageable.
		def countOfPeople = Person.findAll().size()
		if (countOfPeople > 100 && !params?.max) {
			opts.max = 100
		}

		def personList = criteria.list(opts) {
			if (params.id) {
				eq("id", Long.parseLong(params.id))
			}

			if (params.group_id) {
				groups {
					eq("id", Long.parseLong(params.group_id))
				}
			}

			if (params.widget_id) {
				personWidgetDefinitions {
					widgetDefinition {
						eq("widgetGuid", params.widget_id)
					}

					// only list widgets that are explicitly assigned
					// to this user
					eq("userWidget", true)
				}
			}

			if (params.filters) {
				if (params.filterOperator?.toUpperCase() == 'OR') {
					or {
						JSON.parse(params.filters).each {
							switch(it.filterField) {
								case 'lastLogin':
									if (it.filterValue instanceof List) {
										between("lastLogin", new Date(it.filterValue[0]), new Date(it.filterValue[1] + 86400000))
									}
									else {
										def from = it.filterValue - (it.filterValue % 86400000)
										between("lastLogin", new Date(from), new Date(from + 86400000))
									}
									break
								default:
									ilike(it.filterField, '%' + it.filterValue + '%')
							}
						}
					}
				}
				else {
					JSON.parse(params.filters).each {
						switch(it.filterField) {
							case 'lastLogin':
								if (it.filterValue instanceof List) {
									between("lastLogin", new Date(it.filterValue[0]), new Date(it.filterValue[1] + 86400000))
								}
								else {
									def from = it.filterValue - (it.filterValue % 86400000)
									between("lastLogin", new Date(from), new Date(from + 86400000))
								}
								break
							default:
								ilike(it.filterField, '%' + it.filterValue + '%')
						}
					}
				}
			}

			if (params?.sort) {
				order(params.sort, params?.order?.toLowerCase() ?: 'asc')
			}
			cacheMode(CacheMode.GET)
		}

		def processedList = personList.collect { p ->
			def groupCount = Group.withCriteria {
				cacheMode(CacheMode.GET)
				people {
					eq('id', p.id)
				}

				projections { rowCount() }
			}

			def widgetCount = PersonWidgetDefinition.withCriteria {
				cacheMode(CacheMode.GET)
				person {
					eq('id', p.id)
				}
				projections { rowCount() }
			}

			def dashboardCount = Dashboard.withCriteria {
				cacheMode(CacheMode.GET)
				user {
					eq('id', p.id)
				}
				projections { rowCount() }
			}
			serviceModelService.createServiceModel(p, [
						totalGroups: groupCount[0],
						totalWidgets: widgetCount[0],
						totalDashboards: dashboardCount[0]
					])
		}

		return [success: true, data: processedList, results: personList.totalCount]
	}

	// TODO: We'll need to address this for OzonePrincipal in that we could be saving a
	// new group principal.  Overall, this method should be removed when we no longer
	// require it, but we'll need to ensure that the equivalent functionality remains
	// so that administrators can create a new group using the group widget.
	def createOrUpdate(params) {
		if (!getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to see a list of users in the system.',
			exceptionType: OwfExceptionTypes.Authorization)
		}

		def returnValue = null
		def isNewUser = false

		log.debug("AccountService.createOrUpdate with params:: " + params)
		if (params.data && !params.tab) {
			def users = []
			def newUsers = []
			def json = JSON.parse(params.data)
			json.each { data ->
				data.each {
					if (!data.isNull(it.key)) {
						params[it.key] = it.value
					}
				}
				def user = Person.findByUsername(params.username)
				if (user && !params.id) {
					throw new OwfException(message: 'A user with this name already exists.',exceptionType: OwfExceptionTypes.GeneralServerError)
				}

				if (!user) {
					//Create
					user = new Person()
					user.enabled = true
					user.emailShow = true
					loggingService.log("Added new User [username:" + user.username +
							",userRealName:" + user.userRealName + "]")
					isNewUser = true
				}
				params.lastLogin = params.lastLogin ? new Date(params.lastLogin) : null
				params.prevLogin = params.prevLogin ? new Date(params.prevLogin) : null
				user.properties = params

				user.save(failOnError: true, flush: true)
				users << user
				if (isNewUser) {
					newUsers << user
				}
			}

			def ctx = grailsApplication.mainContext
			def sessionFactory = ctx.sessionFactory
			def session = sessionFactory.getCurrentSession()
			def addQuery = "INSERT INTO owf_group_people (person_id, group_id) VALUES (?, ?)"

			def grpAllUsers = Group.findByName(EDefaultGroupNames.GROUP_USER.strVal)
			newUsers.each { personRec ->
				session.createSQLQuery(addQuery)
						.setLong(0, personRec.id)
						.setLong(1, grpAllUsers.id)
						.executeUpdate()
			}

			returnValue = users.collect { serviceModelService.createServiceModel(it) }
		}
		else if (params.update_action && (params.id || params.user_id)) {
			def id = params.id ?: params.user_id
			def user = Person.findById(id,[cache:true])
			if (user) {
				def updatedWidgets = []
				if ('widgets' == params.tab) {
					def widgets = JSON.parse(params.data)

					widgets.each {
						def widget = WidgetDefinition.findByWidgetGuid(it.id, [cache: true])
						if (widget) {
							def results = PersonWidgetDefinition.createCriteria().list() {
								eq("person", user)
								eq("widgetDefinition", widget)
							}

							if (params.update_action == 'add') {
								if (!results) {
									def queryReturn = PersonWidgetDefinition.executeQuery("SELECT MAX(pwd.pwdPosition) AS retVal FROM PersonWidgetDefinition pwd WHERE pwd.person = ?", [user])

									def maxPosition = queryReturn[0]?: -1
									maxPosition++
									def personWidgetDefinition = new PersonWidgetDefinition(
											person: user,
											widgetDefinition: widget,
											userWidget: true,
											visible: true,
											pwdPosition: maxPosition)

									user.addToPersonWidgetDefinitions(personWidgetDefinition)
									widget.addToPersonWidgetDefinitions(personWidgetDefinition)
								}
								// If the user already had this PWD, then set the direct user
								// assocation flag.
								else if (results[0] != null) {
									results[0].userWidget = true
									results[0].save(flush: true)
								}
							}
							else if (params.update_action == 'remove') {
								results?.each { result ->
									// If the user was assigned the widget through a group,
									// keep the PWD but un-flag it's direct user association.
									if (result.groupWidget) {
										result.userWidget = false
										result.save(flush:true)
									}
									else {
										// The user no longer has any association to the widget;
										// remove the PWD.
										user.removeFromPersonWidgetDefinitions(result)
										widget.removeFromPersonWidgetDefinitions(result)
									}
								}
							}

							updatedWidgets << widget
						}
					}
					if (updatedWidgets)
						returnValue = updatedWidgets.collect { serviceModelService.createServiceModel(it) }
				}

				//handle associations
				if ('groups' == params.tab) {
					def updatedGroups = []
					def groups = JSON.parse(params.data)
					groups.each {
						def group = Group.findById(it.id.toLong(), [cache: true])
						if (group) {
							if (params.update_action == 'add') {
								group.addToPeople(user)
							}
							else if (params.update_action == 'remove') {
								group.removeFromPeople(user)
							}

							group.save(flush: true, failOnError: true)
							updatedGroups << group
						}
					}
					if (!updatedGroups.isEmpty()) {
						returnValue = updatedGroups.collect { serviceModelService.createServiceModel(it) }
					}
				}

			}
		}

		return [success: true, data: returnValue]
	}

	// TODO: We'll need to address this for OzonePrincipal in that we could be deleting
	// user or group principals through this method.  Unstated, but very necessary, we'll want
	// to use a better interface than the "params" object.
	def bulkDeleteUsersForAdmin(personIdsToDelete) {
		if (!getLoggedInUserIsAdmin()) {
			throw new OwfException(message:'You are not authorized to bulkDelete Admin users.', exceptionType: OwfExceptionTypes.Authorization)
		}

		// Similar code concept also occurs in the SecurityFilters class to
		// manage this relationship.
		def cUserExist = Group.createCriteria()
		def gUserExist = cUserExist.list {
			eq('automatic', true)
			people {
				'in'('id', personIdsToDelete)
			}
			projections {
				property('id')
			}
			cache false
		}

		def ctx = grailsApplication.mainContext
		def sessionFactory = ctx.sessionFactory
		def session = sessionFactory.getCurrentSession()
		def delQuery = "DELETE FROM owf_group_people WHERE person_id = ? AND group_id = ?"

		personIdsToDelete.each { personId ->
			gUserExist.each { groupId ->
				session.createSQLQuery(delQuery)
						.setLong(0, personId)
						.setLong(1, groupId)
						.executeUpdate()
			}

			def result = deleteUser(['person': Person.get(personId), 'adminEnabled': true])
		}

		return [success: true]
	}

	// TODO: Naturally, we'll remove this when we're ready to cut over to principal
	private def deleteUserLoggedInCheck(Person person) {
		def loggedInUser = getLoggedInUser()
		if (loggedInUser && loggedInUser.username.equalsIgnoreCase(person.username)) {
			throw new OwfException(message:'Your are not permitted to delete yourself.', exceptionType: OwfExceptionTypes.GeneralServerError)
		}
	}

	private def deleteUserLoggedInCheck(OzonePrincipal person) {
		def loggedInUser = getLoggedInPrincipal()
		if (loggedInUser && loggedInUser.canonicalName.equalsIgnoreCase(person.canonicalName)) {
			throw new OwfException(message:'Your are not permitted to delete yourself.', exceptionType: OwfExceptionTypes.GeneralServerError)
		}
	}

	// TODO: We'll likely need to see if this is even reachable and, if so, change it for OzonePrincipal
	// and tighten down the interface based on some kind of command object.
	def deleteUser(params) {
		def person
		if (params.person) {
			person = params.person
		}
		else {
			if (getLoggedInUserIsAdmin() && params.username) {
				person = Person.findByUsername(params.username)
			}
			else {
				throw new OwfException(message:'User ' + params.username  + ' is not permitted to delete a user ', exceptionType: OwfExceptionTypes.NotFound)
			}
		}

		// Before we embark upon this process, perform a few basic checks.  Likely
		// that we should check the user is administrative here, versus in the branching
		// construct above, but the test was there originally and we presume that this
		// function could be called by the application itself or other means which would
		// preclude testing here.
		if (person == null) {
			throw new OwfException(message:'User ' + params.username + ' not found.', exceptionType: OwfExceptionTypes.NotFound)
		}
		if (person.username.equalsIgnoreCase(Person.NEW_USER)) {
			throw new OwfException(message: 'The default template user may not be deleted',
			exceptionType: OwfExceptionTypes.Authorization)
		}
		deleteUserLoggedInCheck((Person) person)

		try {
			//we need to make a copy of the list of groups because hibernate will update the same list when a the user is
			//removed from the group
			def groups = person.groups.collect { it }
			groups.each { it.removeFromPeople(person) }

			//we need to unset audit log fields which will
			//cause a constraint violation if we let grails simply cascade delete the user

			//search through all dashboards for dashboards which have reference to the user being deleted
			def dashboards = Dashboard.withCriteria { eq('createdBy', person) }

			//if there are any dashboards unassign this user from the fields
			dashboards.each {
				//explicitly clear those audit log fields
				it.createdBy = null
			}

			//search through all dashboards for dashboards which have reference to the user being deleted
			dashboards = Dashboard.withCriteria { eq('editedBy', person) }

			//if there are any dashboards unassign this user from the fields
			dashboards.each {
				//explicitly clear those audit log fields
				it.editedBy = null
			}

			person.delete(flush: true)
			return [success: true, person: person]
		}
		catch (e) {
			log.error(e)
			throw new OwfException(message: 'A fatal error occurred while trying to delete a user. Params: ' + params.toString(), exceptionType: OwfExceptionTypes.Database)
		}
	}

	private def convertJsonParamToDomainField(jsonParam) {
		switch (jsonParam) {
			case 'username':
				return 'username'
			case 'userRealName':
				return 'userRealName'
			case 'lastLogin':
				return 'lastLogin'
			case 'prevLogin':
				return 'prevLogin'
			default :
				log.error("JSON parameter: ${jsonParam} for Domain class Preference has not been mapped in PreferenceService#convertJsonParamToDomainField")
				throw new OwfException (message: "JSON parameter: ${jsonParam}, Domain class: Preference",
				exceptionType: OwfExceptionTypes.JsonToDomainColumnMapping)
		}
	}
}
