package ozone.owf.grails.filters

import javax.annotation.PostConstruct

import org.apache.log4j.*
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken

import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.EDefaultGroupNames
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.security.authentication.OWFUserDetailsImpl

import com.yammer.metrics.MetricRegistry
import com.yammer.metrics.Timer


class SecurityFilters {

	def enum USERSTATE {NOCHANGE, UPDATE, NEW}

	def accountService
	def administrationService
	def personWidgetDefinitionService
	def preferenceService

	def config = ConfigurationHolder.config

	@Autowired
	MetricRegistry metricRegistry

	// Yammer metrics here
	def timers

	@PostConstruct
	public void initializeTimers() {
		timers= [
					'setUserDefaults': metricRegistry.timer(MetricRegistry.name(SecurityFilters.class, "setUserDefaults")),
					'createGroupRecordsInDatabase': metricRegistry.timer(MetricRegistry.name(SecurityFilters.class, "createGroupRecordsInDatabase")),
					'setPersonInfo': metricRegistry.timer(MetricRegistry.name(SecurityFilters.class, "setPersonInfo")),
					'setForAdmin': metricRegistry.timer(MetricRegistry.name(SecurityFilters.class, "setForAdmin")),
					'setGroupsForUser': metricRegistry.timer(MetricRegistry.name(SecurityFilters.class, "setGroupsForUser"))
				]
	}

	def filters = {
		securityAll(controller:'index', action:'index') {
			before = {
				// Before we go any farther, let's make sure that the groups
				// the user belongs to in LDAP are actually also stored in the
				// database, assuming that we want to, of course....
				if (config.autoMapLdapGroupsToOzone) {
					createGroupRecordsInDatabase()
				}
				def uState = setPersonInfo()
				if (uState != USERSTATE.NOCHANGE) {
					if (uState == USERSTATE.NEW) {
						setUserDefaults()
					}

					setGroupsForUser()

					// There's a guard in this call so that if the user is not
					// an administrator we take no action.
					setForAdmin()
				}
			}
		}
	}

	private def setUserDefaults() {
		def username = accountService.getLoggedInUsername()
		Timer.Context context = timers.setUserDefaults.time()

		def oldAuthentication = SCH.context.authentication
		def oldPrincipal = oldAuthentication.principal

		try {
			def newUser = Person.findByUsername(Person.NEW_USER)

			// temporarily give user admin privileges if they don't already have them so that
			// we can safely call our services for setting the user defaults
			def temporaryPrincipal = new OWFUserDetailsImpl(oldPrincipal.username, oldPrincipal.password, [ new org.springframework.security.core.authority.GrantedAuthorityImpl('ROLE_ADMIN') ], [])
			SCH.context.authentication = new PreAuthenticatedAuthenticationToken(temporaryPrincipal, temporaryPrincipal.getPassword());

			def personInDB = Person.findByUsername(username)
			if (newUser && personInDB) {
				def maxPosition = 0

				def widgets = newUser.personWidgetDefinitions*.widgetDefinition
				personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(personInDB, widgets)

				// Skip if dashboard belongs to a stack, it will be added as a
				// result of adding the user to the stack next
				def dbParms = []
				def dashBoards = Dashboard.findAllByUserAndStackIsNull(newUser)
				dashBoards.each{ db ->
					dbParms.add([
								checkedTargets: personInDB.id,
								guid: db.guid,
								isdefault: db.isdefault,
								name: db.name,
								description: db.description,
								locked: db.locked,
								layoutConfig: db.layoutConfig
							])
				}

				dbParms.each {
					administrationService.cloneDashboards(it)
				}

				// Get all stack default groups DEFAULT_USER is in
				def stackDefaultGroups = Group.withCriteria(uniqueResult: true) {
					eq('stackDefault', true)
					people {
						eq('id', newUser.id)
					}
				}
				stackDefaultGroups.each{
					// Add the stack default group to the user
					it.people << personInDB
					it.save(flush: true)
				}

				def pParams = []
				def preferences = Preference.findAllByUser(newUser)
				preferences.each{ pref ->
					pParams.add([
								checkedTargets: personInDB.id,
								namespace: pref.namespace,
								path: pref.path,
								value: pref.value
							])
				}
				pParams.each {
					administrationService.clonePreference(it)
				}
			}
		} catch (Exception e) {
			// Chew.  We don't want the exception bubbling back.
			log.warn e.toString()
		} finally {
			try {
				SCH.context.authentication = oldAuthentication
			} catch(Exception e) { e.printStackTrace()}
			context.stop()
		}
	}

	private def createGroupRecordsInDatabase() {
		Timer.Context context = timers.createGroupRecordsInDatabase.time()
		try {
			def userGroupNames = accountService.getLoggedInAutomaticUserGroups()*.owfGroupName
			// Don't assume the user has any groups.  Monitoring agents might not
			// have any and if the list is empty, the criteria query below will
			// bomb.  Bail early....
			if (!userGroupNames || userGroupNames.isEmpty()) {
				return
			}

			def userGroupAttribs = [:]
			accountService.getLoggedInAutomaticUserGroups().each {
				userGroupAttribs.put(it.owfGroupName, [email: it.owfGroupEmail, desc: it.owfGroupDescription])
			}

			def cExtant = Group.createCriteria()
			def gExtant = cExtant.list {
				'in'('name', userGroupNames)
				projections {
					property('name')
				}
			}
			userGroupNames.removeAll(gExtant)

			// Now that we have the list of existing group names among those
			// that belong to the user, remove that list and focus on the ones
			// that don't yet exist.
			//
			// NOTE: make sure to keep people initialized as an empty list or you'll
			// be hating life later when you go to add the current user to the group
			// -- you'll get NPE.
			userGroupNames.each {
				try {
					def grpCreate = new Group(
							name: it,
							displayName: it,
							description: userGroupAttribs.get(it).desc,
							email: userGroupAttribs.get(it).email,
							automatic: true,
							status: 'active',
							people: []
							)
					grpCreate.save(flush: true)
				} catch (Exception e) {
					// Chew.  We don't want the exception bubbling back.
				}
			}
		} finally {
			context.stop()
		}
	}

	private USERSTATE setPersonInfo() {
		Timer.Context context = timers.setPersonInfo.time()

		def personInDB
		try {
			personInDB = accountService.getLoggedInUser()

			// Default to checking at most one time per 24 hour span.
			if (personInDB && !personInDB.isOlderThan(config.userUpdateMillis?:43200000)) {
				return USERSTATE.NOCHANGE
			}

			if (!personInDB) {
				def loginDate = new Date()
				personInDB = new Person(
						username : accountService.getLoggedInUsername(),
						emailShow    : false,
						description  : '',
						enabled      : true,
						prevLogin : loginDate,
						lastLogin : loginDate
						)
			} else {
				personInDB.prevLogin = personInDB.lastLogin
				personInDB.lastLogin = new Date()
			}
			personInDB.userRealName = accountService.getLoggedInUserDisplayName()
			personInDB.email = accountService.getLoggedInUserEmail()
			personInDB.save(flush:true)

			if (personInDB.prevLogin == personInDB.lastLogin) {
				return USERSTATE.NEW
			}
			return USERSTATE.UPDATE
		} catch (Exception e) {
			// Chew.  We don't want the exception bubbling back.
			return USERSTATE.NOCHANGE
		} finally {
			context.stop()
		}
	}

	private def setForAdmin() {
		Timer.Context context = timers.setForAdmin.time()

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				return
			}

			def user = accountService.getLoggedInUser()

			// Not sure why, but this code was part of the legacy and
			// don't know if it can be removed.  Certainly can be
			// streamlined, though, from the original.
			def cHiddenAdminWidgets = WidgetDefinition.createCriteria()
			def hiddenAdminWidgets = cHiddenAdminWidgets.list {
				widgetTypes {
					eq('name', WidgetType.ADMIN)
				}
				eq('visible', false)
			}
			hiddenAdminWidgets.each {
				def coreName = it.displayName.split(' ')
				preferenceService.updateForUser(
						userid: user.id,
						namespace: "owf.admin.${coreName[0]}EditCopy",
						path: 'guid_to_launch',
						value: it.widgetGuid)
			}
		} finally {
			context.stop()
		}
	}

	private def setGroupsForUser() {
		Timer.Context context = timers.setGroupsForUser.time()

		// One wrinkle: we'll only manage those groups which are auto-managed.
		// What this effectively allows is manual groups can be named exactly
		// the same as an LDAP group and LDAP will *not* override the manual
		// group.
		def userGroupNames = accountService.getLoggedInAutomaticUserGroups()*.owfGroupName
		def username = accountService.getLoggedInUsername()
		userGroupNames << EDefaultGroupNames.GROUP_USER.strVal
		if (accountService.getLoggedInUserIsAdmin()) {
			userGroupNames << EDefaultGroupNames.GROUP_ADMIN.strVal
		}

		try {
			def personInDB = accountService.getLoggedInUser()

			def cRemoves = Group.createCriteria()
			def gRemoves = cRemoves.list {
				eq('automatic', true)
				people {
					eq('username', username)
				}
				not {
					'in'('name', userGroupNames)
				}
				projections {
					property('id')
				}
				cache false
			}

			// Adds are two-step:  find all the groups we have already and subtract
			// them out of the list of group names, then find the groups
			// corresponding to the remaining names and add those to the person.
			def cUserExist = Group.createCriteria()
			def gUserExist = cUserExist.list {
				eq('automatic', true)
				people {
					eq('username', username)
				}
				projections {
					property('name')
				}
				cache false
			}
			userGroupNames.removeAll(gUserExist)

			// Part 1 is done, now onto part 2.
			def gUserNeeds = []
			if (!userGroupNames.isEmpty()) {
				def cUserNeeds = Group.createCriteria()
				gUserNeeds = cUserNeeds.list {
					eq('automatic', true)
					'in'('name', userGroupNames)
					projections {
						property('id')
					}
					cache false
				}
			}

			// Now, we have a list of ids for groups we should remove and groups
			// we should add. Since the owf_group_people table is not managed
			// we're actually dropping to native SQL for the changes.
			def ctx = AH.application.mainContext
			def sessionFactory = ctx.sessionFactory
			def session = sessionFactory.getCurrentSession()
			def delQuery = "DELETE FROM owf_group_people WHERE person_id = ? AND group_id = ?"
			def addQuery = "INSERT INTO owf_group_people (person_id, group_id) VALUES (?, ?)"
			gRemoves.each {
				session.createSQLQuery(delQuery)
						.setLong(0, personInDB.id)
						.setLong(1, it)
						.executeUpdate()
			}
			gUserNeeds.each {
				session.createSQLQuery(addQuery)
						.setLong(0, personInDB.id)
						.setLong(1, it)
						.executeUpdate()
			}
		} catch (Exception e) {
			// Chew.  We don't want the exception bubbling back.
			log.warn "failed to update groups for user ${username} in database, message: ${e.toString()}"
		} finally {
			context.stop()
		}
	}
}
