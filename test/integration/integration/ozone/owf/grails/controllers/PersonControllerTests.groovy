package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase
import ozone.owf.grails.controllers.PersonController
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Person

class PersonControllerTests extends OWFGroovyTestCase {

	def controller
	def administrationService
	def grailsApplication

	protected void setUp() {
		controller = new PersonController()
		controller.accountService = accountService
		controller.accountService.grailsApplication = grailsApplication
		controller.administrationService = administrationService
		controller.request.contentType = "text/json"
	}

	void testListPersons() {
		createDefaultUserAndAdminData()
		def count = Person.count()
		loginAsAdmin()

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'userRealName'
		controller.params.order = 'ASC'
		controller.list()
		assertEquals true, JSON.parse(controller.response.contentAsString).success
		assertEquals count, JSON.parse(controller.response.contentAsString).data.size()

		controller.params.filterOperator = 'AND'
		controller.list()
		assertEquals true, JSON.parse(controller.response.contentAsString).success
		assertEquals count, JSON.parse(controller.response.contentAsString).data.size()

		controller.params.filterOperator = 'OR'
		controller.list()
		assertEquals true, JSON.parse(controller.response.contentAsString).success
		assertEquals count, JSON.parse(controller.response.contentAsString).data.size()
	}

	// NOTE: we actually test the validation logic directly using a unit test, so no
	// need to repeat all that here.  What we do here is simply verify that the controller
	// responds with the desired string in the presence of bogus data (whatever the reason
	// may be).
	void testListPersonsInvalidDataReturnString() {
		loginAsAdmin()
		controller.list()
		assertTrue controller.response.contentAsString.indexOf('List command object has invalid data.') > -1
	}

	void testListPersonsAsNonAdminUser() {
		loginAsUsernameAndRole('bogusUser1', ERoleAuthority.ROLE_USER.strVal)
		controller.list()

		def respParsed = controller.response.contentAsString
		assertTrue respParsed.indexOf('You are not authorized to see a list of users in the system.') > -1
	}

	void testDeleteAction() {
		loginAsAdmin()
		def person = Person.build(username: 'testBogus2'.toUpperCase(), userRealName: 'Test Bogus 2')
		assertNotNull Person.findByUsername('testBogus2'.toUpperCase())

		controller.params.data = '[{id:' + person.id + '}]'
		controller.delete()

		def resp = controller.response.contentAsString

		assertNull Person.findByUsername('testBogus2'.toUpperCase())
	}

	void testUpdateAction() {
		loginAsAdmin()
		def person = Person.build(username: 'person1'.toUpperCase(), userRealName: 'Person', description: 'Updating Person')

		controller.params.data = [[
				id: person.id,
				username: 'person1'.toUpperCase(),
				userRealName: "New Person",
				description: "Updated Person"
			]].asType(JSON).toString()

		controller.createOrUpdate()

		person = Person.get(person.id)
		assertEquals "New Person", person.userRealName
		assertEquals "Updated Person", person.description
	}

	void testWhoamiAction() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase(), userRealName:'Test Admin 1')

		controller.whoami()

		assertEquals(([
					currentUserName: person.username,
					currentUser: person.userRealName,
					currentUserPrevLogin: person.prevLogin,
					currentId: person.id
				] as JSON).toString(), controller.response.contentAsString)
	}
}
