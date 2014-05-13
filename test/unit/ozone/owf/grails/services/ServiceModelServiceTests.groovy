package ozone.owf.grails.services

import grails.test.GrailsUnitTestCase
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.WidgetDefinition

class ServiceModelServiceTests extends GrailsUnitTestCase {

	def serviceModelService

	private def createPerson() {
		def userRealName = "Mike O'Leary"
		def username = "MO'Leary"

		new Person(
				username: username,
				userRealName: userRealName,
				enabled: true
				)

	}

	protected void setUp() {
		super.setUp()
		serviceModelService = new ServiceModelService()
		def widgetDefinitionServiceMockClass = mockFor(WidgetDefinitionService, true)
		widgetDefinitionServiceMockClass.demand.getRequirements(1..1) { a -> [] }
		widgetDefinitionServiceMockClass.demand.getRequirements(1..1) { a, b -> [] }
		serviceModelService.widgetDefinitionServiceBean = widgetDefinitionServiceMockClass.createMock()
	}

	void testDashboardDefinitionToServiceModel() {
		def dashboard = new Dashboard()
		dashboard.guid = '3F2504E0-4F89-11D3-9A0C-0305E82C3301'
		dashboard.isdefault = false
		dashboard.dashboardPosition = 0
		dashboard.name =  "Hello World 1234567890!@\$%^&*()_+-|?><`~."
		dashboard.user = new Person()

		def serviceModel = serviceModelService.createServiceModel(dashboard)

		assertEquals serviceModel.name, dashboard.name
		assertEquals serviceModel.isdefault, dashboard.isdefault
		assertEquals serviceModel.guid, dashboard.guid
	}

	void testDashboardDefinitionToServiceModelWithApostropheInUsername() {
		def dashboard = new Dashboard()

		def username = "George Can'tstanza"

		dashboard.guid = '3F2504E0-4F89-11D3-9A0C-0305E82C3301'
		dashboard.isdefault = false
		dashboard.dashboardPosition = 0
		dashboard.name =  "Testing userid with apostrophe"
		dashboard.user = new Person(username: username)

		def serviceModel = serviceModelService.createServiceModel(dashboard)

		assertEquals serviceModel.name, dashboard.name
		assertEquals serviceModel.isdefault, dashboard.isdefault
	}

	void testPersonToServiceModel() {
		def person = createPerson()
		def serviceModel = serviceModelService.createServiceModel(person)

		assertEquals serviceModel.username, person.username
		assertEquals serviceModel.userRealName, person.userRealName
	}

	void testPreferenceToJsonWithJsonInValue() {
		def val =
				'''
		{
			"a": "apple",
			"b": "banana"
		}
		'''
		def namespace = "com.company.widget"
		def path = "abc"
		def user = createPerson()
		def preference = new Preference()
		preference.namespace = namespace
		preference.path = path
		preference.value = val
		preference.user = user

		def json = serviceModelService.createServiceModel(preference).toDataMap()
		assertEquals json.get("namespace"), namespace
		assertEquals json.get("path") ,path
		assertEquals json.get("user").get("userId"), user.username
		assertEquals json.get("value"), val
	}

	void testPreferenceToServiceModelWithSingleTinckInValue() {
		def val = "I can't do it"
		def namespace = "com.company.widget"
		def path = "status"
		def user = createPerson()
		def preference = new Preference()
		preference.namespace = namespace
		preference.path = path
		preference.value = val
		preference.user = user

		def serviceModel = serviceModelService.createServiceModel(preference)
		assertEquals serviceModel.namespace, namespace
		assertEquals serviceModel.value , val
		assertEquals serviceModel.path ,path
		assertEquals serviceModel.user.username, user.username
	}

	void testWidgetDefinitionToServiceModel() {
		def widgetDefinition = new WidgetDefinition()

		widgetDefinition.universalName = "3F2504E0-4F89-11D3-9A0C-0305E82C3301"
		widgetDefinition.widgetGuid = "3F2504E0-4F89-11D3-9A0C-0305E82C3301"
		widgetDefinition.widgetVersion = "1.0"
		widgetDefinition.displayName = "Hello World 1234567890!@\$'%^&*()_+-|?><`~."
		widgetDefinition.widgetUrl = "https://localhost/"
		widgetDefinition.imageUrlSmall = "https://localhost/"
		widgetDefinition.imageUrlLarge = "https://localhost/"
		widgetDefinition.width = 200
		widgetDefinition.height = 200
		widgetDefinition.personWidgetDefinitions = []

		def serviceModel = serviceModelService.createServiceModel(widgetDefinition)

		assertEquals serviceModel.displayName, widgetDefinition.displayName
		assertEquals serviceModel.widgetGuid, widgetDefinition.widgetGuid
		assertEquals serviceModel.widgetUrl, widgetDefinition.widgetUrl
		assertEquals serviceModel.imageUrlSmall, widgetDefinition.imageUrlSmall
		assertEquals serviceModel.imageUrlLarge, widgetDefinition.imageUrlLarge
		assertEquals serviceModel.width, widgetDefinition.width
		assertEquals serviceModel.height, widgetDefinition.height
		assertEquals serviceModel.widgetVersion, widgetDefinition.widgetVersion
	}

	void testWidgetDefinitionToServiceModelWithCachedImage() {
		def widgetDefinition = new WidgetDefinition()

		widgetDefinition.universalName = "3F2504E0-4F89-11D3-9A0C-0305E82C3301"
		widgetDefinition.widgetGuid = "3F2504E0-4F89-11D3-9A0C-0305E82C3301"
		widgetDefinition.widgetVersion = "1.0"
		widgetDefinition.displayName = "Hello World 1234567890!@\$'%^&*()_+-|?><`~."
		widgetDefinition.widgetUrl = "https://localhost/"
		widgetDefinition.imageUrlSmall = "https://localhost/"
		widgetDefinition.imageUrlLarge = "https://localhost/"
		widgetDefinition.width = 200
		widgetDefinition.height = 200
		widgetDefinition.personWidgetDefinitions = []

		def serviceModel = serviceModelService.createServiceModel(widgetDefinition, [localImages: true])

		assertEquals serviceModel.displayName, widgetDefinition.displayName
		assertEquals serviceModel.widgetGuid, widgetDefinition.widgetGuid
		assertEquals serviceModel.widgetUrl, widgetDefinition.widgetUrl
		assertEquals serviceModel.imageUrlSmall, "widget/3F2504E0-4F89-11D3-9A0C-0305E82C3301/image/imageUrlSmall"
		assertEquals serviceModel.imageUrlLarge, "widget/3F2504E0-4F89-11D3-9A0C-0305E82C3301/image/imageUrlLarge"
		assertEquals serviceModel.width, widgetDefinition.width
		assertEquals serviceModel.height, widgetDefinition.height
		assertEquals serviceModel.widgetVersion, widgetDefinition.widgetVersion
	}

}
