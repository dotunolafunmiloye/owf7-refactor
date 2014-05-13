package integration.ozone.owf.grails.conf

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.User

import ozone.owf.grails.domain.EDefaultGroupNames
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.services.ServiceModelService

class OWFGroovyTestCase extends GroovyTestCase {

	int testAdmin1ID = -1
	int testUser1ID = -1
	int testUser2ID = -1
	def accountService

	protected void setUp() {
		super.setUp()
		accountService.serviceModelService = new ServiceModelService()
	}

	protected void tearDown() {
		SCH.clearContext();
		super.tearDown();
	}

	protected void loginAsAdmin() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
	}

	protected void loginAsUsernameAndRole(def username, def role_string) {
		SCH.clearContext()

		GrantedAuthority[] authorities = [new GrantedAuthorityImpl(role_string)]
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(
				new User(username.toUpperCase(), "password1", true, true, true, true, authorities),
				"password1"
				)
	}

	protected createDefaultUserAndAdminData() {
		if (testAdmin1ID < 0) {
			Person.build(username: 'testAdmin1'.toUpperCase(), userRealName: 'Test Admin', email: 'something@something.com')
			Group.findByName(EDefaultGroupNames.GROUP_ADMIN.strVal).addToPeople(Person.findByUsername("testAdmin1".toUpperCase()))

			testAdmin1ID = Person.findByUsername("testAdmin1".toUpperCase()).id
		}
		if (testUser1ID < 0) {
			Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test User 1', email: 'something@something.com')
			Group.findByName(EDefaultGroupNames.GROUP_USER.strVal).addToPeople(Person.findByUsername("testUser1".toUpperCase()))

			testUser1ID = Person.findByUsername("testUser1".toUpperCase()).id
		}
		if (testUser2ID < 0) {
			Person.build(username: 'testUser2'.toUpperCase(), userRealName: 'Test User 2', email: 'something@something.com')
			Group.findByName(EDefaultGroupNames.GROUP_USER.strVal).addToPeople(Person.findByUsername("testUser2".toUpperCase()))

			testUser2ID = Person.findByUsername("testUser2".toUpperCase()).id
		}
	}

}
