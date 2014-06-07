package ozone.owf.grails.services

import grails.test.*

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder as SCH

import ozone.owf.grails.OwfException
import ozone.security.authentication.OWFUserDetailsImpl
import ozone.security.authorization.model.GrantedAuthorityImpl
import ozone.security.authorization.model.OwfGroupImpl

class AccountServiceTests extends GrailsUnitTestCase {

	AccountService testService = new AccountService()

	protected Authentication authenticate(person, credentials, authorities) {
		Authentication auth = new TestingAuthenticationToken(person, null, authorities as GrantedAuthority[])
		auth.authenticated = true
		SCH.context.authentication = auth
		return auth
	}

	protected void setUp() {
		def person = new OWFUserDetailsImpl(
				'unitTestUser', null,
				[new GrantedAuthorityImpl("ROLE_USER")], [new OwfGroupImpl("A_GROUP")])

		person.displayName = 'Unit Test User'
		person.organization = 'Government'
		person.email = 'test@test.gov'
		authenticate(person, "creds", [new GrantedAuthorityImpl("ROLE_USER")])
	}

	void testGetLoggedInAutomaticUserGroups() {
		assertTrue testService.loggedInAutomaticUserGroups.size == 1
	}

	void testGetLoggedInUserDisplayName() {
		assertTrue testService.loggedInUserDisplayName == 'Unit Test User'
	}

	void testGetLoggedInUserEmail() {
		assertTrue testService.loggedInUserEmail == 'test@test.gov'
	}

	void testGetLoggedInUserIsUser() {
		assertTrue testService.loggedInUserIsUser
	}

	void testGetLoggedInUserIsAdmin() {
		assertFalse testService.loggedInUserIsAdmin
	}

	void testGetLoggedInUsername() {
		assertTrue testService.loggedInUsername == 'unitTestUser'.toUpperCase()
	}

	void testGetLoggedInUserRoles() {
		def desired = [new GrantedAuthorityImpl("ROLE_USER")]
		def result = testService.loggedInUserRoles

		assertTrue result.size == desired.size
		assertTrue result[0].authority == desired[0].authority
	}

	void testGetAllUsers() {
		// Should throw exception for ordinary user.
		try {
			testService.getAllUsersByParams([:])
			fail('Expected exception not thrown, ordinary user can enumerate all accounts')
		} catch (Exception e) {
			assertTrue e instanceof OwfException
			assertTrue e.message == 'You are not authorized to see a list of users in the system.'
			assertNotNull e.exceptionType
		}
	}

	void testGetAllUsersByParams() {
		// Should throw exception for ordinary user.
		try {
			testService.getAllUsersByParams([:])
			fail('Expected exception not thrown, ordinary user can enumerate all accounts')
		} catch (Exception e) {
			assertTrue e instanceof OwfException
			assertTrue e.message == 'You are not authorized to see a list of users in the system.'
			assertNotNull e.exceptionType
		}
	}

	void testNegatives() {
		SCH.clearContext()

		assertTrue testService.loggedInUserDisplayName == 'unknown'
		assertTrue testService.loggedInAutomaticUserGroups.size == 0
		assertTrue testService.loggedInUserEmail == ''
		assertFalse testService.loggedInUserIsUser
		assertFalse testService.loggedInUserIsAdmin
		assertNull testService.loggedInUsername
		assertNull testService.loggedInUserRoles
	}
}
