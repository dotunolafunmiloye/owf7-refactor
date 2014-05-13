package ozone.owf.grails.domain

import grails.test.GrailsUnitTestCase
import ozone.owf.TestUtil

class DashboardTests extends GrailsUnitTestCase {

	def dashboard

	protected void setUp() {
		super.setUp()
		mockDomain(Dashboard)
		mockForConstraintsTests(Dashboard)
		dashboard = new Dashboard()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testGuidRequired() {
		TestUtil.assertPropertyRequired('guid', dashboard)
	}

	void testGuidIsGuidConstrained() {
		TestUtil.assertPropertyMatchesGuidConstraints('guid', dashboard)
	}

	void testNameIsRequired() {
		TestUtil.assertPropertyRequired('name', dashboard)
	}

	void testNameIsValid() {
		dashboard.name = "\""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = "\\"
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = """/"""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = "#"
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = "="
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = """{"""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = """}"""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = ":"
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = ";"
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = ""","""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = """["""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = """]"""
		TestUtil.assertNoErrorOnProperty('name', dashboard)
		dashboard.name = "Hello World 1234567890!@\$%^&*()_+-|?><`~."
		TestUtil.assertNoErrorOnProperty('name', dashboard)
	}

	void testNameEscapedIsValid() {
		dashboard.name = "\u5317\u7F8E\u4E2D\u6587\u5831\u7D19"
		TestUtil.assertNoErrorOnProperty('name', dashboard)
	}

	void testNotLockedByDefault() {
		TestUtil.assertEquals(false, dashboard.locked)
	}

	void testServiceModelNoParams() {
		dashboard.name = 'Test'
		dashboard.guid = UUID.randomUUID().toString()
		dashboard.dashboardPosition = 1

		def sm = dashboard.toServiceModel(null)
		assertNotNull sm
		assertTrue sm.groups.isEmpty()
		assertEquals sm.name, dashboard.name
		assertEquals sm.guid, dashboard.guid
		assertEquals sm.dashboardPosition, dashboard.dashboardPosition
	}

	void testServiceModelEmptyParams() {
		dashboard.name = 'Test'
		dashboard.guid = UUID.randomUUID().toString()
		dashboard.dashboardPosition = 1

		def sm = dashboard.toServiceModel([:])
		assertNotNull sm
		assertTrue sm.groups.isEmpty()
		assertEquals sm.name, dashboard.name
		assertEquals sm.guid, dashboard.guid
		assertEquals sm.dashboardPosition, dashboard.dashboardPosition
	}

	void testServiceModelWithParams() {
		def params = [:]
		params.groupCount = 5
		dashboard.name = 'Test'
		dashboard.guid = UUID.randomUUID().toString()
		dashboard.dashboardPosition = 1

		def sm = dashboard.toServiceModel(params)
		assertNotNull sm
		assertEquals params.groupCount, sm.groups.size()
		assertEquals sm.name, dashboard.name
		assertEquals sm.guid, dashboard.guid
		assertEquals sm.dashboardPosition, dashboard.dashboardPosition
	}
}
