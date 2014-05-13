package integration.ozone.owf.grails.services

import integration.ozone.owf.grails.conf.DataClearingTestCase

import org.springframework.dao.DataAccessException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.EDefaultGroupNames
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Person
import ozone.owf.grails.services.AccountService
import ozone.security.authentication.OWFUserDetailsImpl

class AccountServiceTests extends DataClearingTestCase {

	/**
	 * Simulate the LDAP-based service which would fish out roles.
	 */
	private class MyTestUserDetailsService implements UserDetailsService {
		@Override
		public UserDetails loadUserByUsername(String arg0)
		throws UsernameNotFoundException, DataAccessException {
			def setAuthorities = [] as Set
			def setGroups = [] as Set
			setAuthorities.add(new GrantedAuthorityImpl(ERoleAuthority.ROLE_USER.strVal))
			setGroups.add(EDefaultGroupNames.GROUP_USER.strVal)

			if (arg0.toLowerCase().contains('admin')) {
				setAuthorities.add(new GrantedAuthorityImpl(ERoleAuthority.ROLE_ADMIN.strVal))
				setGroups.add(EDefaultGroupNames.GROUP_ADMIN.strVal)
			}

			def owfDetails = new OWFUserDetailsImpl(arg0.toUpperCase(), null, setAuthorities, setGroups)

			owfDetails
		}
	}

	def accountService
	def serviceModelService

	public void setUp() {
		super.setUp()

		// login as a regular user.  Call loginAsAdmin if your test needs
		loginAsUsernameAndRole("testUser1", "ROLE_USER")
		accountService = new AccountService()
		accountService.userAuthService = new MyTestUserDetailsService()
	}

	protected void tearDown() {
		SCH.clearContext()
		super.tearDown()
	}

	void testGetAllUsersMustBeAdmin() {
		shouldFail(OwfException, {
			accountService.getAllUsers()
		})
	}

	void testGetAllUsers() {
		loginAsAdmin()
		def baseCount = Person.count()
		Person.build(username: 'username', userRealName: 'Real Name', enabled: true)
		assertEquals baseCount + 1, accountService.getAllUsers().size
	}

	void testGetAllUsersWithApos() {
		// this is used in the administrative service.  TODO:  Move this test there or refactor
		loginAsAdmin()
		def baseCount = Person.count()
		def username = "Mike O'Reilly"
		Person.build(username: username, userRealName: 'Real Name', enabled: true)
		def json = accountService.getAllUsers().collect { serviceModelService.createServiceModel(it) }
		assertNotNull json
		assertEquals username, json[baseCount].username
	}

	void testGetLoggedInAutomaticUserGroups() {
		// We're testing indirectly here as the actual object in the authentication
		// context is not an instance of OWFUserDetailsImpl (see the login... method
		// below).  So we're reaching back up a bit to verify that, if we actually
		// had the right kind of object, we'd get the right results....
		def detailsAdmin = accountService.userAuthService.loadUserByUsername('testAdmin1')
		assertNotNull detailsAdmin
		assertTrue detailsAdmin instanceof OWFUserDetailsImpl
		assertEquals 2, detailsAdmin.owfGroups.size()

		def detailsUser = accountService.userAuthService.loadUserByUsername('testUser1')
		assertNotNull detailsUser
		assertTrue detailsUser instanceof OWFUserDetailsImpl
		assertEquals 1, detailsUser.owfGroups.size()

		// As per above, the actual list will be empty when testing....
		loginAsAdmin()
		def grps = accountService.getLoggedInAutomaticUserGroups()
		assertNotNull grps
		assertTrue grps.isEmpty()
	}

	private void loginAsAdmin() {
		loginAsUsernameAndRole("testAdmin1", "ROLE_ADMIN")
	}

	private void loginAsUsernameAndRole(def username, def role_string) {
		SCH.clearContext()

		GrantedAuthority[] authorities = [new GrantedAuthorityImpl(role_string)]
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
		new User(username, "password1", true, true, true, true, authorities),
		"password1"
		)
	}

}
