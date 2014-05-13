package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.controllers.WidgetDefinitionController
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.DomainMappingService

class WidgetDefinitionControllerTests extends OWFGroovyTestCase {

	def controller
	def widgetDefinitionService

	private String createOrUpdateCreateJson = '''[
		{
			"id":"",
			"name":"Bogus",
			"originalName":"",
			"version":"0.9",
			"description":"A messed up widget.",
			"url":"https://www.yahoo.com",
			"headerIcon":"https://www.yahoo.com/img.gif",
			"image":"https://www.yahoo.com/launch.gif",
			"width":500,
			"height":625,
			"widgetGuid":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
			"universalName":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
			"maximized":"",
			"minimized":"",
			"x":"",
			"y":"",
			"visible":true,
			"definitionVisible":"",
			"background":false,
			"disabled":"",
			"editable":"",
			"tags":[],
			"singleton":false,
			"allRequired":"",
			"directRequired":"",
			"userId":"",
			"userRealName":"",
			"totalUsers":"",
			"totalGroups":"",
			"widgetTypes":[{"id":1,"name":"standard"}],
			"descriptorUrl":"",
			"title":"Bogus",
			"groups":""
		}
	]'''

	private String createOrUpdateUpdateJson = '''[
		{
			"id":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
			"name":"Bogus",
			"originalName":"",
			"version":"0.9.9",
			"description":"A messed up widget.",
			"url":"https://www.yahoo.com",
			"headerIcon":"https://www.yahoo.com/img.gif",
			"image":"https://www.yahoo.com/launch.gif",
			"width":500,
			"height":625,
			"widgetGuid":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
			"universalName":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
			"maximized":false,
			"minimized":false,
			"x":0,
			"y":0,
			"visible":true,
			"definitionVisible":true,
			"background":false,
			"disabled":"",
			"editable":"",
			"tags":[],
			"singleton":false,
			"allRequired":[],
			"directRequired":[],
			"userId":"",
			"userRealName":"",
			"totalUsers":1,
			"totalGroups":4,
			"widgetTypes":[{"id":1,"name":"standard"}],
			"descriptorUrl":null,
			"title":"Bogus",
			"groups":""
		}
	]'''

	private def createWidgetDefinitionForTest() {
		def widgetDefinition = WidgetDefinition.build(
				descriptorUrl : '../examples/fake-widgets/widget-c.json',
				displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980
				)

		return widgetDefinition
	}

	private def createWidgetDefinitionForTest(widgetName, imageUrlLarge, imageUrlSml, guid, widgetUrl, descriptorUrl, universalName) {
		def widgetDefinition = WidgetDefinition.build(
				descriptorUrl : '../examples/fake-widgets/' + descriptorUrl,
				displayName : widgetName,
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/' + imageUrlLarge,
				imageUrlSmall : '../images/blue/icons/widgetContainer/' + imageUrlSml,
				widgetVersion : '1.0',
				widgetGuid : guid,
				universalName : universalName,
				widgetUrl : '../examples/fake-widgets/' + widgetUrl,
				width : 980
				)

		return widgetDefinition
	}

	private void setupWidgetsForDependencyChecks() {
		def g = Group.build(name: 'Bogus', displayName: 'Group Ownership of Widget')
		def wt = WidgetType.build(name: 'standard')
		def w1 = WidgetDefinition.build(displayName: 'Widget 1', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 1', widgetType: wt)
		def w2 = WidgetDefinition.build(displayName: 'Widget 2', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 2', widgetType: wt)
		DomainMapping.build(srcId: w1.id, srcType: WidgetDefinition.TYPE, relationshipType: RelationshipType.requires.strVal, destId: w2.id, destType: WidgetDefinition.TYPE)
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: w2.id, destType: WidgetDefinition.TYPE)
	}

	private void setupWidgetsForRecursiveDependencyChecks() {
		setupWidgetsForDependencyChecks()
		def w2 = WidgetDefinition.findByDisplayName('Widget 2')
		def w3 = WidgetDefinition.build(displayName: 'Widget 3', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 3', widgetType: WidgetType.findByName('standard'))
		DomainMapping.build(srcId: w2.id, srcType: WidgetDefinition.TYPE, relationshipType: RelationshipType.requires.strVal, destId: w3.id, destType: WidgetDefinition.TYPE)
	}

	@Override
	protected void setUp() {
		super.setUp()
		controller = new WidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
	}

	void testCreateOrUpdateWidgetDefinitionAddGroups() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		def g = Group.build(name: 'BogusGroup', displayName: 'Bogus Group')
		def gArray = [g]
		JSONObject objReference = JSON.parse(createOrUpdateUpdateJson)[0]
		def wd = WidgetDefinition.build(widgetGuid: objReference.widgetGuid, displayName: objReference.name,
				widgetUrl: objReference.url, imageUrlSmall: objReference.url, imageUrlLarge: objReference.url,
				width: 201, height: 201, widgetType: wt)

		loginAsAdmin()

		controller.params.data = (gArray as JSON).toString()
		controller.params.tab = 'groups'
		controller.params.update_action = 'add'
		controller.params.widget_id = objReference.widgetGuid
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status
		def dms = DomainMapping.withCriteria {
			eq 'srcId', g.id
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', WidgetDefinition.TYPE
			eq 'destId', wd.id
		}
		assertEquals 1, dms.size()
	}

	void testCreateOrUpdateWidgetDefinitionAddUsers() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		def p = Person.build(username: 'BogusUser', userRealName: 'Bogus User')
		def pArray = [p]
		JSONObject objReference = JSON.parse(createOrUpdateUpdateJson)[0]
		def wd = WidgetDefinition.build(widgetGuid: objReference.widgetGuid, displayName: objReference.name,
				widgetUrl: objReference.url, imageUrlSmall: objReference.url, imageUrlLarge: objReference.url,
				width: 201, height: 201, widgetType: wt)

		loginAsAdmin()

		controller.params.data = (pArray as JSON).toString()
		controller.params.tab = 'users'
		controller.params.update_action = 'add'
		controller.params.widget_id = objReference.widgetGuid
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status
		def pwds = PersonWidgetDefinition.withCriteria {
			eq 'person', p
			eq 'widgetDefinition', wd
		}
		assertEquals 1, pwds.size()
	}

	void testCreateOrUpdateWidgetDefinitionCreate() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		loginAsAdmin()

		controller.params.data = createOrUpdateCreateJson
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status

		def js = JSON.parse(createOrUpdateCreateJson)[0]
		def w = WidgetDefinition.findByWidgetGuid(js.widgetGuid)
		assertNotNull w
		assertEquals w.displayName, js.name
		assertEquals w.widgetUrl, js.url
		assertEquals w.imageUrlSmall, js.headerIcon
		assertEquals w.imageUrlLarge, js.image
		assertEquals w.width, js.width
		assertEquals w.height, js.height
	}

	void testCreateOrUpdateWidgetDefinitionFailsIfNotAdmin() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_USER.strVal)

		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 401, resp.status
		assertTrue resp.contentAsString.contains('You are not authorized to manage widgets.')
	}

	void testCreateOrUpdateWidgetDefinitionLegacyUnusedDataPathFails() {
		loginAsAdmin()

		controller.params.displayName = 'Widget C'
		controller.params.widgetUrl = '../examples/fake-widgets/widget-c.html'
		controller.params.imageUrlSmall = '../images/blue/icons/widgetContainer/widgetCsm.gif'
		controller.params.imageUrlLarge = '../images/blue/icons/widgetIcons/widgetC.gif'
		controller.params.width = 980
		controller.params.height = 740
		controller.params.widgetGuid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.widgetVersion = '1.0'
		controller.params.descriptorUrl = '../examples/fake-widgets/widget-c.json'
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 500, resp.status
		assertTrue resp.contentAsString.contains('Create or update command object has invalid data.')
	}

	void testCreateOrUpdateWidgetDefinitionRemoveGroups() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		def g = Group.build(name: 'BogusGroup', displayName: 'Bogus Group')
		def gArray = [g]
		JSONObject objReference = JSON.parse(createOrUpdateUpdateJson)[0]
		def wd = WidgetDefinition.build(widgetGuid: objReference.widgetGuid, displayName: objReference.name,
				widgetUrl: objReference.url, imageUrlSmall: objReference.url, imageUrlLarge: objReference.url,
				width: 201, height: 201, widgetType: wt)
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destType: WidgetDefinition.TYPE, destId: wd.id)

		loginAsAdmin()

		controller.params.data = (gArray as JSON).toString()
		controller.params.tab = 'groups'
		controller.params.update_action = 'remove'
		controller.params.widget_id = objReference.widgetGuid
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status
		def dms = DomainMapping.withCriteria {
			eq 'srcId', g.id
			eq 'srcType', Group.TYPE
			eq 'relationshipType', RelationshipType.owns.strVal
			eq 'destType', WidgetDefinition.TYPE
			eq 'destId', wd.id
		}
		assertEquals 0, dms.size()
	}

	void testCreateOrUpdateWidgetDefinitionRemoveUsers() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		def p = Person.build(username: 'BogusUser', userRealName: 'Bogus User')
		def pArray = [p]
		JSONObject objReference = JSON.parse(createOrUpdateUpdateJson)[0]
		def wd = WidgetDefinition.build(widgetGuid: objReference.widgetGuid, displayName: objReference.name,
				widgetUrl: objReference.url, imageUrlSmall: objReference.url, imageUrlLarge: objReference.url,
				width: 201, height: 201, widgetType: wt)
		PersonWidgetDefinition.build(person: p, widgetDefinition: wd)

		loginAsAdmin()

		controller.params.data = (pArray as JSON).toString()
		controller.params.tab = 'users'
		controller.params.update_action = 'remove'
		controller.params.widget_id = objReference.widgetGuid
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status
		def pwds = PersonWidgetDefinition.withCriteria {
			eq 'person', p
			eq 'widgetDefinition', wd
		}
		assertEquals 0, pwds.size()
	}

	void testCreateOrUpdateWidgetDefinitionUpdate() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)

		JSONArray arrWidgets = JSON.parse(createOrUpdateUpdateJson)
		JSONObject objReference = arrWidgets[0]
		def wd = WidgetDefinition.build(widgetGuid: objReference.widgetGuid, displayName: objReference.name,
				widgetUrl: objReference.url, imageUrlSmall: objReference.url, imageUrlLarge: objReference.url,
				width: 201, height: 201, widgetType: wt)

		loginAsAdmin()

		controller.params.data = createOrUpdateUpdateJson
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 200, resp.status
		// Additional assertions here about the returned data to confirm the API behaves according to some contract.

		// Now to make sure that the desired update actually happened.
		def wdTest = WidgetDefinition.findByWidgetGuid(objReference.widgetGuid)
		assertEquals wdTest.displayName, objReference.name
		assertEquals wdTest.widgetUrl, objReference.url
		assertEquals wdTest.imageUrlSmall, objReference.headerIcon
		assertEquals wdTest.imageUrlLarge, objReference.image
		assertEquals wdTest.width, objReference.width
		assertEquals wdTest.height, objReference.height
	}

	void testCreateOrUpdateWidgetDefinitionUpdateMissingFails() {
		loginAsAdmin()

		controller.params.data = createOrUpdateUpdateJson
		controller.createOrUpdate()
		def resp = controller.response

		assertEquals 404, resp.status
		assertTrue resp.contentAsString.contains("The requested widget, guid 14af5f76-eaf3-46c2-f6d6-f3fcf62779d3, was not found.")
	}

	void testDeleteExistentWidgetDefinition() {
		loginAsAdmin()
		def wt = WidgetType.build(name: 'standard')
		def w = WidgetDefinition.build(displayName: 'Widget 1', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage', widgetType: wt)

		assertNotNull WidgetDefinition.findByWidgetGuid(w.widgetGuid)

		controller.request.contentType = "text/json"
		controller.params.id = w.widgetGuid
		controller.delete()

		assertEquals w.widgetGuid, JSON.parse(controller.response.contentAsString).data[0].id
		assertNull WidgetDefinition.findByWidgetGuid(w.widgetGuid)
	}

	void testDeleteNonexistentWidgetDefinition() {
		loginAsAdmin()
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html','widget-c.json', 'com.example.widgetc')

		controller.request.contentType = "text/json"
		controller.params.id = '0c5435cf-4021-4f2a-ba69-dde451d12558'
		controller.delete()

		assertNotNull WidgetDefinition.findByWidgetGuid('0c5435cf-4021-4f2a-ba69-dde451d12551')
	}

	void testDependentsBaseCaseEmptyResponse() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName('Widget 1')

		controller.request.contentType = "text/json"
		controller.params.ids = [w.widgetGuid]
		controller.dependents()

		def resp = controller.response
		def respJson = JSON.parse(resp.contentAsString)
		assertEquals 200, resp.status
		assertTrue respJson.data.isEmpty()
	}

	void testDependentsBaseCaseNonEmptyResponse() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName('Widget 2')
		def wTest = WidgetDefinition.findByDisplayName('Widget 1')

		controller.request.contentType = "text/json"
		controller.params.ids = [w.widgetGuid]
		controller.dependents()

		def resp = controller.response
		def respJson = JSON.parse(resp.contentAsString)
		assertEquals 200, resp.status
		assertEquals wTest.widgetGuid, respJson.data[0].id
		assertEquals 0, respJson.data[0].value.totalGroups
		assertEquals 0, respJson.data[0].value.totalUsers
	}

	void testDependentsInvalidData() {
		controller.request.contentType = "text/json"
		controller.params.ids = ['Bogus']
		controller.dependents()

		def resp = controller.response
		def respCaS = resp.contentAsString
		assertEquals 500, resp.status
		assertTrue respCaS.contains('Dependents command object has invalid data.')
	}

	void testDependentsNestedNoRecurse() {
		setupWidgetsForRecursiveDependencyChecks()
		def w = WidgetDefinition.findByDisplayName('Widget 3')
		def wTest = WidgetDefinition.findByDisplayName('Widget 2')

		controller.request.contentType = "text/json"
		controller.params.ids = [w.widgetGuid]
		controller.params.noRecurse = true
		controller.dependents()

		def resp = controller.response
		def respJson = JSON.parse(resp.contentAsString)
		assertEquals 200, resp.status
		assertEquals 1, respJson.data.size()
		assertEquals wTest.widgetGuid, respJson.data[0].id
		assertEquals 0, respJson.data[0].value.totalGroups
		assertEquals 0, respJson.data[0].value.totalUsers
	}

	void testDependentsNestedWithRecurse() {
		setupWidgetsForRecursiveDependencyChecks()
		def w = WidgetDefinition.findByDisplayName('Widget 3')
		def wTest1 = WidgetDefinition.findByDisplayName('Widget 1')
		def wTest2 = WidgetDefinition.findByDisplayName('Widget 2')

		controller.request.contentType = "text/json"
		controller.params.ids = [w.widgetGuid]
		controller.params.noRecurse = false
		controller.dependents()

		def resp = controller.response
		def respJson = JSON.parse(resp.contentAsString)
		assertEquals 200, resp.status
		assertEquals 2, respJson.data.size()
		def guids = respJson.data*.id
		[wTest1, wTest2].each {
			assertTrue guids.contains(it.widgetGuid)
		}
		respJson.data.each {
			assertEquals 0, it.value.totalGroups
			assertEquals 0, it.value.totalUsers
		}
	}

	void testDependentsNonExistent() {
		def wt = WidgetType.build(name: 'standard')
		def w = WidgetDefinition.build(displayName: 'Widget 1', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 1', widgetType: wt)

		controller.request.contentType = "text/json"
		controller.params.ids = [w.widgetGuid]
		controller.dependents()

		def resp = controller.response
		def respJson = JSON.parse(resp.contentAsString)
		assertEquals 200, resp.status
		assertTrue respJson.data.isEmpty()
	}

	void testExport() {
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html','widget-c.json', 'com.example.widgetc')
		def w = WidgetDefinition.findByDisplayName('Widget C')
		def filename = 'test'

		loginAsAdmin()
		controller.request.contentType = "text/json"
		controller.params.id = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.filename = filename
		controller.export()

		def resp = controller.response
		assertEquals "attachment; filename=" + filename + ".json", resp.getHeader("Content-disposition")
		assertEquals resp.contentAsString, (w.asExportableMap() as JSON).toString(true)
	}

	void testExportBadInputData() {
		def guid = UUID.randomUUID().toString()
		def filename = 'test'
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif',guid,'widget-c.html','widget-c.json', 'com.example.widgetc')

		loginAsAdmin()
		controller.request.contentType = "text/json"
		controller.params.id = guid
		controller.export()

		assertEquals 500, controller.response.status
		assertTrue controller.response.contentAsString.contains('Export command object has invalid data.')
	}

	void testExportMissingWidget() {
		def guid = UUID.randomUUID().toString()

		loginAsAdmin()
		controller.request.contentType = "text/json"
		controller.params.id = guid
		controller.params.filename = 'test'
		controller.export()

		assertEquals 404, controller.response.status
		assertTrue controller.response.contentAsString.contains('Widget ' + guid + ' was not found.')
	}

	void testExportNonAdmin() {
		def guid = UUID.randomUUID().toString()
		def filename = 'test'
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif',guid,'widget-c.html','widget-c.json', 'com.example.widgetc')

		loginAsUsernameAndRole('aUser'.toUpperCase(), ERoleAuthority.ROLE_USER.strVal)
		controller.request.contentType = "text/json"
		controller.params.id = guid
		controller.params.filename = filename
		controller.export()

		assertEquals 401, controller.response.status
		assertTrue controller.response.contentAsString.contains('You are not authorized to export widgets.')
	}

	void testListForWidgetDefinition() {
		loginAsAdmin()

		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html','widget-c.json', 'com.example.widgetc')
		createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html','widget-d.json', 'com.example.widgetd')

		controller.request.contentType = "text/json"
		controller.params.widgetName = '%Widget%'
		controller.list()

		assertEquals 2, JSON.parse(controller.response.contentAsString).data.size()
		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).data[0].value.namespace
		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).data[1].value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).data[0].path
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12552', JSON.parse(controller.response.contentAsString).data[1].path
		assertEquals '../examples/fake-widgets/widget-c.json', JSON.parse(controller.response.contentAsString).data[0].value.descriptorUrl
		assertEquals '../examples/fake-widgets/widget-d.json', JSON.parse(controller.response.contentAsString).data[1].value.descriptorUrl
	}

	void testListForWidgetDefinitionByGroupIds() {
		loginAsAdmin()

		def widgetDef1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html',
				'widget-c.json', 'com.example.widgetc')
		def widgetDef2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html',
				'widget-d.json', 'com.example.widgetd')
		def widgetDef3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html',
				'widget-e.json', 'com.example.widgete')

		def group1 = Group.build(name:'Group1',automatic:false,status:'active')
		def group2 = Group.build(name:'Group2',automatic:false,status:'active')
		def group3 = Group.build(name:'Group3',automatic:false,status:'active')

		def domainMappingService = new DomainMappingService()
		domainMappingService.createMapping(group1, RelationshipType.owns, widgetDef1)
		domainMappingService.createMapping(group1, RelationshipType.owns, widgetDef2)
		domainMappingService.createMapping(group1, RelationshipType.owns, widgetDef3)
		domainMappingService.createMapping(group2, RelationshipType.owns, widgetDef2)
		domainMappingService.createMapping(group3, RelationshipType.owns, widgetDef1)
		domainMappingService.createMapping(group3, RelationshipType.owns, widgetDef3)

		controller.request.contentType = "text/json"
		controller.params.groupIds = "['" + group1.id + "','" + group3.id + "']"
		controller.list()

		assertEquals 2, JSON.parse(controller.response.contentAsString).data.size()
		assertTrue(['Widget C', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).data[0].value.namespace,
			JSON.parse(controller.response.contentAsString).data[1].value.namespace] as Set)
	}

	void testListForWidgetDefinitionByUniversalName() {
		loginAsAdmin()

		createWidgetDefinitionForTest()

		controller.request.contentType = "text/json"
		controller.params.universalName = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.list()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).data[0].value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).data[0].value.universalName
		assertEquals '../examples/fake-widgets/widget-c.json', JSON.parse(controller.response.contentAsString).data[0].value.descriptorUrl
	}

	void testShowForExistentWidgetDefinition() {
		loginAsAdmin()
		createWidgetDefinitionForTest()

		controller.request.contentType = "text/json"
		controller.params.widgetGuid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.show()

		def resp = controller.response
		assertEquals 200, resp.status
		assertNotNull WidgetDefinition.findByDisplayNameAndWidgetGuid('Widget C', '0c5435cf-4021-4f2a-ba69-dde451d12551')
		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).data[0].value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).data[0].path
	}

	void testShowForNonexistentWidgetDefinition() {
		loginAsAdmin()
		createWidgetDefinitionForTest()

		def guid = UUID.randomUUID().toString()
		assertNull WidgetDefinition.findByWidgetGuid(guid)

		controller.request.contentType = "text/json"
		controller.params.widgetGuid = guid
		controller.show()

		def resp = controller.response
		def respCaS = resp.contentAsString
		assertEquals 404, resp.status
		assertTrue respCaS.contains("Widget Definition ${guid} not found.")
	}
}
