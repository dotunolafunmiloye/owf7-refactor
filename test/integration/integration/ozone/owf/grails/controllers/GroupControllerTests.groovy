package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.controllers.GroupController
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition

class GroupControllerTests extends OWFGroovyTestCase {

	def groupService
	def controller

	@Override
	protected void setUp() {
		super.setUp()
		def p = Person.build(username: 'TestUserGroup1', userRealName: 'Test User Group1')
		def g = Group.build(name: 'Group1', description: '', email: 'test@test.com', automatic: false, status: 'active')
		g.addToPeople(p)

		Dashboard.build(guid: UUID.randomUUID().toString(), name: 'Bogus Dashboard', dashboardPosition: 1)
		WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: 'Bogus Widget')
	}

	void testListGroupsInvalidDataReturnString() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.list()
		assertTrue controller.response.contentAsString.indexOf('List command object has invalid data.') > -1
	}

	void testListGroupsAsNonAdminUser() {
		loginAsUsernameAndRole('bogusUser1', ERoleAuthority.ROLE_USER.strVal)

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.list()

		def respParsed = controller.response.contentAsString
		assertTrue respParsed.indexOf('You are not authorized to see a list of groups in the system.') > -1
	}

	void testListSingleGroup() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.id = Group.findByName('Group1').id as String
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertEquals 'Group1', JSON.parse(controller.response.contentAsString).data.name[0]
		assertEquals Group.findByName('Group1').id, JSON.parse(controller.response.contentAsString).data.id[0]
	}

	void testListSinglePerson() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.user_id = Person.findByUsername('TestUserGroup1').id as String
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertEquals 'Group1', JSON.parse(controller.response.contentAsString).data.name[0]
		assertEquals Group.findByName('Group1').id, JSON.parse(controller.response.contentAsString).data.id[0]
	}

	void testListSingleMissingPerson() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.user_id = (Person.findByUsername('TestUserGroup1').id + 1000) as String
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertTrue JSON.parse(controller.response.contentAsString).data.isEmpty()
		assertEquals 0, JSON.parse(controller.response.contentAsString).results
	}

	void testListGroupsForMissingDashboard() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.dashboard_id = UUID.randomUUID().toString()
		controller.list()

		assertTrue controller.response.contentAsString.indexOf('Requested dashboard not found in database.') > -1
	}

	void testListGroupsForUnMappedDashboard() {
		loginAsAdmin()

		def db = Dashboard.findByName('Bogus Dashboard')

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.dashboard_id = db.guid
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertTrue JSON.parse(controller.response.contentAsString).data.isEmpty()
		assertEquals 0, JSON.parse(controller.response.contentAsString).results
	}

	void testListGroupsForDashboard() {
		loginAsAdmin()

		def g = Group.findByName('Group1')
		def db = Dashboard.findByName('Bogus Dashboard')
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.toString(), destId: db.id, destType: Dashboard.TYPE)

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.dashboard_id = db.guid
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertEquals 'Group1', JSON.parse(controller.response.contentAsString).data[0].name
		assertEquals 1, JSON.parse(controller.response.contentAsString).results
	}

	void testListGroupsForMissingWidget() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.widget_id = UUID.randomUUID().toString()
		controller.list()

		assertTrue controller.response.contentAsString.indexOf('Requested widget not found in database.') > -1
	}

	void testListGroupsForUnMappedWidget() {
		loginAsAdmin()

		def w = WidgetDefinition.findByDisplayName('Bogus Widget')

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.widget_id = w.widgetGuid
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertTrue JSON.parse(controller.response.contentAsString).data.isEmpty()
		assertEquals 0, JSON.parse(controller.response.contentAsString).results
	}

	void testListGroupsForWidget() {
		loginAsAdmin()

		def g = Group.findByName('Group1')
		def w = WidgetDefinition.findByDisplayName('Bogus Widget')
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.toString(), destId: w.id, destType: WidgetDefinition.TYPE)

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.max = 50
		controller.params.offset = 0
		controller.params.sort = 'displayName'
		controller.params.order = 'ASC'
		controller.params.widget_id = w.widgetGuid
		controller.list()

		assertNotNull JSON.parse(controller.response.contentAsString).data
		assertEquals 'Group1', JSON.parse(controller.response.contentAsString).data[0].name
		assertEquals 1, JSON.parse(controller.response.contentAsString).results
	}

	void testCreateGroup() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.data = '[{"name":"GroupCreate","id":0,"description":"","totalWidgets":0,"totalUsers":0,"automatic":false,"status":"active","email":"test@test.com"}]'
		assertNull Group.findByName('GroupCreate')

		controller.createOrUpdate()

		assertNotNull Group.findByName('GroupCreate')
		assertEquals 'GroupCreate', JSON.parse(controller.response.contentAsString).data[0].name
		assertEquals 'test@test.com', JSON.parse(controller.response.contentAsString).data[0].email
	}

	void testCreateGroupInvalidData() {
		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"

		controller.params.data = '[{"name":"","id":0,"description":"","totalWidgets":0,"totalUsers":0,"automatic":false,"status":"active","email":"test@test.com"}]'
		assertNull Group.findByName('GroupCreate')

		controller.createOrUpdate()
		def pData = JSON.parse(controller.response.contentAsString)
		assertTrue pData.errorMsg.contains('Data supplied from the user interface does not properly validate.')
		assertFalse pData.success

		controller.params.data = '[{"id":0,"description":"","totalWidgets":0,"totalUsers":0,"automatic":false,"status":"active","email":"test@test.com"}]'
		assertNull Group.findByName('GroupCreate')

		controller.createOrUpdate()
		def pData1 = JSON.parse(controller.response.contentAsString)
		assertTrue pData1.errorMsg.contains('Data supplied from the user interface does not properly validate.')
		assertFalse pData1.success
	}

	void testUpdateGroup() {
		def name = 'GroupUpdate'
		def grp = buildBasicManualGroup(name)
		loginAsAdmin()

		assertNotNull Group.findByName(name)

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = '[{"name":"GroupChanged","id":' + grp.id + ',"description":"The group is updated","status":"inactive"}]'
		controller.createOrUpdate()

		assertNull Group.findByName(name)
		assertNotNull Group.findByName('GroupChanged')
	}

	void testUpdateGroupInvalidData() {
		def name = 'GroupUpdate'
		def grp = buildBasicManualGroup(name)
		loginAsAdmin()

		assertNotNull Group.findByName(name)

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = '[{"name":"","id":' + grp.id + ',"description":"The group is updated","status":"inactive"}]'
		controller.createOrUpdate()

		def grp1 = Group.findByName(name)
		assertNotNull grp1
		assertEquals 'An updateable group', grp1.description
	}

	void testUpdateGroupDashboards() {
		def name = 'GroupUpdate'
		def grp = buildBasicManualGroup(name)

		def dbCollection = [1, 2].collect {
			Dashboard.build(name: "Dashboard_For_Test_${it}", guid: UUID.randomUUID().toString(), description: "Test dashboard number ${it}", dashboardPosition: it)
		}

		def strJson = (dbCollection as JSON).toString()

		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = strJson
		controller.params.group_id = grp.id
		controller.params.tab = 'dashboards'
		controller.params.update_action = 'add'
		controller.createOrUpdate()

		def pData1 = JSON.parse(controller.response.contentAsString)
		assertTrue pData1.data instanceof JSONArray
		pData1.data.each {
			assertNotNull it.name
			assertTrue it.name.contains('Dashboard_For_Test_')
		}
	}

	void testUpdateRequiresAdmin() {
		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = '[{"name":"GroupCreate","id":0,"description":"","totalWidgets":0,"totalUsers":0,"automatic":false,"status":"active","email":"test@test.com"}]'
		controller.createOrUpdate()

		assertNull Group.findByName('GroupCreate')
		def pData1 = JSON.parse(controller.response.contentAsString)
		assertTrue pData1.errorMsg.contains('You are not authorized to edit groups in the system.')
		assertFalse pData1.success
	}

	void testUpdateGroupUsers() {
		def name = 'GroupUpdate'
		def grp = buildBasicManualGroup(name)

		def uCollection = [1, 2].collect {
			Person.build(username: "Person_For_Test_${it}", userRealName: "Person F. Test${it}")
		}

		def strJson = (uCollection as JSON).toString()

		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = strJson
		controller.params.group_id = grp.id
		controller.params.tab = 'users'
		controller.params.update_action = 'add'
		controller.createOrUpdate()

		def pData1 = JSON.parse(controller.response.contentAsString)
		assertTrue pData1.data instanceof JSONArray
		pData1.data.each {
			assertNotNull it.username
			assertTrue it.username.contains('Person_For_Test_')
		}
	}

	void testUpdateGroupWidgets() {
		def name = 'GroupUpdate'
		def grp = buildBasicManualGroup(name)

		def wCollection = [1, 2].collect {
			WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget Definition ${it}", widgetUrl: 'about:blank',
					imageUrlLarge: 'about:blank', imageUrlSmall: 'about:blank', height: 250, width: 400)
		}

		def strJson = (wCollection as JSON).toString()

		loginAsAdmin()

		controller = new GroupController()
		controller.groupService = groupService
		controller.request.contentType = "text/json"
		controller.params.data = strJson
		controller.params.group_id = grp.id
		controller.params.tab = 'widgets'
		controller.params.update_action = 'add'
		controller.createOrUpdate()

		def pData1 = JSON.parse(controller.response.contentAsString)
		assertTrue pData1.data instanceof JSONArray
		pData1.data.each {
			assertNotNull it.value
			assertTrue it.value.namespace.contains('Widget Definition')
		}
	}

	private Group buildBasicManualGroup(desiredName = null) {
		def grp = Group.build(name: desiredName ?: 'GroupUpdate', description: 'An updateable group', automatic: false, status: 'active')
		return grp
	}
}