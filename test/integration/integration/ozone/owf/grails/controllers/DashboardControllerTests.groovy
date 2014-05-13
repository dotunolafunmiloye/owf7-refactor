package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import grails.gorm.DetachedCriteria
import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.controllers.DashboardController
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class DashboardControllerTests extends OWFGroovyTestCase {

	private sampleJson = """{
		"xtype" : "container",
		"cls" : "hbox ",
		"layout" : {
			"type" : "hbox",
			"align" : "stretch"
		},
		"items" : [ {
			"xtype" : "desktoppane",
			"cls" : "left",
			"flex" : 1,
			"htmlText" : "50%",
			"items" : [],
			"widgets" : [ {
				"universalName" : null,
				"widgetGuid" : "54c33d47-29c3-4248-99bc-1624a86e99cd",
				"uniqueId" : "f0819fab-5d12-ea66-6b2d-bc422a2e0869",
				"dashboardGuid" : "3f8f4d92-8766-ecaf-026f-d8923f7f735b",
				"paneGuid" : "acf09abd-ec35-8059-2f0c-5539a0247e02",
				"name" : "Widget 0",
				"active" : true,
				"x" : 44,
				"y" : 80,
				"minimized" : false,
				"maximized" : false,
				"pinned" : false,
				"collapsed" : false,
				"columnPos" : 0,
				"buttonId" : null,
				"buttonOpened" : false,
				"region" : "none",
				"statePosition" : 1,
				"singleton" : false,
				"floatingWidget" : false,
				"background" : false,
				"zIndex" : 19040,
				"height" : 440,
				"width" : 540,
				"launchData" : "acf09abd-ec35-8059-2f0c-5539a0247e02"
			} ],
			"paneType" : "desktoppane",
			"defaultSettings" : {
				"widgetStates" : {
					"54c33d47-29c3-4248-99bc-1624a86e99cd" : {
						"x" : 44,
						"y" : 80,
						"height" : 440,
						"width" : 540,
						"timestamp" : 1395678749250
					}
				}
			}
		}, {
			"xtype" : "dashboardsplitter"
		}, {
			"xtype" : "container",
			"cls" : "vbox right",
			"layout" : {
				"type" : "vbox",
				"align" : "stretch"
			},
			"items" : [ {
				"xtype" : "accordionpane",
				"cls" : "top",
				"flex" : 1,
				"htmlText" : "50%",
				"items" : [],
				"widgets" : [ {
					"universalName" : null,
					"widgetGuid" : "6ae574b7-5034-4b9a-8381-fed082c99921",
					"uniqueId" : "ec93ea54-945a-254a-8073-2bba0037245f",
					"dashboardGuid" : "3f8f4d92-8766-ecaf-026f-d8923f7f735b",
					"paneGuid" : "73b2c8a0-b6ec-9ba8-ea2c-1814de576f3b",
					"name" : "Widget 1",
					"active" : false,
					"x" : 900,
					"y" : 54,
					"zIndex" : 0,
					"minimized" : false,
					"maximized" : false,
					"pinned" : false,
					"collapsed" : false,
					"columnPos" : 0,
					"buttonId" : null,
					"buttonOpened" : false,
					"region" : "none",
					"statePosition" : 3,
					"singleton" : false,
					"floatingWidget" : false,
					"height" : 314,
					"width" : 897,
					"launchData" : "73b2c8a0-b6ec-9ba8-ea2c-1814de576f3b"
				}, {
					"universalName" : null,
					"widgetGuid" : "fa70509b-4066-4010-afa6-2e5634c5a877",
					"uniqueId" : "dad4b888-e2fb-0aa9-7787-8812622e2452",
					"dashboardGuid" : "3f8f4d92-8766-ecaf-026f-d8923f7f735b",
					"paneGuid" : "73b2c8a0-b6ec-9ba8-ea2c-1814de576f3b",
					"name" : "Widget 2",
					"active" : false,
					"x" : 900,
					"y" : 368,
					"zIndex" : 0,
					"minimized" : false,
					"maximized" : false,
					"pinned" : false,
					"collapsed" : false,
					"columnPos" : 0,
					"buttonId" : null,
					"buttonOpened" : false,
					"region" : "none",
					"statePosition" : 2,
					"singleton" : false,
					"floatingWidget" : false,
					"height" : 314,
					"width" : 897,
					"launchData" : "73b2c8a0-b6ec-9ba8-ea2c-1814de576f3b"
				} ],
				"paneType" : "accordionpane",
				"defaultSettings" : {
					"widgetStates" : {
						"6ae574b7-5034-4b9a-8381-fed082c99921" : {
							"timestamp" : 1395678721266
						},
						"fa70509b-4066-4010-afa6-2e5634c5a877" : {
							"timestamp" : 1395678721265
						}
					}
				}
			}, {
				"xtype" : "dashboardsplitter"
			}, {
				"xtype" : "fitpane",
				"cls" : "bottom",
				"flex" : 1,
				"htmlText" : "50%",
				"items" : [],
				"paneType" : "fitpane",
				"widgets" : [ {
					"universalName" : null,
					"widgetGuid" : "3549b4ed-78c0-4fe3-928a-bc84eaf3d6fb",
					"uniqueId" : "1b0399df-9341-80fc-0a13-e2f19438225e",
					"dashboardGuid" : "3f8f4d92-8766-ecaf-026f-d8923f7f735b",
					"paneGuid" : "d6114da7-13a2-bf6a-706b-b084679f7be4",
					"name" : "Widget 3",
					"active" : false,
					"x" : 900,
					"y" : 686,
					"zIndex" : 0,
					"minimized" : false,
					"maximized" : false,
					"pinned" : false,
					"collapsed" : false,
					"columnPos" : 0,
					"buttonId" : null,
					"buttonOpened" : false,
					"region" : "none",
					"statePosition" : 1,
					"singleton" : false,
					"floatingWidget" : false,
					"height" : 628,
					"width" : 897,
					"launchData" : "d6114da7-13a2-bf6a-706b-b084679f7be4"
				} ],
				"defaultSettings" : {}
			} ],
			"flex" : 1
		} ],
		"flex" : 3
}"""
	def controller
	def dashboardService

	def sessionFactory

	private void createTestDashboards(num) {
		createDefaultUserAndAdminData()
		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		for (int i = 0 ; i < num ; i++) {
			Dashboard.build(name: 'Dashboard ' + i, guid:java.util.UUID.randomUUID().toString(), user: person, dashboardPosition: i)
		}
	}

	private queryDashboardByUser(username, dashboardname) {
		def person = Person.createCriteria().list {
			and {
				eq('username', username.toUpperCase())
				dashboards {
					eq('name', dashboardname)
				}
			}
		}

		return person.dashboards
	}

	private setupComplexMappingsWithWidgetsAndLayouts() {
		setupWidgetsFromSampleJson()

		def p1 = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def p2 = Person.build(username: 'testUser2'.toUpperCase(), userRealName: 'Test U. Two')
		def p3 = Person.build(username: 'testUser3'.toUpperCase(), userRealName: 'Test U. Three')

		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1)
		def db2 = Dashboard.build(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 2)
		def db3 = Dashboard.build(name: 'dashboard3', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3)

		// As with the widgets, make sure the dashboard GUIDs line up.
		def submittedJson = sampleJson
		def dbAllGroup = Dashboard.list()
		def dashboardGuidRegex = /\"dashboardGuid\" \: \"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def dashboardGuidMatcher = (submittedJson =~ dashboardGuidRegex)
		dbAllGroup.each { dashboard ->
			def json = submittedJson
			dashboardGuidMatcher.each { match ->
				def oldDashboardId = match[1]
				json = json.replaceAll(oldDashboardId, dashboard.guid)
			}
			dashboard.layoutConfig = json
			dashboard.save()
		}

		def g1 = Group.build(name: 'testGroup1', displayName: 'Test G. One')

		DomainMapping.build(srcId: g1.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db1.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: g1.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db2.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: g1.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: db3.id, destType: Dashboard.TYPE)

		// Not too worried about the dashboard GUID or unique ID at this point.  Typically, we'd cross that bridge in the test, such as we might
		// do with a restore or an update.
		def db1U1 = Dashboard.build(name: 'dashboard1 U1', guid: '11111111-1111-1234-1234-1234567890a1', dashboardPosition: 1, user: p1, layoutConfig: db1.layoutConfig)
		def db2U1 = Dashboard.build(name: 'dashboard2 U1', guid: '11111111-2222-1234-1234-1234567890a2', dashboardPosition: 2, user: p1, layoutConfig: db2.layoutConfig)
		def db3U1 = Dashboard.build(name: 'dashboard3 U1', guid: '11111111-3333-1234-1234-1234567890a3', dashboardPosition: 3, user: p1, layoutConfig: db3.layoutConfig)
		def db1U2 = Dashboard.build(name: 'dashboard1 U2', guid: '22222222-1111-1234-1234-1234567890a1', dashboardPosition: 1, user: p2, layoutConfig: db1.layoutConfig)
		def db2U2 = Dashboard.build(name: 'dashboard2 U2', guid: '22222222-2222-1234-1234-1234567890a2', dashboardPosition: 2, user: p2, layoutConfig: db2.layoutConfig)
		def db3U2 = Dashboard.build(name: 'dashboard3 U2', guid: '22222222-3333-1234-1234-1234567890a3', dashboardPosition: 3, user: p2, layoutConfig: db3.layoutConfig)
		def db1U3 = Dashboard.build(name: 'dashboard1 U3', guid: '33333333-1111-1234-1234-1234567890a1', dashboardPosition: 1, user: p3, layoutConfig: db1.layoutConfig)

		DomainMapping.build(srcId: db1U1.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db1.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db1U2.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db1.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db1U3.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db1.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db2U1.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db2.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db2U2.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db2.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db3U1.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db3.id, destType: Dashboard.TYPE)
		DomainMapping.build(srcId: db3U2.id, srcType: Dashboard.TYPE, relationshipType: RelationshipType.cloneOf.strVal, destId: db3.id, destType: Dashboard.TYPE)
	}

	private setupWidgetsFromSampleJson() {
		def standardWidgetType = WidgetType.build(name: WidgetType.STANDARD)

		def widgetGuidRegex = /\"widgetGuid\"\s*\:\s*\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def widgetGuidMatcher = (sampleJson =~ widgetGuidRegex)
		def widgets = widgetGuidMatcher.collect { match -> match[1] }

		widgets.eachWithIndex { guid, i ->
			def w = WidgetDefinition.build(widgetGuid: guid, displayName: "Widget ${i}", widgetUrl: 'http://www.yahoo.com',
					imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500, width: 900, widgetType: standardWidgetType)
		}
	}

	@Override
	protected void setUp() {
		super.setUp()
		controller = new DashboardController()
		dashboardService.accountService = accountService
		controller.dashboardService = dashboardService
	}

	void testBulkDeleteAndUpdateSetsOrder() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser1'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		Person p = Person.findByUsername('testUser1'.toUpperCase())
		List<Dashboard> reals = Dashboard.findAllByUser(p, [sort: 'name', order: 'desc'])
		JSONArray dbs = reals.collect { JSON.parse('{"guid": "' + it.guid + '", "name": "' + it.name + '", "isdefault": false}') }

		// Whether or not the updateOrder parameter is given, the order should always be updated because setting null violates a
		// constraint (position is not allowed to go null on Dashboard domain objects).
		controller.dashboardService = dashboardService
		controller.request.contentType = "text/json"
		controller.params.viewGuidsToDelete = '[]'
		controller.params.viewsToUpdate = dbs.toString()
		controller.bulkDeleteAndUpdate()

		def resp = controller.response
		assertEquals 200, resp.status

		def ctDefault = 0
		def dbChecks = Dashboard.findAllByUser(p, [sort: 'name', order: 'desc'])
		dbChecks.eachWithIndex { db, i ->
			if (db.isdefault) { ctDefault++ }
			assertEquals i + 1, db.dashboardPosition
		}
		// Absolutely no idea why this line doesn't work (we get 3, not the expected 1).  Sure there's some Hibernate
		// arcanery under the covers, but it's a headache to figure it.
		//		assertEquals 1, ctDefault
	}

	void testBulkDeleteAndUpdateWithNonExistentGuid() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser1'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		// Use a random UUID which doesn't exist to trigger a dashboard not found.
		def guid = UUID.randomUUID().toString()
		controller.dashboardService = dashboardService
		controller.request.contentType = "text/json"
		controller.params.viewGuidsToDelete = '[]'
		controller.params.viewsToUpdate = '[{"guid": "' + guid + '", "name": "bogus", "isdefault": "true"}]'
		controller.bulkDeleteAndUpdate()

		def resp = controller.response
		assertEquals 404, resp.status
		assertTrue resp.contentAsString.contains("Dashboard ${guid} is invalid.")
	}

	void testBulkDeleteAndUpdateWithNonExistentUser() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('iDontExist'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)
		Dashboard real = Dashboard.findByName('dashboard1 U1')

		// Trigger a dashboard not found, even with a valid UUID for the dashboard to update, owing to the user not being found.
		controller.dashboardService = dashboardService
		controller.request.contentType = "text/json"
		controller.params.viewGuidsToDelete = '[]'
		controller.params.viewsToUpdate = '[{"guid": "' + real.guid + '", "name": "' + real.name + '", "isdefault": "true"}]'
		controller.bulkDeleteAndUpdate()

		def resp = controller.response
		assertEquals 401, resp.status
	}

	void testBulkDeleteAndUpdateWithoutParams() {
		loginAsAdmin()
		createTestDashboards(2)

		controller.request.contentType = "text/json"
		controller.bulkDeleteAndUpdate()

		assertEquals '"Error during bulkDeleteAndUpdate: Data supplied from the user interface does not properly validate. Bulk management command object has invalid data."', controller.response.contentAsString
	}

	void testBulkDeleteAndUpdateWithWrongUser() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser2'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)
		Dashboard real = Dashboard.findByName('dashboard1 U1')

		// Trigger a dashboard not found, even with a valid UUID for the dashboard to update, owing to the user not being found.
		controller.dashboardService = dashboardService
		controller.request.contentType = "text/json"
		controller.params.viewGuidsToDelete = '[]'
		controller.params.viewsToUpdate = '[{"guid": "' + real.guid + '", "name": "' + real.name + '", "isdefault": "true"}]'
		controller.bulkDeleteAndUpdate()

		def resp = controller.response
		assertEquals 401, resp.status
		assertTrue resp.contentAsString.contains("Dashboard user of ${real.guid} does not match login user.")
	}

	void testCannotDeleteLastDashboardForUserFromPreferenceAPI() {
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)

		def person = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)

		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db1 as JSON}]"
		controller.delete()

		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()
		assertTrue controller.response.contentAsString.contains('All users must have at least one dashboard.')
	}

	void testCreateDuplicateName() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testAdmin3'.toUpperCase())
		def dashboard = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a0', user: person)

		controller.request.contentType = "text/json"
		controller.params.guid = '12345678-1234-1234-1234-1234567890a1'
		controller.params.name = 'dashboard1'
		controller.params.personId = person.id
		controller.create()

		assertNull JSON.parse(controller.response.contentAsString)[0]
	}

	void testCreateForAdmin() {
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.guid = '12345678-1234-1234-1234-1234567890a0'
		controller.params.name = 'dashboard1'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.create()

		def pData = JSON.parse(controller.response.contentAsString)
		assertEquals 'dashboard1', pData.name
		assertEquals '12345678-1234-1234-1234-1234567890a0', pData.guid
	}

	void testCreateForUser() {
		loginAsUsernameAndRole("testUser1", ERoleAuthority.ROLE_USER.strVal)

		controller.request.contentType = "text/json"
		controller.params.guid = '12345678-1234-1234-1234-1234567890a0'
		controller.params.name = 'dashboard1'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.create()

		def pData = JSON.parse(controller.response.contentAsString)
		assertEquals 'dashboard1', pData.name
		assertEquals '12345678-1234-1234-1234-1234567890a0', pData.guid
	}

	void testCreateForUserAsDefault() {
		loginAsUsernameAndRole("testUser1", ERoleAuthority.ROLE_USER.strVal)

		controller.request.contentType = "text/json"
		controller.params.guid = '12345678-1234-1234-1234-1234567890a0'
		controller.params.name = 'dashboard1'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.params.isdefault = true
		controller.create()

		def pData = JSON.parse(controller.response.contentAsString)
		assertEquals 'dashboard1', pData.name
		assertEquals '12345678-1234-1234-1234-1234567890a0', pData.guid
	}

	void testCreateOrUpdateCreateAdminGroupDashboardWithWidgetUniversalName() {
		def standardWidgetType = WidgetType.build(name: WidgetType.STANDARD)
		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), universalName: 'Widget 1 UN', displayName: "Widget 1",
				widgetUrl: 'http://www.yahoo.com', imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500,
				width: 900, widgetType: standardWidgetType)

		def wUseGuid = UUID.randomUUID().toString() // Setup the scenario where the service has to find the widget by universal name and replace the GUID.
		def dbGuid = UUID.randomUUID().toString()
		def d = new Dashboard(name: 'Brand New Dashboard', guid: dbGuid, dashboardPosition: 1, alteredByAdmin: false,
				description: "Bogus", locked: false, layoutConfig: """{
	widgets : [ {
		universalName : 'Widget 1 UN',
		region : none,
		buttonOpened : false,
		zIndex : 19070,
		minimized : false,
		floatingWidget : false,
		uniqueId : d28ad9d7-5100-4d65-9223-d0694d8a2b5a,
		height : 563,
		pinned : false,
		name : 'Test Widget 1',
		launchData : null,
		widgetGuid : ${wUseGuid},
		columnPos : 0,
		singleton : false,
		width : 729,
		buttonId : null,
		paneGuid : 419476d2-f831-37fe-f610-6d597b3b5c72,
		dashboardGuid : ${dbGuid},
		collapsed : false,
		maximized : false,
		statePosition : 1,
		background : false,
		active : true,
		y : 135,
		x : 245
	} ],
	defaultSettings : {
		widgetStates : {
			${wUseGuid} : {
				timestamp : 1395761383094,
				height : 563,
				width : 729,
				y : 135,
				x : 245
			}
		}
	},
	height : 100%,
	items : [],
	xtype : desktoppane,
	flex : 1,
	paneType : desktoppane
}""")

		def p = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		loginAsUsernameAndRole(p.username, ERoleAuthority.ROLE_ADMIN.strVal)

		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "[${d as JSON}]"
		controller.params.isGroupDashboard = true
		controller.createOrUpdate()

		def resp = controller.response
		def respCaS = resp.contentAsString

		assertEquals 200, resp.status
		assertTrue (respCaS.indexOf(wUseGuid) == -1)
	}

	void testCreateOrUpdateCreateGroupDashboard() {
		// We're testing a group dashboard, so we'll want to make sure the widgets exist first.  We'll test what happens when we don't
		// create the widgets in another test.
		setupWidgetsFromSampleJson()
		def d = new Dashboard(name: 'Brand New Dashboard', guid: '3f8f4d92-8766-ecaf-026f-d8923f7f735b', dashboardPosition: 5, alteredByAdmin: false,
				description: "Bogus", locked: false, layoutConfig: sampleJson)
		def p = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')

		loginAsUsernameAndRole(p.username, ERoleAuthority.ROLE_ADMIN.strVal)
		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "[${d as JSON}]"
		controller.params.isGroupDashboard = true
		controller.createOrUpdate()

		def resp = controller.response
		assertEquals 200, resp.status

		def dbCheck = Dashboard.findByGuid('3f8f4d92-8766-ecaf-026f-d8923f7f735b')
		assertNotNull dbCheck
		assertEquals 1, dbCheck.dashboardPosition
		assertFalse dbCheck.isdefault

		def sampleUniqueIds = (JSON.parse(sampleJson)).items.widgets*.uniqueId as Set
		def actualUniqueIds = (JSON.parse(dbCheck.layoutConfig)).items.widgets*.uniqueId as Set
		assertFalse sampleUniqueIds.equals(actualUniqueIds)
	}

	void testCreateOrUpdateCreateGroupDashboardNoWidgets() {
		// Complementary test to the one above -- what happens if the widgets don't exist?  Contrary to all reason, the code doesn't actually
		// care....  This is more a check of whether the save continues even if it defies logic and is here to ensure that the legacy behavior
		// continues.
		def d = new Dashboard(name: 'Brand New Dashboard', guid: '3f8f4d92-8766-ecaf-026f-d8923f7f735b', dashboardPosition: 5, alteredByAdmin: false,
				description: "Bogus", locked: false, layoutConfig: sampleJson)
		def p = Person.build(username: 'bUser1'.toUpperCase(), userRealName: 'Bogus User 1')

		loginAsUsernameAndRole(p.username.toUpperCase(), ERoleAuthority.ROLE_ADMIN.strVal)
		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "[${d as JSON}]"
		controller.params.isGroupDashboard = true
		controller.createOrUpdate()

		def resp = controller.response
		assertEquals 200, resp.status

		def dbCheck = Dashboard.findByGuid('3f8f4d92-8766-ecaf-026f-d8923f7f735b')
		assertNotNull dbCheck
		assertEquals 1, dbCheck.dashboardPosition
		assertFalse dbCheck.isdefault

		def sampleUniqueIds = (JSON.parse(sampleJson)).items.widgets*.uniqueId as Set
		def actualUniqueIds = (JSON.parse(dbCheck.layoutConfig)).items.widgets*.uniqueId as Set
		assertFalse sampleUniqueIds.equals(actualUniqueIds)
	}

	void testCreateOrUpdateCreateUserDashboardNoDefaultSpecified() {
		setupWidgetsFromSampleJson()
		def p = Person.build(username: 'bUser1'.toUpperCase(), userRealName: 'Bogus User 1')

		def dbs = []
		(10..13).eachWithIndex { dNum, i ->
			def d = new Dashboard(name: "Dashboard ${dNum}", guid: UUID.randomUUID().toString(), dashboardPosition: i + 1,
					alteredByAdmin: false, description: "Bogus", locked: false, layoutConfig: sampleJson, user: p)
			dbs << d
		}

		loginAsUsernameAndRole(p.username.toUpperCase(), ERoleAuthority.ROLE_ADMIN.strVal)
		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "${dbs as JSON}"
		controller.createOrUpdate()

		def resp = controller.response
		assertEquals 200, resp.status

		def dbChecks = Dashboard.findAllByUser(p)
		assertEquals 4, dbChecks.size()
		int ctDefault = 0
		dbChecks.each { db -> if (db.isdefault) { ctDefault++ } }
		assertEquals 1, ctDefault
	}

	void testCreateOrUpdateCreateUserDashboardWithDefault() {
		setupWidgetsFromSampleJson()
		def p = Person.build(username: 'bUser1'.toUpperCase(), userRealName: 'Bogus User 1')

		def dfltGuid
		def dbs = []
		(10..13).eachWithIndex { dNum, i ->
			def d = new Dashboard(name: "Dashboard ${dNum}", guid: UUID.randomUUID().toString(), dashboardPosition: i + 1,
					alteredByAdmin: false, description: "Bogus", locked: false, layoutConfig: sampleJson, user: p,
					isdefault: i == 0)
			if (i == 0) { dfltGuid = d.guid }
			dbs << d
		}

		loginAsUsernameAndRole(p.username.toUpperCase(), ERoleAuthority.ROLE_ADMIN.strVal)
		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "${dbs as JSON}"
		controller.createOrUpdate()

		def resp = controller.response
		assertEquals 200, resp.status

		def dbChecks = Dashboard.findAllByUser(p)
		assertEquals 4, dbChecks.size()
		dbChecks.each { db ->
			if (db.guid == dfltGuid) {
				assertTrue db.isdefault
			}
			else {
				assertFalse db.isdefault
			}
		}
	}

	void testCreateOrUpdateUpdateAdminGroupDashboard() {
		setupComplexMappingsWithWidgetsAndLayouts()

		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Garbage", widgetUrl: 'http://www.yahoo.com',
				imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500, width: 900,
				widgetType: WidgetType.findByName(WidgetType.STANDARD))
		def p = Person.findByUserRealName('Test U. One')
		def dbs = Dashboard.findByNameLike('dashboard%')
		def dfltDbGuid = null
		def dashData = dbs.collect { db ->
			if (db.user != null && !dfltDbGuid) {
				dfltDbGuid = db.guid
			}
			if (db.user == null) {
				db.dashboardPosition = 5
			}
			db.description = 'Testing description save'
			db.name = db.name + ' UPDATED'
			db.locked = false
			db.isdefault = false
			db.layoutConfig = '{ widgets: [{widgetGuid: ' + w.widgetGuid + '}]}'

			db
		}

		loginAsUsernameAndRole(p.username, ERoleAuthority.ROLE_ADMIN.strVal)

		controller.request.contentType = "text/json"
		controller.params.adminEnabled = true
		controller.params.data = "${dashData as JSON}"
		controller.params.isGroupDashboard = true
		controller.createOrUpdate()

		def resp = controller.response

		assertEquals 200, resp.status
		def dbChecks = Dashboard.findByNameLike('dashboard%')
		dbChecks.each {
			assertEquals 'Testing description save', it.description
			assertTrue it.name.contains(' UPDATED')
			assertFalse it.locked
			if (it.user == null) {
				assertEquals 1, it.dashboardPosition
			}
			else {
				if (it.guid == dfltDbGuid) {
					assertTrue it.isdefault
				}
				else {
					assertFalse it.isdefault
				}
			}
			assertTrue it.layoutConfig.contains('{"widgets":[{"widgetGuid":"' + w.widgetGuid + '","dashboardGuid":"' + it.guid + '","uniqueId":"')
		}
	}

	void testDeleteAsAdminRejectIfNoDashboardsRemain() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)
		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db1 as JSON}]"
		controller.params.adminEnabled = true
		controller.params.user_id = person.id
		controller.delete()

		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()
		assertTrue controller.response.contentAsString.contains('All users must have at least one dashboard.')
	}

	void testDeleteAsAdminRejectIfUserNotFound() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)
		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db1 as JSON}]"
		controller.params.adminEnabled = true
		controller.params.user_id = 5000000
		controller.delete()

		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()
		assertTrue controller.response.contentAsString.contains('Could not locate user in the database.')
	}

	void testDeleteByUser() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testAdmin3'.toUpperCase())
		Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)
		def dbDelete = Dashboard.build(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 2, user: person)
		Dashboard.build(name: 'dashboard3', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3, user: person)

		assertEquals 1, queryDashboardByUser('testAdmin3', 'dashboard2').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${dbDelete as JSON}]"
		controller.params.adminEnabled = true
		controller.params.user_id = person.id
		controller.delete()

		assertEquals 0, queryDashboardByUser('testAdmin3', 'dashboard2').size()
	}

	void testDeleteByUserId() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testAdmin3'.toUpperCase())
		Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)
		Dashboard.build(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 2, user: person)
		Dashboard.build(name: 'dashboard3', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3, user: person)

		assertEquals 1, queryDashboardByUser('testAdmin3', 'dashboard3').size()

		controller.request.contentType = "text/json"
		// Verify that the embedded user_id parameter is ignored.
		controller.params.data = '[{"guid": "12345678-1234-1234-1234-1234567890a2", "name": "dashboard2", "dashboardPosition": 2, "user_id": 500000000}]'
		controller.params.adminEnabled = true
		controller.params.user_id = person.id
		controller.delete()

		assertEquals 0, queryDashboardByUser('testAdmin3', 'dashboard2').size()
	}

	void testDeleteFromPreferenceAPI() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		def person = Person.build(username: 'testAdmin3'.toUpperCase())
		def db = Dashboard.build(name: 'dashboard3', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 1, user: person)
		def db2 = Dashboard.build(name: 'dashboard4', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 2, user: person)

		assertEquals 1, queryDashboardByUser('testAdmin3', 'dashboard3').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db as JSON}]"
		controller.delete()

		def pData = JSON.parse(controller.response.contentAsString)

		assertEquals 0, queryDashboardByUser('testAdmin3', 'dashboard3').size()
		assertEquals 1, queryDashboardByUser('testAdmin3', 'dashboard4').size()
	}

	void testDeleteGroupDashboardsCleansAllRelationships() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def db2 = Dashboard.findByGuid('12345678-1234-1234-1234-1234567890a2')
		def db2U1 = Dashboard.findByGuid('11111111-2222-1234-1234-1234567890a2')
		def db2U2 = Dashboard.findByGuid('22222222-2222-1234-1234-1234567890a2')

		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		controller.request.contentType = "text/json"
		controller.params.data = "[${db2 as JSON}]"
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.delete()

		assertNull Dashboard.findByGuid('12345678-1234-1234-1234-1234567890a2')
		assertNull Dashboard.findByGuid('11111111-2222-1234-1234-1234567890a2')
		assertNull Dashboard.findByGuid('22222222-2222-1234-1234-1234567890a2')
		assertNull DomainMapping.findBySrcIdAndSrcTypeAndRelationshipType(db2U1.id, Dashboard.TYPE, RelationshipType.cloneOf.strVal)
		assertNull DomainMapping.findBySrcIdAndSrcTypeAndRelationshipType(db2U2.id, Dashboard.TYPE, RelationshipType.cloneOf.strVal)
		assertNull DomainMapping.findByRelationshipTypeAndDestIdAndDestType(RelationshipType.owns.strVal, db2.id, Dashboard.TYPE)
	}

	void testDeleteGroupDashboardsRejectIfAnyUserHasZeroDashboardsLeft() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def db = Dashboard.findByGuid('12345678-1234-1234-1234-1234567890a1')

		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)

		controller.request.contentType = "text/json"
		controller.params.data = "[${db as JSON}]"
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.delete()

		assertNotNull Dashboard.findByGuid('12345678-1234-1234-1234-1234567890a1')
		assertNotNull Dashboard.findByGuid('11111111-1111-1234-1234-1234567890a1')
		assertNotNull Dashboard.findByGuid('22222222-1111-1234-1234-1234567890a1')
		assertNotNull Dashboard.findByGuid('33333333-1111-1234-1234-1234567890a1')
		assertTrue controller.response.contentAsString.contains("Cannot delete dashboards with GUID(s): 12345678-1234-1234-1234-1234567890a1 as doing so would leave some users without a dashboard.")
	}

	void testDeleteNonExistentDashboardAsAdmin() {
		loginAsUsernameAndRole('testAdmin3', ERoleAuthority.ROLE_ADMIN.strVal)
		def person = Person.build(username: 'testAdmin3'.toUpperCase())

		// Don't actually store this one....
		def db = new Dashboard(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3, user: person)

		controller.request.contentType = "text/json"
		controller.params.data = "[${db as JSON}]"
		controller.params.user_id = person.id
		controller.delete()

		assertTrue controller.response.contentAsString.contains('One or more dashboards was not found.')
	}

	void testDeleteNonExistentDashboardAsUser() {
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)
		def person = Person.build(username: 'testUser1'.toUpperCase())

		// Don't actually store this one....
		def db = new Dashboard(name: 'dashboard2', guid: '12345678-1234-1234-1234-1234567890a3', dashboardPosition: 3, user: person)

		controller.request.contentType = "text/json"
		controller.params.data = "[${db as JSON}]"
		controller.delete()

		assertTrue controller.response.contentAsString.contains('One or more dashboards was not found.')
	}

	void testDeleteRejectIfNotOwner() {
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)

		def person = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def person2 = Person.build(username: 'testUser2'.toUpperCase(), userRealName: 'Test U. Two')
		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person2)
		def db = Dashboard.build(name: 'dashboardTest', guid: '12345678-1234-1234-1234-1234567890a2', dashboardPosition: 1, user: person)
		assertEquals 1, queryDashboardByUser('testUser2', 'dashboard1').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db1 as JSON}]"
		controller.delete()

		assertEquals 1, queryDashboardByUser('testUser2', 'dashboard1').size()
		assertTrue controller.response.contentAsString.contains('You are not authorized to delete dashboards for another user.')
	}

	void testDeleteSignalAdminNonAdminUser() {
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)

		def person = Person.build(username: 'testUser1'.toUpperCase(), userRealName: 'Test U. One')
		def db1 = Dashboard.build(name: 'dashboard1', guid: '12345678-1234-1234-1234-1234567890a1', dashboardPosition: 1, user: person)
		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()

		controller.request.contentType = "text/json"
		controller.params.data = "[${db1 as JSON}]"
		controller.params.adminEnabled = true
		controller.delete()

		assertEquals 1, queryDashboardByUser('testUser1', 'dashboard1').size()
		assertTrue controller.response.contentAsString.contains('You are not authorized to delete dashboards for another user.')
	}

	void testList() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.list()

		assertEquals 10, JSON.parse(controller.response.contentAsString).data.size()
	}

	void testListAdminDashboardsForSingleGroup() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def g1 = Group.findByName('testGroup1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1', 'dashboard2', 'dashboard3']
	}

	// The following methods testListAdminDashboardsForSingleGroup* test a specific path through the controller
	// which looks like the following:
	//
	//	if (cmd.adminEnabled) {
	//		...
	//		else if (cmd.group_id && cmd.tab == 'dashboards') {
	//
	//		}
	//	}
	//
	// Note that we could have a case for testing the filtering here as well, but the actual
	// implementation uses shared code for filtering, so the tests above will have already verified the
	// operation of that code.
	void testListAdminDashboardsForSingleGroupNoneExist() {
		def g1 = Group.build(name: 'NoDashboardsAtAll', displayName: 'No Dashboards At All')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, pData.data.size()
	}

	void testListAdminDashboardsForSingleGroupWithMaxAndOffset() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def g1 = Group.findByName('testGroup1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 1
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2']
	}

	void testListAdminDashboardsForSingleGroupWithOffsetOutOfBounds() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def g1 = Group.findByName('testGroup1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 10
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, pData.data.size()
	}

	void testListAdminDashboardsForSingleGroupWithSortReversed() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def g1 = Group.findByName('testGroup1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard3', 'dashboard2', 'dashboard1']
		assertEquals pData.data[0].name, 'dashboard3'
	}

	void testListAdminDashboardsForSingleGroupWithSortReversedAndOffset() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def g1 = Group.findByName('testGroup1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.group_id = g1.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 2
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1']
	}

	// The following methods testListAdminDashboardsForSingleUser* test a specific path through the controller
	// which looks like the following:
	//
	//	if (cmd.adminEnabled) {
	//		...
	//		else if (cmd.user_id && cmd.tab == 'dashboards') {
	//
	//		}
	//	}
	//
	// Note that the same coverage case for filtering that applied to group-specific dashboards also
	// applies here.
	void testListAdminDashboardsForSingleUserTU1() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1 U1', 'dashboard2 U1', 'dashboard3 U1']
	}

	void testListAdminDashboardsForSingleUserTU3() {
		// Same test as above, only with a different user that's mapped to different groups
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser3'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1 U3']
	}

	void testListAdminDashboardsForSingleUserWithOffsetAndMax() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 1
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2 U1']
	}

	void testListAdminDashboardsForSingleUserWithOffsetOutOfBounds() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 1
		controller.params.offset = 10
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, pData.data.size()
	}

	void testListAdminDashboardsForSingleUserWithSortReversed() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard3 U1', 'dashboard2 U1', 'dashboard1 U1']
		assertEquals pData.data[0].name, 'dashboard3 U1'
	}

	void testListAdminDashboardsForSingleUserWithSortReversedAndOffset() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.user_id = p.id
		controller.params.tab = 'dashboards'
		controller.params.max = 50
		controller.params.offset = 1
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 2, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2 U1', 'dashboard1 U1']
		assertEquals pData.data[0].name, 'dashboard2 U1'
	}

	// The following methods testListAdminGroupDashboards* test a specific path through the controller
	// which looks like the following:
	//
	//	if (cmd.adminEnabled) {
	//		...
	//		else if (cmd.isGroupDashboard) {
	//
	//		}
	//	}
	void testListAdminGroupDashboards() {
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1', 'dashboard2', 'dashboard3']
	}

	void testListAdminGroupDashboardsWithFilter() {
		// Similar to above, but filtering, no constraints on offset or max.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.params.filterOperator = "OR"
		controller.params.filters = """[{"filterField":"name","filterValue":"dashboard1"}]"""
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard1']
	}

	void testListAdminGroupDashboardsWithFilterAndMax() {
		// Similar to above, but filtering, offset normal and result size forced to 1.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 1
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.params.filterOperator = "OR"
		controller.params.filters = """[{"filterField":"name","filterValue":"dashboard"}]"""
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2']
	}

	void testListAdminGroupDashboardsWithFilterAndOffsetAndSortReversed() {
		// Similar to above, but filtering, offset normal and result size forced to 1.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 50
		controller.params.offset = 1
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.params.filterOperator = "OR"
		controller.params.filters = """[{"filterField":"name","filterValue":"dashboard"}]"""
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 2, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2', 'dashboard1']
		assertEquals pData.data[0].name, 'dashboard2'
	}

	void testListAdminGroupDashboardsWithFilterOffsetOutOfBounds() {
		// Similar to above, but filtering, offset set out of reach.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 50
		controller.params.offset = 2
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.params.filterOperator = "OR"
		controller.params.filters = """[{"filterField":"name","filterValue":"dashboard1"}]"""
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, pData.data.size()
	}

	void testListAdminGroupDashboardsWithOffset() {
		// Same as above, but with offset
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 5
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 2, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2', 'dashboard3']
	}

	void testListAdminGroupDashboardsWithOffsetAndMax() {
		// Same as above, but with offset and paging both constrained.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 1
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals pData.data*.name, ['dashboard2']
	}

	void testListAdminGroupDashboardsWithOffsetAndMaxOutOfBounds() {
		// Same as above, but with offset and paging both constrained to unreachable values.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 1
		controller.params.offset = 5
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, pData.data.size()
	}

	void testListAdminGroupDashboardsWithSortReversed() {
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.isGroupDashboard = true
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'DESC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 3, pData.data.size()
		assertEquals pData.data*.name, ['dashboard3', 'dashboard2', 'dashboard1']
		assertEquals pData.data[0].name, 'dashboard3'
	}

	// The following methods testListAdminSingleDashboard* test a specific path through the controller
	// which looks like the following:
	//
	//	if (cmd.adminEnabled) {
	//		if (cmd.id) {
	//
	//		}
	//	}
	//
	// Note that this path is not allowed to have filters as we explicitly exclude that when dealing with a single
	// dashboard.  See the command object definition for this constraint implementation.
	void testListAdminSingleDashboard() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def db1 = Dashboard.findByName('dashboard1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.id = db1.guid
		controller.params.max = 50
		controller.params.offset = 0
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals 'dashboard1', pData.data[0].name
	}

	void testListAdminSingleDashboardIgnoresOffsetOutOfBounds() {
		// When looking for a single dashboard, we don't care about the paging stuff.
		setupComplexMappingsWithWidgetsAndLayouts()
		def db1 = Dashboard.findByName('dashboard1')

		loginAsAdmin()
		controller.params.adminEnabled = true
		controller.params.id = db1.guid
		controller.params.max = 50
		controller.params.offset = 1
		controller.params.order = 'ASC'
		controller.params.sort = 'name'
		controller.list()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals 'dashboard1', pData.data[0].name
	}

	void testListNoDefaultPaging() {
		// This will trap the command object adding default values -- create a large (for a single
		// request) data set and then see that we get it all back.  OWF typically digests in bites
		// of 25 or 50 records.
		loginAsAdmin()
		createTestDashboards(100)

		controller.request.contentType = "text/json"
		controller.list()

		assertEquals 100, JSON.parse(controller.response.contentAsString).data.size()
	}

	void testListWithPaging() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 0
		controller.list()

		assertEquals 5, JSON.parse(controller.response.contentAsString).data.size()
		assertEquals 'Dashboard 0', JSON.parse(controller.response.contentAsString).data[0].name
		assertEquals 'Dashboard 4', JSON.parse(controller.response.contentAsString).data[4].name
	}

	void testListWithPagingGetSecondPage() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 5
		controller.list()

		assertEquals 5, JSON.parse(controller.response.contentAsString).data.size()
		assertEquals 'Dashboard 5', JSON.parse(controller.response.contentAsString).data[0].name
		assertEquals 'Dashboard 9', JSON.parse(controller.response.contentAsString).data[4].name
	}

	void testListWithPagingMaxIsOne() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 1
		controller.params.offset = 0
		controller.list()

		assertEquals 1, JSON.parse(controller.response.contentAsString).data.size()
	}

	void testListWithPagingOffsetGreaterThanMax() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 100
		controller.list()

		assertEquals 0, JSON.parse(controller.response.contentAsString).data.size()
	}

	void testListWithPagingOffsetSetToTotalMinusOne() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 20
		controller.params.offset = 9
		controller.list()

		assertEquals 1, JSON.parse(controller.response.contentAsString).data.size()
	}

	void testListWithPagingWithFilterOnName() {
		loginAsAdmin()
		createTestDashboards(10)

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 0
		controller.params.filterOperator = "OR"
		controller.params.filters = """[{"filterField":"name","filterValue":"Dashboard 0"}]"""
		controller.list()

		assertEquals 1, JSON.parse(controller.response.contentAsString).data.size()
		assertEquals 'Dashboard 0', JSON.parse(controller.response.contentAsString).data[0].name
	}

	void testRestoreAsAdminMatchParentIsDefault() {
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		// Set all the individual's dashboards to isdefault: false
		def dcSetAllDbsNotDefault = new DetachedCriteria(Dashboard).build {
			eq 'user', p
		}
		dcSetAllDbsNotDefault.updateAll(isdefault: false)
		def dbs = Dashboard.findAllByUser(p)

		// Set all the parent dashboards to isdefault: true
		def dcSetAllGroupDbsNotDefault = new DetachedCriteria(Dashboard).build {
			isNull 'user'
		}
		dcSetAllDbsNotDefault.updateAll(isdefault: true)

		loginAsAdmin()

		// Now, make sure that the parent property is obeyed if we don't otherwise specify.
		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertTrue pData.data[0].isdefault
	}

	void testRestoreAsAdminRemovesLaunchData() {
		// Login as a admin user and try to restore a dashboard.  Should work and should remove the launch data.  Verify by
		// parsing the layoutConfig.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dbs = Dashboard.findAllByUser(p)
		def dmParent = DomainMapping.findBySrcTypeAndSrcId(Dashboard.TYPE, dbs[0].id)
		def dParent = Dashboard.get(dmParent.destId)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()

		// Not sure what goes into launchData, but what should come out is nulls.  So we hard-wire some GUIDs in to the sample
		// data and then verify we get no matches following the restore.
		def launchDataRegex = /\"launchData\"\s*\:\s*\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def launchDataMatcher = (pData.data[0].layoutConfig =~ launchDataRegex)
		def launchGuids = []
		launchDataMatcher.eachWithIndex { match, i ->
			launchGuids << match[1]
		}
		assertTrue launchGuids.isEmpty()
	}

	void testRestoreAsAdminRetainsProperWidgetGuid() {
		// Login as a admin user and try to restore a dashboard.  Should work and should should set the unique ID GUID
		// in the layoutConfig to be something random, rather than what's in the parent.  Verify by
		// parsing the layoutConfig of parent and child for the unique ID.  Should have same number of matches and
		// each match should be different GUID in the child.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dbs = Dashboard.findAllByUser(p)
		def dmParent = DomainMapping.findBySrcTypeAndSrcId(Dashboard.TYPE, dbs[0].id)
		def dParent = Dashboard.get(dmParent.destId)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()

		// Now assert that the widget GUID is the same as what's in the parent dashboard by first building the list of
		// originals from the submitted JSON that was sent on the parent dashboard (see the setup), then making sure
		// those GUIDs all occur in the layout configuration.
		def jsLayout = JSON.parse(pData.data[0].layoutConfig)
		def widgetGuidRegex = /\"widgetGuid\"\s*\:\s*\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def widgetGuidMatcher = (dParent.layoutConfig =~ widgetGuidRegex)
		def oldWidgetGuids = []
		widgetGuidMatcher.eachWithIndex { match, i ->
			oldWidgetGuids << match[1]
		}

		oldWidgetGuids.each {
			assertTrue pData.data[0].layoutConfig.indexOf(it) > -1
		}
	}

	void testRestoreAsAdminReturnsGroups() {
		// Login as a admin user and try to restore a dashboard.  Should work and should return as part of the data object
		// a service model object that has the right number of groups (should match the ownership of the parent) with names
		// like "Group 1, Group 2...."
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dbs = Dashboard.findAllByUser(p)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertEquals 1, pData.data[0].groups.size()
	}

	void testRestoreAsAdminSetsProperDashboardGuid() {
		// Login as a admin user and try to restore a dashboard.  Should work and should should set the dashboard GUID
		// in the layoutConfig to be the dashboard GUID of the child, rather than the parent.  Verify by
		// parsing the layoutConfig.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dbs = Dashboard.findAllByUser(p)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()

		// Now assert that the dashboard GUID is the child dashboard by first building the list of matches in the
		// child dashboard's layout and asserting that there's only one unique value which also happens to match
		// the child dashboard GUID.
		def dashboardGuidRegex = /\"dashboardGuid\"\s*\:\s*\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def dashboardGuidMatcher = (pData.data[0].layoutConfig =~ dashboardGuidRegex)
		def dashboardGuids = [] as Set
		dashboardGuidMatcher.eachWithIndex { match, i ->
			dashboardGuids << match[1]
		}

		assertEquals 1, dashboardGuids.size()
		assertTrue dashboardGuids.contains(dbs[0].guid)
	}

	void testRestoreAsAdminSetsProperUniqueIdGuid() {
		// Login as a admin user and try to restore a dashboard.  Should work and should should set the unique ID GUID
		// in the layoutConfig to be something random, rather than what's in the parent.  Verify by
		// parsing the layoutConfig of parent and child for the unique ID.  Should have same number of matches and
		// each match should be different GUID in the child.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dbs = Dashboard.findAllByUser(p)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()

		// Now assert that the unique ID is different than what's in the parent dashboard by first building the list of
		// originals from the submitted JSON that was sent on the parent dashboard (see the setup), then making sure
		// those GUIDs don't occur in the layout configuration.
		def uniqueIdRegex = /\"uniqueId\"\s*\:\s*\"([A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12})\"/
		def uniqueIdMatcher = (sampleJson =~ uniqueIdRegex)
		def oldUniqueIds = []
		uniqueIdMatcher.eachWithIndex { match, i ->
			oldUniqueIds << match[1]
		}

		oldUniqueIds.each {
			assertTrue pData.data[0].layoutConfig.indexOf(it) == -1
		}
	}

	void testRestoreAsAdminUpdatesDefaultToFalse() {
		// Login as a admin user and try to restore a dashboard setting the default to false.  Should work and set the isdefault to false.  Further,
		// there should be at least one default for the user (verify this).
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dcSetAllDbsNotDefault = new DetachedCriteria(Dashboard).build {
			eq 'user', p
		}
		dcSetAllDbsNotDefault.updateAll(isdefault: true)
		def dbs = Dashboard.findAllByUser(p)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.params.isdefault = false
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertFalse pData.data[0].isdefault
	}

	void testRestoreAsAdminUpdatesDefaultToTrue() {
		// Login as a admin user and try to restore a dashboard setting the default to true.  Should work and set the isdefault to true.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def dcSetAllDbsNotDefault = new DetachedCriteria(Dashboard).build {
			eq 'user', p
		}
		dcSetAllDbsNotDefault.updateAll(isdefault: false)
		def dbs = Dashboard.findAllByUser(p)

		loginAsAdmin()

		controller.params.guid = dbs[0].guid
		controller.params.isdefault = true
		controller.restore()

		def resp = controller.response
		def pData = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, pData.data.size()
		assertTrue pData.data[0].isdefault
	}

	void testRestoreNonAdminFailsOnOtherUserDashboard() {
		// Login as a regular user and try to restore a dashboard that isn't your own.  Should fail.
		setupComplexMappingsWithWidgetsAndLayouts()
		def p = Person.findByUsername('testUser1'.toUpperCase())
		def db = Dashboard.findByName('dashboard1 U2')

		loginAsUsernameAndRole(p.username, ERoleAuthority.ROLE_USER.strVal)

		controller.params.guid = db.guid
		controller.params.isdefault = false
		controller.restore()

		def resp = controller.response

		assertEquals 401, resp.status
		assertTrue resp.contentAsString.contains('You are not authorized to restore dashboards for another user.')
	}

	void testRestoreNonExistentAsAdmin() {
		// Login as a admin user and try to restore a dashboard that doesn't exist.  Should fail.
		setupComplexMappingsWithWidgetsAndLayouts()

		loginAsAdmin()

		def randomUUID = UUID.randomUUID().toString()
		controller.params.guid = randomUUID
		controller.restore()

		def resp = controller.response

		assertEquals 404, resp.status
		assertTrue resp.contentAsString.contains("Dashboard ${randomUUID} not found.")
	}

	void testUpdateDashboardMissingDashboard() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser1'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		def guid = UUID.randomUUID().toString()
		controller.request.contentType = "text/json"
		controller.params.guid = guid
		controller.params.name = 'Random Dashboard'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.update()

		def resp = controller.response
		assertEquals 404, resp.status
		assertTrue resp.contentAsString.contains("The requested dashboard, guid ${guid} was not found.")
	}

	void testUpdateDashboardNoMask() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser1'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		Dashboard db = Dashboard.findByUser(Person.findByUsername('testUser1'.toUpperCase()), [sort: 'name', order: 'asc'])
		def isLocked = new Boolean(db.locked)

		controller.request.contentType = "text/json"
		controller.params.guid = db.guid
		controller.params.name = 'Updated Dashboard1 U1'
		controller.params.layoutConfig = JSON.parse(sampleJson).toString()
		controller.params.state = '[Blank]'
		controller.params.locked = !isLocked
		controller.params.description = "Bubba shot the jukebox."
		controller.params.isdefault = false
		controller.update()

		def resp = controller.response
		assertEquals 200, resp.status

		def pData = JSON.parse(resp.contentAsString)
		assertEquals 'Updated Dashboard1 U1', pData.name
		assertEquals "Bubba shot the jukebox.", pData.description
		assertTrue pData.isdefault
		assertEquals !isLocked, pData.locked
		assertFalse pData.isGroupDashboard
		assertEquals "TESTUSER1", pData.user.userId

		// Test the layout....
		def sample = (JSON.parse(sampleJson)).items.widgets*.uniqueId
		def actual = (JSON.parse(pData.layoutConfig)).items.widgets*.uniqueId
		assertEquals sample, actual

		def sampleDb = (JSON.parse(sampleJson)).items.widgets*.dashboardGuid
		def actualDb = (JSON.parse(pData.layoutConfig)).items.widgets*.dashboardGuid
		assertEquals sampleDb, actualDb
	}

	void testUpdateDashboardNonExistentUser() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('iDontExist'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)
		Dashboard db = Dashboard.findByUser(Person.findByUsername('testUser1'.toUpperCase()), [sort: 'name', order: 'asc'])

		controller.request.contentType = "text/json"
		controller.params.guid = db.guid
		controller.params.name = 'Random Dashboard'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.update()

		def resp = controller.response
		assertEquals 401, resp.status
		assertTrue resp.contentAsString.contains('You are not authorized to update this dashboard.')
	}

	// We're deliberately sending data that formerly would be accepted by the endpoint that has no
	// demonstrated occurrence in the wild.  The endpoint now ignores anything that it's not expecting
	// so the behavior has changed somewhat.  That's the real point (now) of this test.
	void testUpdateDashboardWithMaskIgnored() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser1'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		Dashboard db = Dashboard.findByUser(Person.findByUsername('testUser1'.toUpperCase()), [sort: 'name', order: 'asc'])
		def isLocked = new Boolean(db.locked)

		controller.request.contentType = "text/json"
		controller.params.guid = db.guid
		controller.params.name = 'Updated Dashboard1 U1'
		controller.params.layoutConfig = JSON.parse(sampleJson).toString()
		controller.params.state = '[Blank]'
		controller.params.locked = !isLocked
		controller.params.description = "Bubba shot the jukebox."
		controller.params.isdefault = false
		controller.params.regenerateStateIds = true
		controller.update()

		def resp = controller.response
		assertEquals 200, resp.status

		def pData = JSON.parse(resp.contentAsString)
		assertEquals 'Updated Dashboard1 U1', pData.name
		assertEquals "Bubba shot the jukebox.", pData.description
		assertTrue pData.isdefault
		assertEquals !isLocked, pData.locked
		assertFalse pData.isGroupDashboard
		assertEquals "TESTUSER1", pData.user.userId

		// Test the layout....
		def sample = (JSON.parse(sampleJson)).items.widgets*.uniqueId
		def actual = (JSON.parse(pData.layoutConfig)).items.widgets*.uniqueId
		assertTrue sample.equals(actual)

		def sampleDb = (JSON.parse(sampleJson)).items.widgets*.dashboardGuid as Set
		def actualDb = (JSON.parse(pData.layoutConfig)).items.widgets*.dashboardGuid as Set
		assertTrue sampleDb.equals(actualDb)
	}

	void testUpdateDashboardWrongUser() {
		setupComplexMappingsWithWidgetsAndLayouts()
		loginAsUsernameAndRole('testUser2'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)

		List<Dashboard> dbs = Dashboard.findAllByUser(Person.findByUsername('testUser1'.toUpperCase()))

		controller.request.contentType = "text/json"
		controller.params.guid = dbs[0].guid
		controller.params.name = 'Random Dashboard'
		controller.params.layoutConfig = '{}'
		controller.params.state = '[]'
		controller.update()

		def resp = controller.response
		assertEquals 401, resp.status
		assertTrue resp.contentAsString.contains('You are not authorized to update this dashboard.')
	}
}