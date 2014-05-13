package integration.ozone.owf.grails.domain

import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.junit.After
import org.junit.Before
import org.junit.Test

import ozone.owf.grails.domain.OzonePrincipal
import ozone.owf.grails.domain.Person

class OzonePrincipalTests extends OWFGroovyTestCase {

	@Before
	void setUp() {
		// Setup logic here
	}

	@After
	void tearDown() {
		// Tear down logic here
	}

	@Test
	void testFromPerson() {
		def person = Person.build(username: "Mike O'Neil", userRealName: "Mike O'Neil")

		def prin = OzonePrincipal.fromPerson(person)
		assertNotNull prin
	}

	@Test
	void testUpdate() {
		def tDate1 = new Date('01/01/2009')
		def tDate2 = new Date('01/01/2010')
		def person = Person.build(username: "TestUpdate1".toUpperCase(), userRealName: "Test Update 1", enabled: true, description: 'something', lastLogin: tDate1)

		def prin = OzonePrincipal.findByPersonId(person.id)
		assertNotNull prin

		person.userRealName = 'Test Update 2'
		person.description = 'Description'
		person.lastLogin = tDate2
		person.save(flush: true)

		def prinCheck = OzonePrincipal.read(prin.id)
		assertEquals 'Test Update 2', prinCheck.friendlyName
		assertEquals 'Description', prinCheck.description
		assertEquals tDate2, prinCheck.lastLogin
	}

	@Test
	void testDelete() {
		def tDate1 = new Date('01/01/2009')
		def person = Person.build(username: "TestUpdate1".toUpperCase(), userRealName: "Test Update 1", enabled: true, description: 'something', lastLogin: tDate1)
		def pId = person.id

		def prin = OzonePrincipal.findByPersonId(pId)
		assertNotNull prin

		person.delete(flush: true)
		assertNull OzonePrincipal.findByPersonId(pId)
	}
}
