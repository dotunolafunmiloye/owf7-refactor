package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase
import ozone.owf.grails.controllers.PersonWidgetDefinitionController
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.DomainMappingService

class PersonWidgetDefinitionControllerTest extends OWFGroovyTestCase {

	def personWidgetDefinitionService
	def serviceModelService
	def controller

	@Override
	protected void setUp() {
		super.setUp()
		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
	}

	private PersonWidgetDefinition createWidgetDefinitionForTest(widgetName, imageUrlLarge, imageUrlSml, guid, widgetUrl, pwdPosition) {
		createDefaultUserAndAdminData()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(
				displayName: widgetName,
				height: 740,
				imageUrlLarge: '../images/blue/icons/widgetIcons/' + imageUrlLarge,
				imageUrlSmall: '../images/blue/icons/widgetContainer/' + imageUrlSml,
				widgetGuid: guid,
				universalName: guid,
				widgetVersion: '1.0',
				widgetUrl: '../examples/fake-widgets/' + widgetUrl,
				width: 980,
				widgetType: WidgetType.findByName('standard')
				)
		def personWidgetDefinition = PersonWidgetDefinition.build(
				person: person,
				widgetDefinition: widgetDefinition,
				visible: true,
				pwdPosition: pwdPosition
				)

		return personWidgetDefinition
	}

	private void createWidgetDefinitionsForTest(numWidgetDefinitions) {
		createDefaultUserAndAdminData()
		def person = Person.findByUsername('testAdmin1'.toUpperCase())

		def r = 1..numWidgetDefinitions
		r.each { i ->
			def widgetDefinition = WidgetDefinition.build(
					displayName:		'Widget C' + i,
					height: 			740,
					imageUrlLarge:		'../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall:		'../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid:			UUID.randomUUID().toString(),
					universalName:		UUID.randomUUID().toString(),
					widgetVersion:		'1.0',
					widgetUrl:			'../examples/fake-widgets/widget-c.html',
					width:				980,
					widgetType: 		WidgetType.findByName('standard')
					)

			PersonWidgetDefinition.build(
					person:				person,
					widgetDefinition:	widgetDefinition,
					visible:			true,
					pwdPosition:		i
					)
		}
	}

	void testBulkDelete() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.widgetGuidsToDelete = '["' + widgetToFind.widgetGuid + '"]'
		controller.bulkDelete()

		assertTrue JSON.parse(controller.response.contentAsString).success
	}

	void testBulkDeleteAndUpdateWithoutParams() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.bulkDeleteAndUpdate()

		assertEquals '"Error during bulkDeleteAndUpdate: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToDelete param required. Params: [:]"', controller.response.contentAsString
	}

	void testBulkDeleteWithoutParams() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.bulkDelete()

		assertEquals '"Error during bulkDelete: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToDelete param required. Params: [:]"', controller.response.contentAsString
	}

	void testBulkUpdate() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.widgetsToUpdate = '[{"guid":"' + widgetToFind.widgetGuid + '", "visible":true}]'
		controller.bulkUpdate()

		assertTrue JSON.parse(controller.response.contentAsString).success
	}

	void testBulkUpdateWithoutParams() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.bulkUpdate()
		assertEquals '"Error during bulkUpdate: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToUpdate param required. Params: [:]"', controller.response.contentAsString
	}

	void testCreatePersonWidgetDefinition() {
		createDefaultUserAndAdminData()
		loginAsAdmin()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)

		controller.request.contentType = "text/json"
		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = person.id
		controller.create()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
	}

	void testCreatePersonWidgetDefinitionByUnauthorizedUser() {
		createDefaultUserAndAdminData()
		loginAsUsernameAndRole('testUser1', ERoleAuthority.ROLE_USER.strVal)

		def person = Person.findByUsername('testUser2'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)

		controller.request.contentType = "text/json"
		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = person.id // Unauthorized user
		controller.create()

		assertEquals '"Error during create: You are not authorized to access this entity. You are not authorized to create widgets for other users."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testCreatePersonWidgetDefinitionByUnknownUser() {
		createDefaultUserAndAdminData()
		loginAsAdmin()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)

		controller.request.contentType = "text/json"
		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = 100000  // Unknown user
		controller.create()

		assertEquals '"Error during create: The requested entity was not found. Person with id of 100000 not found while attempting to create a widget for a user."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuid() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		def widgetDefinition = WidgetDefinition.findByDisplayName('Widget C1')

		controller.request.contentType = "text/json"
		controller.params.guid = widgetDefinition.widgetGuid
		controller.delete()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinition(widgetDefinition)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuidAndPersonId() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)
		def widgetDefinition = personWidgetDefinition.widgetDefinition

		controller.request.contentType = "text/json"
		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.personId = person.id
		controller.params.adminEnabled = true
		controller.delete()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuidAndUsername() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)
		def widgetDefinition = personWidgetDefinition.widgetDefinition

		controller.request.contentType = "text/json"
		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.username = person.username
		controller.params.adminEnabled = true
		controller.delete()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	// FIXME: Where's the beef?  This should have an assertion somewhere.
	void testDuplicateWidget() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.request.parameters = [guid:'0c5435cf-4021-4f2a-ba69-dde451d12551', windowname:"true"]
		controller.create()
	}

	void testListByWidgetName() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'% C1%']
		controller.list()

		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString)[0].path
	}

	// FIXME: Where's the beef?  This should have an assertion somewhere.
	void testListByWidgetNameButNotFound() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'1']
		controller.list()
	}

	void testListPersonWidgetDefinition() {
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		loginAsAdmin()
		def person = accountService.getLoggedInUser()

		controller.request.contentType = "text/json"
		controller.params.widgetName = '%Widget%'
		controller.list()

		def results = JSON.parse(controller.response.contentAsString)

		assertEquals 2, results.size()
		assertEquals 'Widget C', results[0].value.namespace
		assertEquals 'Widget D', results[1].value.namespace
	}

	void testListUserAndGroupWidgets() {
		createWidgetDefinitionsForTest(7)

		// Create some additional definitions beyond those specified for the user to see if we get them all or just
		// the user ones....
		def r = 10..12
		r.each { i ->
			def widgetDefinition = WidgetDefinition.build(
					displayName:		'Widget C' + i,
					height: 			740,
					imageUrlLarge:		'../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall:		'../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid:			UUID.randomUUID().toString(),
					universalName:		UUID.randomUUID().toString(),
					widgetVersion:		'1.0',
					widgetUrl:			'../examples/fake-widgets/widget-c.html',
					width:				980,
					widgetType: 		WidgetType.findByName('standard')
					)
		}

		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.listUserAndGroupWidgets()

		def resp = controller.response
		def respCaS = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 7, respCaS.size()
		assertTrue respCaS*.value.namespace.contains('Widget C1')
	}

	void testListUserAndGroupWidgetsBadData() {
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.widgetGuid = 'GARBAGE'
		controller.listUserAndGroupWidgets()

		def resp = controller.response
		def respCaS = resp.contentAsString

		assertEquals 500, resp.status
		assertTrue respCaS.contains('List user and group widgets command object has invalid data.')
	}

	void testListUserAndGroupWidgetsByGuid() {
		createWidgetDefinitionsForTest(7)

		// Create some additional definitions beyond those specified for the user to see if we get them all or just
		// the user ones....
		def r = 10..12
		r.each { i ->
			def widgetDefinition = WidgetDefinition.build(
					displayName:		'Widget C' + i,
					height: 			740,
					imageUrlLarge:		'../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall:		'../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid:			UUID.randomUUID().toString(),
					universalName:		UUID.randomUUID().toString(),
					widgetVersion:		'1.0',
					widgetUrl:			'../examples/fake-widgets/widget-c.html',
					width:				980,
					widgetType: 		WidgetType.findByName('standard')
					)
		}

		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.widgetGuid = WidgetDefinition.findByDisplayName('Widget C1').widgetGuid
		controller.listUserAndGroupWidgets()

		def resp = controller.response
		def respCaS = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, respCaS.size()
		assertEquals 'Widget C1', respCaS[0].value.namespace
	}

	void testListUserAndGroupWidgetsByName() {
		createWidgetDefinitionsForTest(7)

		// Create some additional definitions beyond those specified for the user to see if we get them all or just
		// the user ones....
		def r = 10..12
		r.each { i ->
			def widgetDefinition = WidgetDefinition.build(
					displayName:		'Widget C' + i,
					height: 			740,
					imageUrlLarge:		'../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall:		'../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid:			UUID.randomUUID().toString(),
					universalName:		UUID.randomUUID().toString(),
					widgetVersion:		'1.0',
					widgetUrl:			'../examples/fake-widgets/widget-c.html',
					width:				980,
					widgetType: 		WidgetType.findByName('standard')
					)
		}

		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.widgetName = WidgetDefinition.findByDisplayName('Widget C1').displayName
		controller.listUserAndGroupWidgets()

		def resp = controller.response
		def respCaS = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 1, respCaS.size()
		assertEquals 'Widget C1', respCaS[0].value.namespace
	}

	void testListUserAndGroupWidgetsUnOwnedByGuid() {
		createWidgetDefinitionsForTest(7)

		// Create some additional definitions beyond those specified for the user to see if we get them all or just
		// the user ones....
		def r = 10..12
		r.each { i ->
			def widgetDefinition = WidgetDefinition.build(
					displayName:		'Widget C' + i,
					height: 			740,
					imageUrlLarge:		'../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall:		'../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid:			UUID.randomUUID().toString(),
					universalName:		UUID.randomUUID().toString(),
					widgetVersion:		'1.0',
					widgetUrl:			'../examples/fake-widgets/widget-c.html',
					width:				980,
					widgetType: 		WidgetType.findByName('standard')
					)
		}

		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.widgetGuid = WidgetDefinition.findByDisplayName('Widget C10').widgetGuid
		controller.listUserAndGroupWidgets()

		def resp = controller.response
		def respCaS = JSON.parse(resp.contentAsString)

		assertEquals 200, resp.status
		assertEquals 0, respCaS.size()
	}

	// FIXME: Where's the beef?  This should have an assertion somewhere.
	void testNotAuthorizedToCreateAWidget() {
		loginAsAdmin()
		def person = Person.build()

		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)

		controller.request.contentType = "text/json"
		controller.request.parameters = [guid:'0c5435cf-4021-4f2a-ba69-dde451d12551', personId:"#{person.id}", windowname:"true"]
		controller.create()
	}

	void testShowNonexistentPersonWidgetDefinition() {
		loginAsAdmin()

		def person = Person.findByUsername('testUser1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)

		controller.request.contentType = "text/json"
		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.show()

		assertEquals '"Error during show: The requested entity was not found. Widget with guid of 0c5435cf-4021-4f2a-ba69-dde451d12551 not found."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testShowPersonWidgetDefinitionByGuid() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.guid = widgetToFind.widgetGuid
		controller.show()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
	}

	void testShowPersonWidgetDefinitionByUniqueIdWithoutUuid() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.guid = widgetToFind.universalName
		controller.show()

		assertEquals "\"Error during show: The requested entity was not found. Widget with guid of ${widgetToFind.universalName} not found.\"", controller.response.contentAsString
	}

	void testShowPersonWidgetDefinitionByUuid() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.universalName = widgetToFind.universalName
		controller.show()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
	}

	void testShowPersonWidgetDefinitionWithUuid() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.params.guid = widgetToFind.widgetGuid
		controller.params.universalName = widgetToFind.universalName
		controller.show()

		assertEquals widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals widgetToFind.widgetGuid, JSON.parse(controller.response.contentAsString).path
	}

	void testUpdateNonexistentPersonWidgetDefinition() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.findByWidgetGuid(widgetToFind.widgetGuid)

		controller.request.contentType = "text/json"
		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12559'
		controller.params.personId = person.id
		controller.update()

		assertEquals '"Error during update: The requested entity was not found. Widget 0c5435cf-4021-4f2a-ba69-dde451d12559 not found."', controller.response.contentAsString
		assertNotNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testUpdatePersonWidgetDefinition() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)

		assertEquals widgetToFind.displayName, personWidgetDefinition.widgetDefinition.displayName
		personWidgetDefinition.widgetDefinition.displayName = 'Widget D'

		controller.request.contentType = "text/json"
		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.personId = person.id
		controller.update()

		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).value.namespace
		assertNotSame widgetToFind.displayName, JSON.parse(controller.response.contentAsString).value.namespace
	}

	void testWidgetList() {
		createWidgetDefinitionsForTest(7)
		loginAsAdmin()

		def widgetToFind = WidgetDefinition.findByDisplayName('Widget C1')
		assertNotNull widgetToFind

		controller.request.contentType = "text/json"
		controller.widgetList()

		def json=JSON.parse(controller.response.contentAsString)

		assertNotNull json.rows
		assertTrue widgetToFind.widgetGuid in json.rows*.path
		assertNotNull json.results
		assertTrue json.success

		assertTrue("Small icon url points to cached image for ${json.rows[0].value.smallIconUrl}",
				json.rows[0].value.smallIconUrl.matches('widget/.*/image/imageUrlSmall'))
		assertTrue("Large icon url points to cached image for ${json.rows[0].value.largeIconUrl}",
				json.rows[0].value.largeIconUrl.matches('widget/.*/image/imageUrlLarge'))
	}

	void testWidgetListByGroupIds() {
		loginAsAdmin()

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)
		def pwd3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html', 3)

		def group1 = Group.build(name:'Group1',automatic:false,status:'active')
		def group2 = Group.build(name:'Group2',automatic:false,status:'active')
		def group3 = Group.build(name:'Group3',automatic:false,status:'active')

		def domainMappingService = new DomainMappingService()
		domainMappingService.createMapping(group1, RelationshipType.owns, pwd1.widgetDefinition)
		domainMappingService.createMapping(group1, RelationshipType.owns, pwd2.widgetDefinition)
		domainMappingService.createMapping(group1, RelationshipType.owns, pwd3.widgetDefinition)
		domainMappingService.createMapping(group2, RelationshipType.owns, pwd2.widgetDefinition)
		domainMappingService.createMapping(group3, RelationshipType.owns, pwd1.widgetDefinition)
		domainMappingService.createMapping(group3, RelationshipType.owns, pwd3.widgetDefinition)

		controller.request.contentType = "text/json"
		controller.params.groupIds = "['" + group1.id + "','" + group3.id + "']"
		controller.widgetList()

		assertEquals 2, JSON.parse(controller.response.contentAsString).rows.size()
		assertTrue(['Widget C', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).rows[0].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[1].value.namespace] as Set)
	}

	void testWidgetListWithPaging() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		def widgetNames = (WidgetDefinition.findAllByDisplayNameIlike('Widget C%'))*.displayName

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 0
		controller.widgetList()

		assertEquals 5, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results

		def resultNames = JSON.parse(controller.response.contentAsString).rows*.value*.namespace
		assertEquals resultNames[0], widgetNames[0]
		assertEquals resultNames[4], widgetNames[4]
	}

	void testWidgetListWithPagingGetSecondPage() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		def widgetNames = (WidgetDefinition.findAllByDisplayNameIlike('Widget C%'))*.displayName

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 5
		controller.widgetList()

		assertEquals 5, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results

		def resultNames = JSON.parse(controller.response.contentAsString).rows*.value*.namespace
		assertEquals resultNames[0], widgetNames[5]
		assertEquals resultNames[4], widgetNames[9]
	}

	void testWidgetListWithPagingMaxIsOne() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.max = 1
		controller.params.offset = 0
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingNegativeValuesIgnored() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.max = -5
		controller.params.offset = -100
		controller.widgetList()

		assertEquals 10, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingOffsetGreaterThanMax() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 100
		controller.widgetList()

		assertEquals 0, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingOffsetSetToTotalMinusOne() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.max = 20
		controller.params.offset = 9
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingZeroMax() {
		createWidgetDefinitionsForTest(10)
		loginAsAdmin()

		controller.request.contentType = "text/json"
		controller.params.max = 0
		controller.params.offset = 0
		controller.widgetList()

		assertEquals 0, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

}
