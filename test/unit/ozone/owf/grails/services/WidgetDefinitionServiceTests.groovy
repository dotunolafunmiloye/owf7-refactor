package ozone.owf.grails.services

import grails.converters.JSON
import grails.test.GrailsUnitTestCase
import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class WidgetDefinitionServiceTests extends GrailsUnitTestCase {

	def widgetDefinitionService
	def marketplaceService
	def g = [config: [owf: [marketplaceLocation: 'localhost']]]

	def widgetsJson = ['''
		{
			"displayName": "Simple Widget 1",
			"description": "description",
			"imageUrlLarge": "largeImage",
			"imageUrlSmall": "smallImage",
			"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc60",
			"widgetUrl": "http://wikipedia.com",
			"widgetVersion": "1",
			"singleton": false,
			"visible": true,
			"background": false,
			"height": 200,
			"width": 300,
			"defaultTags" : ["tag"],
			"directRequired": []
		}
		''',
		'''{
			"displayName": "Simple Widget 2",
			"description": "description",
			"imageUrlLarge": "largeImage",
			"imageUrlSmall": "smallImage",
			"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc61",
			"widgetUrl": "http://wikipedia.com",
			"widgetVersion": "1",
			"singleton": false,
			"visible": true,
			"background": false,
			"height": 200,
			"width": 300,
			"defaultTags" : ["tag"],
			"directRequired": []
		}''',
		'''{
			"displayName": "Simple Widget 3",
			"description": "description",
			"imageUrlLarge": "largeImage",
			"imageUrlSmall": "smallImage",
			"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc62",
			"widgetUrl": "http://wikipedia.com",
			"widgetVersion": "1",
			"singleton": false,
			"visible": true,
			"background": false,
			"height": 200,
			"width": 300,
			"defaultTags" : ["tag"],
			"directRequired": []
		}''',
		'''{
			"displayName": "Null URL",
			"description": "description",
			"imageUrlLarge": "largeImage",
			"imageUrlSmall": "smallImage",
			"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc70",
			"widgetUrl": null,
			"widgetVersion": "1",
			"singleton": false,
			"visible": true,
			"background": false,
			"height": 200,
			"width": 300,
			"defaultTags" : ["tag"],
			"directRequired": ['086ca7a6-5c53-438c-99f2-f7820638fc60']
		}'''
	]

	protected void setUp() {
		// A lot of this is similar to the MarketplaceService test class which
		// is somewhat ugly, but the fact is these two services have some
		// overlap (also ugly)....
		super.setUp()
		mockLogging(WidgetDefinitionService)
		mockLogging(MarketplaceService)
		widgetDefinitionService = new WidgetDefinitionService()

		// Setup domain instances.
		mockDomain(WidgetDefinition)
		mockDomain(PersonWidgetDefinition)
		mockDomain(WidgetType,[
			new WidgetType(name:"standard"),
			new WidgetType(name:"marketplace"),
			new WidgetType(name:"metric"),
		])
		mockDomain(Person, [
			new Person(username: 'Bogus', userRealName: 'Bogus User')
		])

		def personWidgetDefinitionServiceControl = mockFor(PersonWidgetDefinitionService)
		personWidgetDefinitionServiceControl.demand.bulkAssignMultipleWidgetsForSingleUser(0..9999) {
			user, widgetDefinitions ->
			def maxPosition = System.currentTimeMillis() as int
			widgetDefinitions.each { widgetDefinition ->
				maxPosition++
				new PersonWidgetDefinition(
						person: user,
						widgetDefinition: widgetDefinition,
						visible : (widgetDefinition.widgetUrl.equals(null) || widgetDefinition.widgetUrl.isAllWhitespace()) ? false : widgetDefinition.visible,
						disabled: false,
						pwdPosition: maxPosition
						).save(flush: true)
			}
		}
		widgetDefinitionService.personWidgetDefinitionService = personWidgetDefinitionServiceControl.createMock()

		// Default mock.  Later on in one particular test we'll override this
		// to verify alternate code paths.
		def marketplaceServiceControl = mockFor(MarketplaceService, true)
		marketplaceServiceControl.demand.buildWidgetListFromMarketplace(0..9999) { guid, mpSourceUrl ->
			HashSet stReturn = []
			if (guid == '086ca7a6-5c53-438c-99f2-f7820638fc70') stReturn.add(widgetsJson[3])
			if (guid == '086ca7a6-5c53-438c-99f2-f7820638fc60') stReturn.add(widgetsJson[0])
			if (guid == '086ca7a6-5c53-438c-99f2-f7820638fc61') stReturn.add(widgetsJson[1])
			else stReturn.add(widgetsJson[2])

			return stReturn
		}
		marketplaceServiceControl.demand.addListingsToDatabase(0..9999) { setOfJsonStrings ->
			def List<WidgetDefinition> updatedWidgets = setOfJsonStrings.collect { obj ->
				def widgetDefinition = new WidgetDefinition()
				widgetDefinition.displayName = obj.displayName
				widgetDefinition.description = obj.description
				widgetDefinition.height = obj.height as Integer
				widgetDefinition.imageUrlLarge = obj.imageUrlLarge
				widgetDefinition.imageUrlSmall = obj.imageUrlSmall
				widgetDefinition.universalName = obj.universalName ?: obj.widgetGuid
				widgetDefinition.widgetGuid = obj.widgetGuid
				widgetDefinition.widgetUrl = obj.widgetUrl
				widgetDefinition.widgetVersion = obj.widgetVersion
				widgetDefinition.width = obj.width as Integer
				widgetDefinition.singleton = obj.singleton
				widgetDefinition.visible = (obj.widgetUrl.equals(null) || obj.widgetUrl.isAllWhitespace()) ? false : obj.visible
				widgetDefinition.background = obj.background
				widgetDefinition.descriptorUrl = obj.descriptorUrl
				widgetDefinition.widgetType = WidgetType.findByName('standard')
				widgetDefinition.save(flush: true, failOnError: true)
			}
			return updatedWidgets
		}
		marketplaceServiceControl.demand.updateWidgetDomainMappings(0..9999) { widgetGuid, directRequired -> [] }
		widgetDefinitionService.marketplaceService = marketplaceServiceControl.createMock()

		// Always return the synthetic user we created above.  There are
		// probably some tests which would work best when tried as an
		// administrator.  If we need to support such, it's best to move this
		// code into individual tests.
		def accountServiceMockControl = mockFor(AccountService, true)
		accountServiceMockControl.demand.getCurrentUser(0..9999) { return Person.findByUsername('Bogus') }
		accountServiceMockControl.demand.getLoggedInUser(0..9999) { return Person.findByUsername('Bogus') }
		accountServiceMockControl.demand.getLoggedInUserIsAdmin(0..9999) { return false }
		widgetDefinitionService.accountService = accountServiceMockControl.createMock()
		widgetDefinitionService.grailsApplication = g
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testConvertJsonParamToDomainField() {
		assertEquals 'displayName', widgetDefinitionService.convertJsonParamToDomainField('value.namespace')
	}

	void testConvertJsonParamToDomainFieldException() {
		shouldFail(OwfException, { widgetDefinitionService.convertJsonParamToDomainField('iwillneverexist') }
		)
	}

	/**
	 * Verifying just the parameters to the add listings call.  In the actual
	 * code, these params come from the controller, which doesn't really do
	 * much validation.  It's possible, too, that these services could be
	 * called from other services and so we'd like to verify via testing that
	 * the code is well-behaved.
	 */
	void testAddExternalWidgetsToUser() {
		// Test for empty params.
		def params1 = []
		def obj1 = widgetDefinitionService.addExternalWidgetsToUser(params1)
		assertEquals obj1.data, []
		assertTrue obj1.success

		// How about null...?
		def obj2 = widgetDefinitionService.addExternalWidgetsToUser(null)
		assertEquals obj2.data, []
		assertTrue obj2.success

		// Round it out with nothing
		def obj3 = widgetDefinitionService.addExternalWidgetsToUser()
		assertEquals obj3.data, []
		assertTrue obj3.success

		// Test the marketplaceUrl parameter and further verify the OwfE is
		// thrown (mis-configuration)
		widgetDefinitionService.grailsApplication = null
		shouldFail(OwfException, {widgetDefinitionService.addExternalWidgetsToUser()})
		def obj4 = widgetDefinitionService.addExternalWidgetsToUser([marketplaceUrl: 'ok'])
		assertEquals obj4.data, []
		assertTrue obj4.success

		shouldFail(OwfException, { widgetDefinitionService.addExternalWidgetsToUser([marketplaceUrl: 'ok', widgets: '[']) })

		// This should actually create the widget definitions *and* the person
		// widget definition records as well.
		def obj5 = widgetDefinitionService.addExternalWidgetsToUser([
					marketplaceUrl: 'ok',
					widgets: "${widgetsJson}"
				])
		assertTrue obj5.success
		assertFalse obj5.data.isEmpty()

		def pwds = PersonWidgetDefinition.findAll()
		assertEquals widgetsJson.size, pwds.size
	}

	/**
	 * Verify that when we add a widget to a user, then add another to the user,
	 * that we don't eliminate the first widget from the user's launch menu.
	 */
	void testAddExternalWidgetsToUserNoDeleteOldWidgets() {
		def obj1 = widgetDefinitionService.addExternalWidgetsToUser([
					marketplaceUrl: 'junk',
					widgets: "${widgetsJson[0]}"
				])

		def pwds1 = PersonWidgetDefinition.findAll()
		assertEquals 1, pwds1.size

		def obj2 = widgetDefinitionService.addExternalWidgetsToUser([
					marketplaceUrl: 'junk',
					widgets: "${widgetsJson[1]}"
				])
		def pwds2 = PersonWidgetDefinition.findAll()
		assertEquals 2, pwds2.size
	}


	/**
	 * Testing the create of widget definitions from JSON strings typically
	 * supplied by a Marketplace instance.  Note that this does not exercise
	 * the domain mapping portion of the code or the tags.  At this point, what
	 * we really want to see is the generation of the widget definitions.
	 */
	void testCreateWidgetDefinitionFromJson() {
		// Start with the "never seen this widget before and OMP is available"
		// path. Use the default mock object created in setUp().
		def nW1 = widgetDefinitionService.createWidgetDefinitionFromJSON(JSON.parse(widgetsJson[3]), '')
		assertNotNull nW1
		assertEquals 1, nW1.id

		def nW2 = widgetDefinitionService.createWidgetDefinitionFromJSON(JSON.parse(widgetsJson[0]), '')
		assertNotNull nW2
		assertEquals 2, nW2.id

		// Next, try the "never seen this widget before and OMP is either
		// grumpy or doesn't support synchronization because it's not new
		// enough" path.  That is, does the fall-back path work.
		def marketplaceServiceControl1 = mockFor(MarketplaceService, true)
		marketplaceServiceControl1.demand.buildWidgetListFromMarketplace(0..9999) { guid, mpSourceUrl ->
			throw new Exception('Blown OMP')
		}
		marketplaceServiceControl1.demand.addListingsToDatabase(0..9999) { setOfJsonStrings ->
			def List<WidgetDefinition> updatedWidgets = setOfJsonStrings.collect { obj ->
				def widgetDefinition = new WidgetDefinition()
				widgetDefinition.displayName = obj.displayName
				widgetDefinition.description = obj.description
				widgetDefinition.height = obj.height as Integer
				widgetDefinition.imageUrlLarge = obj.imageUrlLarge
				widgetDefinition.imageUrlSmall = obj.imageUrlSmall
				widgetDefinition.universalName = obj.universalName ?: obj.widgetGuid
				widgetDefinition.widgetGuid = obj.widgetGuid
				widgetDefinition.widgetUrl = obj.widgetUrl
				widgetDefinition.widgetVersion = obj.widgetVersion
				widgetDefinition.width = obj.width as Integer
				widgetDefinition.singleton = obj.singleton
				widgetDefinition.visible = (obj.widgetUrl.equals(null) || obj.widgetUrl.isAllWhitespace()) ? false : obj.visible
				widgetDefinition.background = obj.background
				widgetDefinition.descriptorUrl = obj.descriptorUrl
				widgetDefinition.widgetType = WidgetType.findByName('standard')
				widgetDefinition.save(flush: true, failOnError: true)
			}
			return updatedWidgets
		}
		widgetDefinitionService.marketplaceService = marketplaceServiceControl1.createMock()

		def nW3 = widgetDefinitionService.createWidgetDefinitionFromJSON(JSON.parse(widgetsJson[1]), '')
		assertNotNull nW3
		assertEquals 3, nW3.id

		def nW4 = widgetDefinitionService.createWidgetDefinitionFromJSON(JSON.parse(widgetsJson[2]), '')
		assertNotNull nW4
		assertEquals 4, nW4.id

		// Finally, try the "we've seen this widget before path" -- in this
		// particular case, OMP should never be touched and therefore we're
		// setting the restrictions on the call count to zero.
		def marketplaceServiceControl2 = mockFor(MarketplaceService, true)
		marketplaceServiceControl2.demand.buildWidgetListFromMarketplace(0..0) { guid, mpSourceUrl -> [] as HashSet }
		marketplaceServiceControl2.demand.addListingsToDatabase(0..0) { setOfJsonStrings -> [] }
		widgetDefinitionService.marketplaceService = marketplaceServiceControl2.createMock()

		// It should still return the record, though!
		def nW5 = widgetDefinitionService.createWidgetDefinitionFromJSON(JSON.parse(widgetsJson[1]), '')
		assertNotNull nW5
		assertEquals 3, nW5.id
	}

}