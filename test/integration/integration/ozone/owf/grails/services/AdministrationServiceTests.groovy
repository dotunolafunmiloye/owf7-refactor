package integration.ozone.owf.grails.services

import integration.ozone.owf.grails.conf.OWFGroovyTestCase
import ozone.owf.grails.controllers.PersonListCommand
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.WidgetDefinition

class AdministrationServiceTests extends OWFGroovyTestCase {

	def administrationService

	/**
	 * OWF-1087
	 *
	 * There was a 255 character limit on the field value. Turned this into a text field (clob in oracle)
	 */
	void testAddBigJson() {
		loginAsAdmin()
		Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test User 1', email: 'something@something.com')
		Person.build(username: 'testUser2'.toUpperCase(), userRealName: 'Test User 2', email: 'something@something.com')
		Person.build(username: 'testAdmin1'.toUpperCase(), userRealName: 'Test Admin', email: 'something@something.com')

		def testAdmin1ID = Person.findByUsername("testAdmin1".toUpperCase()).id
		def testUser1ID = Person.findByUsername("testUser1".toUpperCase()).id
		def testUser2ID = Person.findByUsername("testUser2".toUpperCase()).id

		Map pref = [
					checkedTargets:	[testAdmin1ID,testUser1ID,testUser2ID],
					isExtAjaxFormat: true,
					namespace: "com.company.widget",
					path: "name",
					value: '{ "a" : "1234567890", "b" : "1234567890", "c" : "1234567890", "d" : "1234567890", "e" : "1234567890", "f" : "1234567890", "g" : "1234567890", "h" : "1234567890", "i" : "1234567890", "j" : "1234567890", "k" : "1234567890", "l" : "1234567890", "m" : "1234567890", "n" : "1234567890", "o" : "1234567890", "p" : "1234567890", "q" : "1234567890", "r" : "1234567890", "s" : "1234567890", "t" : "1234567890", "u" : "1234567890", "v" : "1234567890", "w" : "1234567890", "x" : "1234567890", "y" : "1234567890", "z" : "1234567890" }'
				]
		administrationService.clonePreference(pref)
		assertEquals Preference.findByPath("name").namespace, 'com.company.widget'
	}

	/**
	 * OWF-1087
	 *
	 * There was a 255 character limit on the field value. Turned this into a text field (clob in oracle)
	 */
	void testEditPreferenceWithLargeValue() {
		loginAsAdmin()
		Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test User 1', email: 'something@something.com')
		Person.build(username: 'testUser2'.toUpperCase(), userRealName: 'Test User 2', email: 'something@something.com')
		Person.build(username: 'testAdmin1'.toUpperCase(), userRealName: 'Test Admin', email: 'something@something.com')

		def testAdmin1ID = Person.findByUsername("testAdmin1".toUpperCase()).id
		def testUser1ID = Person.findByUsername("testUser1".toUpperCase()).id
		def testUser2ID = Person.findByUsername("testUser2".toUpperCase()).id
		Map pref = [
					checkedTargets:	[testAdmin1ID,testUser1ID,testUser2ID],
					isExtAjaxFormat: true,
					namespace: "com.company.widget",
					path: "name",
					value: 'test'
				]
		administrationService.clonePreference(pref)
		assertEquals Preference.findByPath("name").namespace, 'com.company.widget'

		Map updatedPref = [
					userid:	testAdmin1ID,
					isExtAjaxFormat: true,
					namespace: "com.company.widget2",
					originalNamespace: "com.company.widget",
					path: "name2",
					originalPath: "name",
					value: '12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890'
				]
		administrationService.updatePreference(updatedPref)
		assertEquals Preference.findByPath("name2").namespace, 'com.company.widget2'
	}

	void testListUsersForUserAdminWidget() {
		Person.build(username: 'BogusUser1', userRealName: 'Bogus User Number 1')
		Person.build(username: 'BogusUser2', userRealName: 'Bogus User Number 2')
		Person.build(username: 'BogusUser3', userRealName: 'Bogus User Number 3')
		Person.build(username: 'SortCheck1', userRealName: 'Sort Check Number 1')
		def baseCount = Person.count()

		// PersonListCommand API
		// The API from a validation perspective is tested by the controller.  Do not test here....
		// Instead, we want to test for adequate coverage of query possibilities here and verify
		// that we get the right results for our query.
		//
		//		String filters
		//		String filterOperator
		//		Integer offset
		//		Integer max
		//		Long id
		//		Long group_id
		//		String widget_id
		//		String sort
		//		String order
		//
		// Results are always of form:
		//      [success: true, data: processedList, results: personList.totalCount]

		// Basic tests here
		PersonListCommand cmd
		cmd = generateStockPersonListCommand()
		def r1 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals baseCount, r1.results
		assertEquals baseCount, r1.data.size()
		assertEquals 'BogusUser1', r1.data[0].username

		cmd = generateStockPersonListCommand()
		cmd.order = 'DESC'
		def r2 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals baseCount, r2.results
		assertEquals baseCount, r2.data.size()
		assertEquals 'SortCheck1', r2.data[0].username

		cmd = generateStockPersonListCommand()
		cmd.order = 'DESC'
		cmd.max = 1
		def r3 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals baseCount, r3.results
		assertEquals 1, r3.data.size()
		assertEquals 'SortCheck1', r3.data[0].username

		cmd = generateStockPersonListCommand()
		cmd.offset = 1
		def r4 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals baseCount, r4.results
		assertEquals baseCount - 1, r4.data.size()
		assertEquals 'BogusUser2', r4.data[0].username

		cmd = generateStockPersonListCommand()
		cmd.id = Person.findByUsername('BogusUser1').id
		def r5 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 1, r5.results
		assertEquals 1, r5.data.size()
		assertEquals 'BogusUser1', r5.data[0].username

		// Build a group and assign here
		Group.build(name: 'Group1', displayName: 'Group Number 1')
		def g = Group.findByName('Group1')
		g.addToPeople(Person.findByUsername('BogusUser1'))

		cmd = generateStockPersonListCommand()
		cmd.group_id = g.id
		def r6 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 1, r6.results
		assertEquals 1, r6.data.size()
		assertEquals 'BogusUser1', r6.data[0].username

		// Build a widget definition and then a PWD to test that path as well.
		def guid = java.util.UUID.randomUUID().toString()
		WidgetDefinition.build(widgetGuid: guid)
		def w = WidgetDefinition.findByWidgetGuid(guid)
		def p = Person.findByUsername('BogusUser1')
		PersonWidgetDefinition.build(person: p, widgetDefinition: w)
		def pwd = PersonWidgetDefinition.findByPerson(p)

		cmd = generateStockPersonListCommand()
		cmd.widget_id = guid
		def r7 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 0, r7.results
		assertEquals 0, r7.data.size()

		pwd.userWidget = true
		pwd.save(flush: true)
		def r8 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 1, r8.results
		assertEquals 1, r8.data.size()
		assertEquals 'BogusUser1', r8.data[0].username

		// Filtering tests here
		cmd = generateStockPersonListCommand()
		cmd.filters = "[{filterField: 'username', filterValue: 'Check'}, {filterField: 'userRealName', filterValue: 'Check'}, {filterField: 'email', filterValue: 'Check'}]"
		cmd.filterOperator = 'or'
		def r9 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 1, r9.results
		assertEquals 1, r9.data.size()
		assertEquals 'SortCheck1', r9.data[0].username

		cmd.filterOperator = 'and'
		def r10 = administrationService.listUsersForUserAdminWidget(cmd)
		assertEquals 0, r10.results
		assertEquals 0, r10.data.size()
	}

	private PersonListCommand generateStockPersonListCommand() {
		PersonListCommand cmd = new PersonListCommand()
		cmd.offset = 0
		cmd.max = 50
		cmd.sort = 'userRealName'
		cmd.order = 'ASC'

		return cmd
	}
}
