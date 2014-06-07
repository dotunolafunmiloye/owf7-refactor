package ozone.owf.grails.services

import grails.test.*

import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder as SCH

import ozone.security.authentication.OWFUserDetailsImpl
import ozone.security.authorization.model.GrantedAuthorityImpl

class AccountServiceAdminTests extends GrailsUnitTestCase {

	AccountService testService = new AccountService()

	protected Authentication authenticate(person, credentials, authorities) {
		Authentication auth = new TestingAuthenticationToken(person, null, authorities as GrantedAuthority[])
		auth.authenticated = true
		SCH.context.authentication = auth
		return auth
	}

	protected void setUp() {
		def person = new OWFUserDetailsImpl(
				'unitTestAdmin', null,
				[new GrantedAuthorityImpl("ROLE_ADMIN")], [])

		person.displayName = 'Unit Test Admin'
		person.organization = 'Government'
		person.email = 'test@test.gov'
		authenticate(person, "creds", [new GrantedAuthorityImpl("ROLE_ADMIN")])
	}

	void testGetLoggedInUserIsUser() {
		assertFalse testService.loggedInUserIsUser
	}

	void testGetLoggedInUserIsAdmin() {
		assertTrue testService.loggedInUserIsAdmin
	}

	void testGetLoggedInUserRoles() {
		def desired = [new GrantedAuthorityImpl("ROLE_ADMIN")]
		def result = testService.loggedInUserRoles

		assertTrue result.size == desired.size
		assertTrue result[0].authority == desired[0].authority
	}
}
