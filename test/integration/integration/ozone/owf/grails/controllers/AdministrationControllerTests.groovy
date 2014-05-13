package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.controllers.AdministrationController
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.Preference

class AdministrationControllerTests extends OWFGroovyTestCase {

	def preferenceService
	def administrationService
	def controller

	/**
	 * Preferences Integration Tests
	 */
	void testAddPreferenceSubmit() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		controller = new AdministrationController()
		controller.administrationService = administrationService
		controller.request.contentType = "text/json"

		controller.params.path = "test path entry 5"
		controller.params.value = "value"
		controller.params.namespace = "com.foo.bar"
		controller.params.isExtAjaxFormat = true
		controller.params.checkedTargets = person.id

		controller.addCopyPreferenceSubmit()

		def preference = Preference.findByUser(person)

		assertEquals 'test path entry 5', JSON.parse(controller.response.contentAsString).path
		assertEquals 'com.foo.bar', JSON.parse(controller.response.contentAsString).namespace
		assertEquals 'test path entry 5', preference.path
		assertEquals 'com.foo.bar', preference.namespace
	}

	void testCopyPreferencesSubmit() {
		loginAsAdmin()
		def personAdmin = Person.build(username:'testAdmin1'.toUpperCase())
		def personUser = Person.build(username:'testUser1'.toUpperCase())
		def preference = Preference.build(path:'test path entry 5', namespace:'com.foo.bar', value:'value', user:personAdmin)

		controller = new AdministrationController()
		controller.administrationService = administrationService
		controller.request.contentType = "text/json"

		controller.params.path = preference.path
		controller.params.value = preference.value
		controller.params.namespace = preference.namespace
		controller.params.isExtAjaxFormat = true
		controller.params.checkedTargets = personUser.id // Copy preference parameters in testAdmin1 to testUser1

		controller.addCopyPreferenceSubmit()

		preference = Preference.findByUser(personUser)

		assertEquals 'test path entry 5', JSON.parse(controller.response.contentAsString).path
		assertEquals 'com.foo.bar', JSON.parse(controller.response.contentAsString).namespace
		assertEquals 'test path entry 5', preference.path
		assertEquals 'com.foo.bar', preference.namespace
	}

	void testUpdatePreference() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())
		def preference = Preference.build(path:'test path entry 5', namespace:'com.foo.bar', value:'value', user:person)

		controller = new AdministrationController()
		controller.administrationService = administrationService
		controller.request.contentType = "text/json"

		controller.params.path = "test path entry 6"
		controller.params.originalPath = "test path entry 5"
		controller.params.value = "value.value"
		controller.params.namespace = "com.foo.bar.dev"
		controller.params.originalNamespace = "com.foo.bar"

		controller.updatePreference()

		preference = Preference.findByUser(person)

		assertEquals 'test path entry 6', JSON.parse(controller.response.contentAsString).path
		assertEquals 'com.foo.bar.dev', JSON.parse(controller.response.contentAsString).namespace
		assertNotSame 'test path entry 5', JSON.parse(controller.response.contentAsString).path
		assertNotSame 'com.foo.bar', JSON.parse(controller.response.contentAsString).namespace
		assertEquals 'test path entry 6', preference.path
		assertEquals 'com.foo.bar.dev', preference.namespace
		assertNotSame 'test path entry 5', preference.path
		assertNotSame 'com.foo.bar', preference.namespace
	}

	void testListPreferences() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar1', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar2', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar3', value:'value', user:person)

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.listPreferences()

		assertEquals 3, JSON.parse(controller.response.contentAsString).results
		assertEquals 'test path entry 1', JSON.parse(controller.response.contentAsString).rows[0].path
		assertEquals 'test path entry 2', JSON.parse(controller.response.contentAsString).rows[1].path
		assertEquals 'test path entry 3', JSON.parse(controller.response.contentAsString).rows[2].path
		assertEquals 'com.foo.bar1', JSON.parse(controller.response.contentAsString).rows[0].namespace
		assertEquals 'com.foo.bar2', JSON.parse(controller.response.contentAsString).rows[1].namespace
		assertEquals 'com.foo.bar3', JSON.parse(controller.response.contentAsString).rows[2].namespace
	}

	void testListPreferencesWithNamespaceParam() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar', value:'value', user:person)

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.params.namespace = 'com.foo.bar'
		controller.listPreferences()

		assertEquals 3, JSON.parse(controller.response.contentAsString).results
		assertEquals 'test path entry 1', JSON.parse(controller.response.contentAsString).rows[0].path
		assertEquals 'test path entry 2', JSON.parse(controller.response.contentAsString).rows[1].path
		assertEquals 'test path entry 3', JSON.parse(controller.response.contentAsString).rows[2].path
		assertEquals 'com.foo.bar', JSON.parse(controller.response.contentAsString).rows[0].namespace
		assertEquals 'com.foo.bar', JSON.parse(controller.response.contentAsString).rows[1].namespace
		assertEquals 'com.foo.bar', JSON.parse(controller.response.contentAsString).rows[2].namespace
	}

	void testDeletePreferencesByPersonId() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar1', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar2', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar3', value:'value', user:person)

		assertEquals 3, Preference.list().size()

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.params.path = 'test path entry 2'
		controller.params.namespace = 'com.foo.bar2'
		controller.params.userid = person.id
		controller.deletePreferences()

		assertEquals 2, Preference.list().size()
		assertEquals 'test path entry 2', JSON.parse(controller.response.contentAsString).path
		assertEquals 'com.foo.bar2', JSON.parse(controller.response.contentAsString).namespace
	}

	void testDeletePreferencesByPersonUsername() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar1', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar2', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar3', value:'value', user:person)

		assertEquals 3, Preference.list().size()

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.params.path = 'test path entry 1'
		controller.params.namespace = 'com.foo.bar1'
		controller.params.username = person.username
		controller.deletePreferences()

		assertEquals 2, Preference.list().size()
		assertEquals 'test path entry 1', JSON.parse(controller.response.contentAsString).path
		assertEquals 'com.foo.bar1', JSON.parse(controller.response.contentAsString).namespace
	}

	void testDeleteNonexistentPreference() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar1', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar2', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar3', value:'value', user:person)

		assertEquals 3, Preference.list().size()

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.params.path = 'test path entry'
		controller.params.namespace = 'com.foo.bar'
		controller.params.username = person.username
		controller.deletePreferences()

		assertEquals 3, Preference.list().size()
		assertNull JSON.parse(controller.response.contentAsString).path
		assertNull JSON.parse(controller.response.contentAsString).namespace
	}

	void testBulkDeletePreferences() {
		loginAsAdmin()
		def person = Person.build(username:'testAdmin1'.toUpperCase())

		Preference.build(path:'test path entry 1', namespace:'com.foo.bar1', value:'value', user:person)
		Preference.build(path:'test path entry 2', namespace:'com.foo.bar2', value:'value', user:person)
		Preference.build(path:'test path entry 3', namespace:'com.foo.bar3', value:'value', user:person)

		assertEquals 3, Preference.list().size()

		def preferenceJsonList = new JSONArray()
		Preference.list().each{
			def json = new JSONObject(namespace:it.namespace, path:it.path, username:it.user.username)
			preferenceJsonList.add(json)
		}

		controller = new AdministrationController()
		controller.preferenceService = preferenceService
		controller.request.contentType = "text/json"

		controller.params.preferencesToDelete = preferenceJsonList.toString()
		controller.params.namespace = null
		controller.params.path = null
		controller.params._method = 'DELETE'
		controller.deletePreferences()

		assertEquals 0, Preference.list().size()
	}
}
