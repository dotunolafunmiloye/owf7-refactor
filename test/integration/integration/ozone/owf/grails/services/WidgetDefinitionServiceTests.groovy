package integration.ozone.owf.grails.services

import grails.converters.JSON
import integration.WidgetDefinitionPostParams
import integration.ozone.owf.grails.conf.DataClearingTestCase

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.AutoLoginAccountService

class WidgetDefinitionServiceTests extends DataClearingTestCase {

	def widgetDefinitionService
	def personWidgetDefinitionService

	private final samplesArray = ["A","D","C","AA","B","BB"]

	public void setUp() {
		super.setUp()

		def acctService = new AutoLoginAccountService()
		Person p = new Person(username:'testUserWidgetDefinitionServiceTesting'.toUpperCase(), userRealName: 'foo', enabled:true)
		p.save()
		acctService.autoAccountName = 'testUserWidgetDefinitionServiceTesting'.toUpperCase()
		acctService.autoRoles = [ERoleAuthority.ROLE_ADMIN.strVal]
		widgetDefinitionService.accountService = acctService
		personWidgetDefinitionService.accountService = acctService
	}

	void testUpdate() {
		def postParamsA = WidgetDefinitionPostParams.generatePostParamsA()
		def resultOfCreate = WidgetDefinition.build(postParamsA)

		def postParamsB = WidgetDefinitionPostParams.generatePostParamsB()
		postParamsB.id = postParamsB.widgetGuid
		//TODO createOrUpdate has bee relegated to the controller, so this test should be recast as controller test
		//def resultOfUpdate = widgetDefinitionService.createOrUpdate(postParamsB)

		//assertTrue resultOfUpdate.success
		//assertEquals 1, WidgetDefinition.findAll().size()

		def widgetDefinition = WidgetDefinition.findByWidgetGuid("12345678-1234-1234-1234-1234567890a0")
		//assertEquals "My Widget Updated", widgetDefinition.displayName
	}

	void testListWithStartAndLimit() {
		createDataForListTests()
		def expectedOrder = samplesArray

		def widgets = widgetDefinitionService.list([offset: "4", max: "1"])
		assertEquals expectedOrder[4], widgets.data[0].displayName
	}

	void testListWithSortAndDir() {
		createDataForListTests()
		def expectedOrder = ["D","C","BB","B","AA","A"]

		def widgets = widgetDefinitionService.list([sort: 'value.namespace', order: 'desc'])

		assertEquals expectedOrder, widgets.data*.displayName
	}

	void testListWithSortAndDirAndStartAndLimit() {
		createDataForListTests()
		def expectedOrder = ["A", "AA", "B", "BB", "C", "D"]

		def widgets = widgetDefinitionService.list([offset: "4", max: "1", sort: 'value.namespace', order: 'asc'])

		assertEquals expectedOrder[4], widgets.data[0].displayName
	}

	void testListWithNoParams() {
		createDataForListTests()
		assertEquals samplesArray.size(), widgetDefinitionService.list().data.size()
	}

	void testListCount() {
		createDataForListTests()
		assertEquals WidgetDefinition.count(), widgetDefinitionService.list().results
	}

	void testListWithOnlySortParameter() {
		createDataForListTests()
		def expectedOrder = ["A", "AA", "B", "BB", "C", "D"]

		def widgets = widgetDefinitionService.list([sort: 'value.namespace'])

		assertEquals expectedOrder, widgets.data*.displayName
	}

	void testListSuccess() {
		createDataForListTests()
		assertTrue widgetDefinitionService.list().success
	}

	void testListWithNoWidgetDefinitions() {
		def list = widgetDefinitionService.list()
		assertTrue list.success
		assertEquals 0, list.data.size()
		assertEquals 0, list.results
	}

	void testListWithBadJSONNameParameter() {
		shouldFail (OwfException,
				{ widgetDefinitionService.list([sort: 'youneverfindmeindomain']) }
				)
	}

	private void testAddExternalWidgetsToUser() {
		//add a dummy widgets that would be from marketplace

		//todo someday fix this crazy json string embedded structure
		def widget1 = ([
					displayName: 'Widget1',
					imageUrlLarge: 'http://widget1.com',
					imageUrlSmall: 'http://widget1.com',
					widgetGuid: '9bd3e9ad-366d-4fda-8ae3-2b269f72e059',
					widgetUrl: 'http://widget1.com',
					widgetVersion: '1',
					singleton: false,
					visible: true,
					background: false,
					isSelected: true,
					height: 200,
					width: 200,
					isExtAjaxFormat: true,
					tags: ([] as JSON).toString(),
					directRequired: (['79ae9905-ce38-4de6-ad89-fe598d497703'] as JSON).toString()
				] as JSON).toString()
		def widget2 = ([
					displayName: 'Widget2',
					imageUrlLarge: 'http://widget2.com',
					imageUrlSmall: 'http://widget2.com',
					widgetGuid: '79ae9905-ce38-4de6-ad89-fe598d497703',
					widgetUrl: 'http://widget2.com',
					widgetVersion: '1',
					singleton: false,
					visible: true,
					background: false,
					isSelected: false,
					height: 200,
					width: 200,
					isExtAjaxFormat: true,
					tags: ([] as JSON).toString(),
					directRequired: (['6aca40aa-1b9e-4044-8bbe-d628e6d4518f'] as JSON).toString()
				] as JSON).toString()
		def widget3 = ([
					displayName: 'Widget3',
					imageUrlLarge: 'http://widget3.com',
					imageUrlSmall: 'http://widget3.com',
					widgetGuid: '6aca40aa-1b9e-4044-8bbe-d628e6d4518f',
					widgetUrl: 'http://widget3.com',
					widgetVersion: '1',
					singleton: false,
					visible: true,
					background: false,
					isSelected: false,
					height: 200,
					width: 200,
					isExtAjaxFormat: true,
					tags: ([] as JSON).toString(),
				] as JSON).toString()

		def params = [
					addExternalWidgetsToUser: true,
					widgets: ([widget1, widget2, widget3] as JSON).toString()
				]

		def result = widgetDefinitionService.createOrUpdate(params)
		def data = result.data

		//check for success
		assertTrue result.success

		//check that widget1, widget2 and widget3 are in the return data
		assertEquals data[0].displayName, "Widget1"
		assertEquals data[1].displayName, "Widget2"
		assertEquals data[2].displayName, "Widget3"
		assertEquals data.size(), 3

		//check to make sure that widget is in the admin widgetlist
		result = widgetDefinitionService.list()
		data = result.data

		//check for success
		assertTrue result.success

		//check that widget1, widget2 and widget3 are in the return data
		assertEquals data[0].displayName, "Widget1"
		assertEquals data[1].displayName, "Widget2"
		assertEquals data[2].displayName, "Widget3"
		assertEquals data.size(), 3

		//check to see if that widget1 is in the approval list depending on the config param
		if (grailsApplication.config.owf.enablePendingApprovalWidgetTagGroup) {
			result = personWidgetDefinitionService.listForAdminByTags(
					new GrailsParameterMap([sort: 'name', order: 'ASC'], null))
			data = result.data

			//check for success
			assertTrue result.success

			//check that only widget1
			assertEquals data[0].widgetDefinition.displayName, "Widget1"
			assertEquals data.size(), 1

			//approve
			result = personWidgetDefinitionService.approveForAdminByTags([
						toApprove: ([
							[
								userId: 'testUserWidgetDefinitionServiceTesting'.toUpperCase(),
								widgetGuid: '9bd3e9ad-366d-4fda-8ae3-2b269f72e059'
							],
							[
								userId: 'testUserWidgetDefinitionServiceTesting'.toUpperCase(),
								widgetGuid: '79ae9905-ce38-4de6-ad89-fe598d497703'
							],
							[
								userId: 'testUserWidgetDefinitionServiceTesting'.toUpperCase(),
								widgetGuid: '6aca40aa-1b9e-4044-8bbe-d628e6d4518f'
							]
						] as JSON).toString(),
						toDelete: ([
						] as JSON).toString()
					])
			data = result

			//check for success
			assertTrue result.success

			//check that the widgets are in the launch menu for the current user
			result = personWidgetDefinitionService.list(new GrailsParameterMap([:],null))
			data = result.personWidgetDefinitionList

			//check for success
			assertTrue result.success

			//check that widget1, widget2 and widget3 are in the return data
			assertEquals data[0].widgetDefinition.displayName, "Widget1"
			assertEquals data[1].widgetDefinition.displayName, "Widget2"
			assertEquals data[2].widgetDefinition.displayName, "Widget3"
			assertEquals data.size(), 3
		}
		else {
			//widgets are auto approved thus nothing should show in the approval widget
			result = personWidgetDefinitionService.listForAdminByTags(
					new GrailsParameterMap([sort: 'name', order: 'ASC'], null))
			data = result.data

			//check for success
			assertTrue result.success

			//check that only widget1
			//println("data:${data}")
			assertEquals data.size(), 0

			//check that the widgets are in the launch menu for the current user
			result = personWidgetDefinitionService.list(new GrailsParameterMap([:],null))
			data = result.personWidgetDefinitionList

			//check for success
			assertTrue result.success

			//check that widget1, widget2 and widget3 are in the return data
			assertEquals data[0].widgetDefinition.displayName, "Widget1"
			assertEquals data[1].widgetDefinition.displayName, "Widget2"
			assertEquals data[2].widgetDefinition.displayName, "Widget3"
			assertEquals data.size(), 3
		}

	}

	private void createDataForListTests() {
		// just some sample data, must be called in each test, spring transactions clean up the db
		samplesArray.eachWithIndex { obj, i ->
			WidgetDefinition.build(universalName: java.util.UUID.randomUUID(), widgetGuid: java.util.UUID.randomUUID(), widgetVersion: '1.0', displayName: obj )
		}
	}
}