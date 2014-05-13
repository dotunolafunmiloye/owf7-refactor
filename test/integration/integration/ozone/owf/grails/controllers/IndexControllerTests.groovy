package integration.ozone.owf.grails.controllers

import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.junit.Before
import org.junit.Test

import ozone.owf.grails.controllers.IndexController
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType


class IndexControllerTests extends OWFGroovyTestCase {

	def dashboardService
	def accountService
	def personWidgetDefinitionService
	def p1

	@Before
	void setUp() {
		p1 = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')

		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, layoutConfig: '{}')
		def db2 = Dashboard.build(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 2, layoutConfig: '{}')
		def db3 = Dashboard.build(name: 'dashboard3', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3, layoutConfig: '{}')

		def g1 = Group.build(name: 'testGroup1', displayName: 'Test G. One')
		def g2 = Group.build(name: 'testGroup2', displayName: 'Test G. Two')
		def g3 = Group.build(name: 'testGroup3', displayName: 'Test G. Three')
		def g4 = Group.build(name: 'testGroup4', displayName: 'Test G. Four')

		DomainMapping.build(srcId: g1.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db1.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: g2.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db2.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: g3.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db3.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: g4.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db3.id, destType: Dashboard.TYPE)

		g1.addToPeople(p1)
		g2.addToPeople(p1)
		g3.addToPeople(p1)
		g4.addToPeople(p1)
	}

	@Test
	void testUserOnlyReceivesOneCopyOfGroupDashboard() {
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)

		def controller = new IndexController()
		controller.dashboardService = dashboardService
		controller.accountService = accountService
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.index()

		assertEquals 0, Dashboard.findAllByUser(p1).size()
	}
}
