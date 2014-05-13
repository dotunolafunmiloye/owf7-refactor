package ozone.owf.grails.services

import grails.test.GrailsUnitTestCase

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class MarketplaceServiceTests extends GrailsUnitTestCase {

	MarketplaceService marketplaceService

	protected void setUp() {
		super.setUp()
		mockLogging(MarketplaceService, false)
		marketplaceService = new MarketplaceService()
		def domainMappingServiceMockClass = mockFor(DomainMappingService, true)
		domainMappingServiceMockClass.demand.deleteAllMappings(0..9999) { a,b,c -> [] }
		domainMappingServiceMockClass.demand.getAllMappings(0..9999) { a,b,c -> [] }
		domainMappingServiceMockClass.demand.getAllMappings(0..9999) { a,b -> [] }
		domainMappingServiceMockClass.demand.createMapping(0..9999) { a,b,c -> [] }
		domainMappingServiceMockClass.demand.deleteMapping(0..9999) { a,b,c -> [] }
		marketplaceService.domainMappingService = domainMappingServiceMockClass.createMock()

		// Mock out the various domains we'll use
		mockDomain(WidgetType,[
			new WidgetType(name:"standard"),
			new WidgetType(name:"marketplace"),
			new WidgetType(name:"metric"),
		])
	}

	protected void tearDown() {
		super.tearDown()
	}
	// copied from what Marketplace sends for a simple, no intents, no dependencies, widget
	def singleSimpleWidgetJson = '''
	{
		"displayName":"name",
		"description":"description",
		"imageUrlLarge":"largeImage",
		"imageUrlSmall":"smallImage",
		"widgetGuid":"086ca7a6-5c53-438c-99f2-f7820638fc6f",
		"widgetUrl":"http://wikipedia.com",
		"widgetVersion":"1",
		"singleton":false,
		"visible":true,
		"background":false,
		"height":200,
		"width":300,
		"directRequired" :[],
		"defaultTags" : ["tag"],
		"widgetTypes":["Widget"]
	}
	'''
	def singleWidgetWithIntentsJson = '''
	{
		"displayName":"nameIntents",
		"description":"descriptionIntents",
		"imageUrlLarge":"largeImageIntents",
		"imageUrlSmall":"smallImageIntents",
		"widgetGuid":"086ca7a6-5c53-438c-99f2-f7820638fc6e",
		"widgetUrl":"http://Intents.com",
		"widgetVersion":"1",
		"singleton":false,
		"visible":true,
		"background":false,
		"height":200,
		"width":300,
		"directRequired" :[],
		"defaultTags" : ["tag"],
		"widgetTypes":["Widget"]
	}
	'''
	def singleSimpleWidget = new JSONArray("[${singleSimpleWidgetJson}]")
	def singleWidgetWithIntents = new JSONArray("[${singleWidgetWithIntentsJson}]")
	def withAndWithoutIntents = new JSONArray("[${singleSimpleWidgetJson}, ${singleWidgetWithIntentsJson}]")

	// just make sure that it actually parses a basic widget
	void testSimplestWidget() {
		mockDomain(WidgetDefinition)

		def widgets = marketplaceService.addListingsToDatabase(singleSimpleWidget)
		assertEquals 1, widgets.size()
		assertEquals "name", widgets[0].displayName
		assertEquals "description", widgets[0].description
		assertEquals "largeImage", widgets[0].imageUrlLarge
		assertEquals "smallImage", widgets[0].imageUrlSmall
		assertEquals "086ca7a6-5c53-438c-99f2-f7820638fc6f", widgets[0].widgetGuid
		assertEquals "http://wikipedia.com", widgets[0].widgetUrl
		assertEquals "1", widgets[0].widgetVersion
		assertEquals false, widgets[0].singleton
		assertEquals true, widgets[0].visible
		assertEquals false, widgets[0].background
		assertEquals 200, widgets[0].height
		assertEquals 300, widgets[0].width
	}

	void testNullOrBlankLaunchUrl() {
		mockDomain(WidgetDefinition)

		def nullWidgetJson = '''
			{
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
				"directRequired": [],
				"defaultTags": ["tag"],
				"widgetTypes": ["Widget"]
			}
		'''

		def blankWidgetJson = '''
			{
				"displayName": "Blank URL",
				"description": "description",
				"imageUrlLarge": "largeImage",
				"imageUrlSmall": "smallImage",
				"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc71",
				"widgetUrl": "",
				"widgetVersion": "1",
				"singleton": false,
				"visible": true,
				"background": false,
				"height": 200,
				"width": 300,
				"directRequired": [],
				"defaultTags": ["tag"],
				"widgetTypes": ["Widget"]
			}
		'''

		def whitespaceWidgetJson = '''
			{
				"displayName": "Whitespace URL",
				"description": "description",
				"imageUrlLarge": "largeImage",
				"imageUrlSmall": "smallImage",
				"widgetGuid": "086ca7a6-5c53-438c-99f2-f7820638fc72",
				"widgetUrl":"     ",
				"widgetVersion": "1",
				"singleton": false,
				"visible": true,
				"background": false,
				"height": 200,
				"width": 300,
				"directRequired": [],
				"defaultTags": ["tag"],
				"widgetTypes": ["Widget"]
			}
		'''

		def oddUrls = new JSONArray("[${nullWidgetJson}, ${blankWidgetJson}, ${whitespaceWidgetJson}]")
		def widgets = marketplaceService.addListingsToDatabase(oddUrls)
		assertEquals 3, widgets.size()
		widgets.each {
			assertFalse it.visible
		}
	}

	void testMultipleWidgets() {
		mockDomain(WidgetDefinition)

		def widgets=marketplaceService.addListingsToDatabase(withAndWithoutIntents)
		assertEquals 2, widgets.size()
		assertEquals "name", widgets[0].displayName
		assertEquals "nameIntents", widgets[1].displayName
	}

}
