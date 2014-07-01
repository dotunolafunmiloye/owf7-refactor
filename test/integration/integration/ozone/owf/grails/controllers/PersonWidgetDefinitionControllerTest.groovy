package integration.ozone.owf.grails.controllers

import grails.converters.JSON
import integration.ozone.owf.grails.conf.OWFGroovyTestCase
import ozone.owf.grails.controllers.PersonWidgetDefinitionController
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Intent
import ozone.owf.grails.domain.IntentDataType
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetDefinitionIntent
import ozone.owf.grails.services.DomainMappingService


class PersonWidgetDefinitionControllerTest extends OWFGroovyTestCase {

	def widgetDefinitionService
	def personWidgetDefinitionService
	def serviceModelService
	def controller

	void testWidgetList() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.widgetList()

		assertNotNull JSON.parse(controller.response.contentAsString).rows
		assertEquals "0c5435cf-4021-4f2a-ba69-dde451d12551", JSON.parse(controller.response.contentAsString).rows[0].path
		assertNotNull JSON.parse(controller.response.contentAsString).results
		assertTrue(JSON.parse(controller.response.contentAsString).success)
	}

	void testListUserAndGroupWidgets() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testListUserAndGroupWidgetsByName() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'%C%']
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testListUserAndGroupWidgetsByExactName() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'Widget C']
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testListUserAndGroupWidgetsByVersion() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetVersion:'1.0']
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testListUserAndGroupWidgetsByWidgetGuid() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetGuid:'0c5435cf-4021-4f2a-ba69-dde451d12551']
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testListUserAndGroupWidgetsByUniqueId() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.widgetDefinitionService = widgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [universalName:'com.company.widget.uuid']
		controller.listUserAndGroupWidgets()

		assertTrue JSON.parse(controller.response.contentAsString)*.id.contains("0c5435cf-4021-4f2a-ba69-dde451d12551")
	}

	void testBulkDelete() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.widgetGuidsToDelete = '["0c5435cf-4021-4f2a-ba69-dde451d12551"]'

		controller.bulkDelete()

		assertTrue JSON.parse(controller.response.contentAsString).success
	}

	void testBulkUpdate() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.widgetsToUpdate = '[{"guid":"0c5435cf-4021-4f2a-ba69-dde451d12551", "visible":true}]'

		controller.bulkUpdate()

		assertTrue JSON.parse(controller.response.contentAsString).success
	}

	void testBulkDeleteWithoutParams() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.bulkDelete()
		assertEquals '"Error during bulkDelete: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToDelete param required. Params: [:]"', controller.response.contentAsString
	}

	void testBulkDeleteAndUpdateWithoutParams() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.bulkDeleteAndUpdate()
		assertEquals '"Error during bulkDeleteAndUpdate: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToDelete param required. Params: [:]"', controller.response.contentAsString
	}

	void testBulkUpdateWithoutParams() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.bulkUpdate()
		assertEquals '"Error during bulkUpdate: The requested entity failed to pass validation. A fatal validation error occurred. WidgetsToUpdate param required. Params: [:]"', controller.response.contentAsString
	}

	void testWidgetListWithPaging() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 0

		//default sorts by name
		controller.widgetList()

		assertEquals 5, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
		assertEquals 'Widget 0', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
		assertEquals 'Widget 4', JSON.parse(controller.response.contentAsString).rows[4].value.namespace
	}

	void testWidgetListWithPagingGetSecondPage() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 5

		//default sorts by name
		controller.widgetList()

		assertEquals 5, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
		assertEquals 'Widget 5', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
		assertEquals 'Widget 9', JSON.parse(controller.response.contentAsString).rows[4].value.namespace
	}

	void testWidgetListWithPagingZeroMax() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 0
		controller.params.offset = 0

		//default sorts by name
		controller.widgetList()

		assertEquals 0, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingOffsetGreaterThanMax() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 5
		controller.params.offset = 100

		//default sorts by name
		controller.widgetList()

		assertEquals 0, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingMaxIsOne() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 1
		controller.params.offset = 0

		//default sorts by name
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingOffsetSetToTotalMinusOne() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = 20
		controller.params.offset = 9

		//default sorts by name
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListWithPagingNegativeValuesIgnored() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionsForTest(10)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.params.max = -5
		controller.params.offset = -100

		//default sorts by name
		controller.widgetList()

		assertEquals 10, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 10, JSON.parse(controller.response.contentAsString).results
	}

	void testWidgetListByIntentAction() {
		loginAsUsernameAndRole('testAdmin1', 'role')

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)
		def pwd3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html', 3)

		def intentDataType1 = IntentDataType.build(dataType: "Address")
		def intentDataType2 = IntentDataType.build(dataType: "City")
		def intentDataType3 = IntentDataType.build(dataType: "LongLat")
		def intentDataType4 = IntentDataType.build(dataType: "Id")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1, intentDataType2])
		def intent2 = Intent.build(action: "Plot", dataTypes: [intentDataType2, intentDataType1, intentDataType3])
		def intent3 = Intent.build(action: "Zoom", dataTypes: [intentDataType1, intentDataType3])
		def intent4 = Intent.build(action: "Open", dataTypes: [intentDataType4])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1, intentDataType2], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent2, [intentDataType3, intentDataType2], false, true)
		def widgetDefinitionIntent3 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent4, [intentDataType3], false, true)
		def widgetDefinitionIntent4 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent3, [intentDataType3], false, true)
		def widgetDefinitionIntent5 = createWidgetDefinitionIntentForTest(pwd3.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent6 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent3, [intentDataType1], true, false)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1, widgetDefinitionIntent2, widgetDefinitionIntent6]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent3, widgetDefinitionIntent4]
		pwd3.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent5]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'action':'Pan'}"
		controller.widgetList()

		assertEquals 2, JSON.parse(controller.response.contentAsString).rows.size()
		assertTrue(['Widget C', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).rows[0].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[1].value.namespace] as Set)
	}

	void testWidgetListByIntentDataType() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)
		def pwd3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html', 3)
		def pwd4 = createWidgetDefinitionForTest('Widget F','widgetF.gif','widgetFsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12554','widget-f.html', 4)

		def intentDataType1 = IntentDataType.build(dataType: "Address")
		def intentDataType2 = IntentDataType.build(dataType: "City")
		def intentDataType3 = IntentDataType.build(dataType: "LongLat")
		def intentDataType4 = IntentDataType.build(dataType: "Id")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1])
		def intent2 = Intent.build(action: "Plot", dataTypes: [intentDataType2, intentDataType1, intentDataType3])
		def intent3 = Intent.build(action: "Zoom", dataTypes: [intentDataType1, intentDataType3])
		def intent4 = Intent.build(action: "Open", dataTypes: [intentDataType4])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1, intentDataType2], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent2, [intentDataType3, intentDataType2], false, true)
		def widgetDefinitionIntent3 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent4, [intentDataType3], false, true)
		def widgetDefinitionIntent4 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent3, [intentDataType3], false, true)
		def widgetDefinitionIntent5 = createWidgetDefinitionIntentForTest(pwd3.widgetDefinition,
				intent4, [intentDataType1], true, false)
		def widgetDefinitionIntent6 = createWidgetDefinitionIntentForTest(pwd4.widgetDefinition,
				intent2, [intentDataType1, intentDataType2, intentDataType3], true, false)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1, widgetDefinitionIntent2]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent3, widgetDefinitionIntent4]
		pwd3.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent5]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'dataType':'Address'}"
		controller.widgetList()

		assertEquals 3, JSON.parse(controller.response.contentAsString).rows.size()
		assertTrue(['Widget C', 'Widget F', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).rows[0].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[1].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[2].value.namespace] as Set)
	}

	void testWidgetListByIntentDataTypeAndAction() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		def intentDataType1 = IntentDataType.build(dataType: "Address")
		def intentDataType2 = IntentDataType.build(dataType: "City")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1])
		def intent2 = Intent.build(action: "Plot", dataTypes: [intentDataType1, intentDataType2])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent2, [intentDataType2], false, true)
		def widgetDefinitionIntent3 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent2, [intentDataType1, intentDataType2], false, true)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1, widgetDefinitionIntent2]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent3]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'action':'Plot','dataType':'Address'}"
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
		assertEquals 'Plot', JSON.parse(controller.response.contentAsString).rows[0].value.intents.receive[0].action
	}

	void testWidgetListByIntentSend() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		def intentDataType1 = IntentDataType.build(dataType: "Address")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1])
		def intent2 = Intent.build(action: "Plot", dataTypes: [intentDataType1])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent2, [intentDataType1], false, true)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent2]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'send':'true'}"
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
		assertEquals 'Pan', JSON.parse(controller.response.contentAsString).rows[0].value.intents.send[0].action
	}

	void testWidgetListByIntentReceive() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		def intentDataType1 = IntentDataType.build(dataType: "Address")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1])
		def intent2 = Intent.build(action: "Plot", dataTypes: [intentDataType1])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent2, [intentDataType1], false, true)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent2]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'receive':'true'}"
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
		assertEquals 'Plot', JSON.parse(controller.response.contentAsString).rows[0].value.intents.receive[0].action
	}

	void testWidgetListByIntentSendAndAction() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		def intentDataType1 = IntentDataType.build(dataType: "Address")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent1, [intentDataType1], false, true)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent2]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'action':'Pan','send':'true'}"
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
	}

	void testWidgetListByIntentSendDataTypeAndAction() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)
		def pwd3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html', 3)

		def intentDataType1 = IntentDataType.build(dataType: "Address")
		def intentDataType2 = IntentDataType.build(dataType: "City")

		def intent1 = Intent.build(action: "Pan", dataTypes: [intentDataType1, intentDataType2])
		def intent2 = Intent.build(action: "Zoom", dataTypes: [intentDataType1, intentDataType2])

		def widgetDefinitionIntent1 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent1, [intentDataType1], true, false)
		def widgetDefinitionIntent2 = createWidgetDefinitionIntentForTest(pwd1.widgetDefinition,
				intent2, [intentDataType1], true, false)
		def widgetDefinitionIntent3 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent1, [intentDataType1, intentDataType2], false, true)
		def widgetDefinitionIntent4 = createWidgetDefinitionIntentForTest(pwd2.widgetDefinition,
				intent2, [intentDataType1, intentDataType2], false, true)
		def widgetDefinitionIntent5 = createWidgetDefinitionIntentForTest(pwd3.widgetDefinition,
				intent1, [intentDataType2], false, true)

		pwd1.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent1]
		pwd2.widgetDefinition.widgetDefinitionIntents = [widgetDefinitionIntent3, widgetDefinitionIntent4]

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.intent = "{'action':'Pan','dataType':'Address','receive':'true'}"
		controller.widgetList()

		assertEquals 1, JSON.parse(controller.response.contentAsString).rows.size()
		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).rows[0].value.namespace
	}

	void testWidgetListByGroupIds() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

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

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.groupIds = "['" + group1.id + "','" + group3.id + "']"
		controller.widgetList()

		assertEquals 2, JSON.parse(controller.response.contentAsString).rows.size()
		assertTrue(['Widget C', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).rows[0].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[1].value.namespace] as Set)
	}

	void testWidgetListByTags() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

		def pwd1 = createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		def pwd2 = createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)
		def pwd3 = createWidgetDefinitionForTest('Widget E','widgetE.gif','widgetEsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12553','widget-e.html', 3)

		pwd1.setTags([[name:'tag1',visible:true,position:-1,editable:true],[name:'tag2',visible:true,position:-1,editable:true]])
		pwd2.setTags([[name:'tag3',visible:true,position:-1,editable:true],[name:'tag1',visible:true,position:-1,editable:true]])
		pwd3.setTags([[name:'tag1',visible:true,position:-1,editable:true],[name:'tag2',visible:true,position:-1,editable:true],
			[name:'tag3',visible:true,position:-1,editable:true]])

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.tags = "['tag1','tag3']"
		controller.widgetList()

		assertEquals 2, JSON.parse(controller.response.contentAsString).rows.size()
		assertTrue(['Widget D', 'Widget E'] as Set == [JSON.parse(controller.response.contentAsString).rows[0].value.namespace,
			JSON.parse(controller.response.contentAsString).rows[1].value.namespace] as Set)
	}

	void testListByWidgetNameButNotFound() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'1']
		controller.list()

		System.out.println("ResponseString: " + controller.modelAndView)
		// assertEquals null, JSON.parse(controller.response.contentAsString)[0]
	}

	void testListByWidgetName() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [widgetName:'%C%']
		controller.list()

		assertEquals "0c5435cf-4021-4f2a-ba69-dde451d12551", JSON.parse(controller.response.contentAsString)[0].path
	}

	void testNotAuthorizedToCreateAWidget() {
		loginAsUsernameAndRole('testAdmin1', 'role')
		def person = Person.build();

		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)
		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [guid:'0c5435cf-4021-4f2a-ba69-dde451d12551', personId:"#{person.id}", windowname:"true"]
		controller.create()

		System.out.println("ResponseString: " + controller.modelAndView)
		//assertTrue "\"Error during create: You are not authorized to access this entity. You are not authorized to create widgets for other users.\"".equals(controller.response.contentAsString)
	}

	void testDuplicateWidget() {
		createWidgetDefinitionForTest()
		loginAsUsernameAndRole('testAdmin1', 'role')
		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"
		controller.request.parameters = [guid:'0c5435cf-4021-4f2a-ba69-dde451d12551', windowname:"true"]
		controller.create()

		System.out.println("ResponseString: " + controller.modelAndView)
		//assertTrue "\"Error during create: You are not authorized to access this entity. You are not authorized to create widgets for other users.\"".equals(controller.response.contentAsString)
	}

	void testCreatePersonWidgetDefinition() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

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

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = person.id
		controller.create()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
	}

	void testCreatePersonWidgetDefinitionByUnknownUser() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

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

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = 100000  // Unknown user
		controller.create()

		assertEquals '"Error during create: The requested entity was not found. Person with id of 100000 not found while attempting to create a widget for a user."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testCreatePersonWidgetDefinitionByUnauthorizedUser() {
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

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.personId = person.id // Unauthorized user
		controller.create()

		assertEquals '"Error during create: You are not authorized to access this entity. You are not authorized to create widgets for other users."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testShowPersonWidgetDefinitionByGuid() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.show()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
	}

	void testShowNonexistentPersonWidgetDefinition() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)

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

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.show()

		assertEquals '"Error during show: The requested entity was not found. Widget with guid of 0c5435cf-4021-4f2a-ba69-dde451d12551 not found."', controller.response.contentAsString
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testShowPersonWidgetDefinitionWithUuid() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12551'
		controller.params.universalName = 'com.company.widget.uuid'
		controller.show()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
	}

	void testShowPersonWidgetDefinitionByUniqueIdWithoutUuid() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = 'com.company.widget.uuid'
		controller.show()

		assertEquals '"Error during show: The requested entity was not found. Widget with guid of com.company.widget.uuid not found."', controller.response.contentAsString
	}

	void testShowPersonWidgetDefinitionByUuid() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.universalName = 'com.company.widget.uuid'
		controller.show()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
	}

	void testListPersonWidgetDefinition() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest('Widget C','widgetC.gif','widgetCsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12551','widget-c.html', 1)
		createWidgetDefinitionForTest('Widget D','widgetD.gif','widgetDsm.gif','0c5435cf-4021-4f2a-ba69-dde451d12552','widget-d.html', 2)

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.widgetName = '%Widget%'
		controller.list()

		assertEquals 2, JSON.parse(controller.response.contentAsString).size()
		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString)[0].value.namespace
		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString)[1].value.namespace
	}

	void testUpdatePersonWidgetDefinition() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)

		assertEquals 'Widget C', personWidgetDefinition.widgetDefinition.displayName
		personWidgetDefinition.widgetDefinition.displayName = 'Widget D'

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.personId = person.id
		controller.update()

		assertEquals 'Widget D', JSON.parse(controller.response.contentAsString).value.namespace
		assertNotSame 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
	}

	void testUpdateNonexistentPersonWidgetDefinition() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.findByWidgetGuid('0c5435cf-4021-4f2a-ba69-dde451d12551')

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = '0c5435cf-4021-4f2a-ba69-dde451d12559'
		controller.params.personId = person.id
		controller.update()

		assertEquals '"Error during update: The requested entity was not found. Widget 0c5435cf-4021-4f2a-ba69-dde451d12559 not found."', controller.response.contentAsString
		assertNotNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuidAndUsername() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)
		def widgetDefinition = personWidgetDefinition.widgetDefinition

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.username = person.username
		controller.params.adminEnabled = true
		controller.delete()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuidAndPersonId() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def personWidgetDefinition = PersonWidgetDefinition.findByPerson(person)
		def widgetDefinition = personWidgetDefinition.widgetDefinition

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = personWidgetDefinition.widgetDefinition.widgetGuid
		controller.params.personId = person.id
		controller.params.adminEnabled = true
		controller.delete()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinitionAndPerson(widgetDefinition, person)
	}

	void testDeletePersonWidgetDefinitionByWidgetGuid() {
		loginAsUsernameAndRole('testAdmin1', ERoleAuthority.ROLE_ADMIN.strVal)
		createWidgetDefinitionForTest()

		def widgetDefinition = WidgetDefinition.findByDisplayName('Widget C')

		controller = new PersonWidgetDefinitionController()
		controller.personWidgetDefinitionService = personWidgetDefinitionService
		controller.request.contentType = "text/json"

		controller.params.guid = widgetDefinition.widgetGuid
		controller.delete()

		assertEquals 'Widget C', JSON.parse(controller.response.contentAsString).value.namespace
		assertEquals '0c5435cf-4021-4f2a-ba69-dde451d12551', JSON.parse(controller.response.contentAsString).path
		assertNull PersonWidgetDefinition.findByWidgetDefinition(widgetDefinition)
	}

	void createWidgetDefinitionForTest() {
		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : 'Widget C',
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
				imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
				widgetGuid : '0c5435cf-4021-4f2a-ba69-dde451d12551',
				universalName : 'com.company.widget.uuid',
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/widget-c.html',
				width : 980)
		def personWidgetDefinition = PersonWidgetDefinition.build(person: person,
				widgetDefinition: widgetDefinition,
				visible : true,
				pwdPosition: 1)
	}

	def createWidgetDefinitionForTest(widgetName, imageUrlLarge, imageUrlSml, guid, widgetUrl, pwdPosition) {
		def person = Person.findByUsername('testAdmin1'.toUpperCase())
		if (!person) person = Person.build(username: 'testAdmin1'.toUpperCase())
		def widgetDefinition = WidgetDefinition.build(displayName : widgetName,
				height : 740,
				imageUrlLarge : '../images/blue/icons/widgetIcons/' + imageUrlLarge,
				imageUrlSmall : '../images/blue/icons/widgetContainer/' + imageUrlSml,
				widgetGuid : guid,
				universalName : guid,
				widgetVersion : '1.0',
				widgetUrl : '../examples/fake-widgets/' + widgetUrl,
				width : 980)
		def personWidgetDefinition = PersonWidgetDefinition.build(person: person,
				widgetDefinition: widgetDefinition,
				visible : true,
				pwdPosition: pwdPosition)

		widgetDefinition.save(flush:true)
		return personWidgetDefinition
	}

	void createWidgetDefinitionsForTest(numWidgetDefinitions) {
		def person = Person.findByUsername('testAdmin1'.toUpperCase())

		for (int i = 0 ; i < numWidgetDefinitions ; i++) {
			def widgetDefinition = WidgetDefinition.build(displayName : 'Widget '+i,
					height : 740,
					imageUrlLarge : '../images/blue/icons/widgetIcons/widgetC.gif',
					imageUrlSmall : '../images/blue/icons/widgetContainer/widgetCsm.gif',
					widgetGuid : java.util.UUID.randomUUID().toString(),
					universalName : java.util.UUID.randomUUID().toString(),
					widgetVersion : '1.0',
					widgetUrl : '../examples/fake-widgets/widget-c.html',
					width : 980)
			def personWidgetDefinition = PersonWidgetDefinition.build(person: person,
					widgetDefinition: widgetDefinition,
					visible : true,
					pwdPosition: i)

		}
	}

	def createWidgetDefinitionIntentForTest(widgetDefinition, intent, dataTypes, send, receive) {
		def widgetDefinitionIntent = WidgetDefinitionIntent.build(
				widgetDefinition : widgetDefinition,
				intent : intent,
				dataTypes : dataTypes,
				send : send,
				receive : receive)

		widgetDefinitionIntent.save(flush:true)
	}
}
