package integration.ozone.owf.grails.domain

import org.junit.Before
import org.junit.Test

import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class WidgetDefinitionTests extends GroovyTestCase {

	@Before
	void setUp() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)
		WidgetDefinition.build(displayName: 'Mandatory', widgetUrl: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com/icon.gif',
				imageUrlLarge: 'http://www.yahoo.com/icon.gif', width: 400, height: 400, visible: true, singleton: false, background: false,
				widgetType: wt, widgetGuid: UUID.randomUUID().toString())

		WidgetDefinition.build(displayName: 'Optional', widgetUrl: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com/icon.gif',
				imageUrlLarge: 'http://www.yahoo.com/icon.gif', width: 400, height: 400, visible: true, singleton: false, background: false,
				widgetType: wt, description: 'Some description', descriptorUrl: 'http://www.yahoo.com/descriptor.htm',
				universalName: 'UN', widgetVersion: '0.99', widgetGuid: UUID.randomUUID().toString())
	}

	@Test
	void testAsExportableMapMandatory() {
		def w = WidgetDefinition.findByDisplayName('Mandatory')
		def mp = w.asExportableMap()
		def desiredKeys = ['displayName', 'widgetUrl', 'imageUrlSmall', 'imageUrlLarge', 'width', 'height',
			'visible', 'singleton', 'background', 'widgetTypes', 'defaultTags'] as Set

		def arrWTName = [w.widgetType.name]

		assertEquals desiredKeys, mp.keySet()
		assertEquals w.displayName, mp.displayName
		assertEquals w.widgetUrl, mp.widgetUrl
		assertEquals w.imageUrlSmall, mp.imageUrlSmall
		assertEquals w.imageUrlLarge, mp.imageUrlLarge
		assertEquals w.width, mp.width
		assertEquals w.height, mp.height
		assertEquals w.visible, mp.visible
		assertEquals w.singleton, mp.singleton
		assertEquals w.background, mp.background
		assertEquals arrWTName, mp.widgetTypes
		assertTrue mp.defaultTags.isEmpty()
	}

	@Test
	void testAsExportableMapOptional() {
		def w = WidgetDefinition.findByDisplayName('Optional')
		def mp = w.asExportableMap()
		def desiredKeys = ['displayName', 'widgetUrl', 'imageUrlSmall', 'imageUrlLarge', 'width', 'height',
			'visible', 'singleton', 'background', 'widgetTypes', 'defaultTags', 'descriptorUrl', 'universalName',
			'description', 'widgetVersion'] as Set

		assertEquals desiredKeys, mp.keySet()
		def arrWTName = [w.widgetType.name]

		assertEquals desiredKeys, mp.keySet()
		assertEquals w.displayName, mp.displayName
		assertEquals w.widgetUrl, mp.widgetUrl
		assertEquals w.imageUrlSmall, mp.imageUrlSmall
		assertEquals w.imageUrlLarge, mp.imageUrlLarge
		assertEquals w.width, mp.width
		assertEquals w.height, mp.height
		assertEquals w.visible, mp.visible
		assertEquals w.singleton, mp.singleton
		assertEquals w.background, mp.background
		assertEquals arrWTName, mp.widgetTypes
		assertTrue mp.defaultTags.isEmpty()

		assertEquals w.descriptorUrl, mp.descriptorUrl
		assertEquals w.universalName, mp.universalName
		assertEquals w.description, mp.description
		assertEquals w.widgetVersion, mp.widgetVersion
	}
}
