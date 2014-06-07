package integration.ozone.owf.grails.services

import grails.test.*
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

/**
 * The marketplace service uses GORM criteria queries and these cannot be unit 
 * tested using mock objects (or, at least, not easily). Moving those tests into
 * this class instead.
 */
class MarketplaceServiceTests extends GroovyTestCase {
	def marketplaceService

	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	void testUpdateWidgetDomainMappings() {
		// By default, we'll have all the administration widgets as these are
		// created by the BootStrap code.
		def tpe = WidgetType.findByName('standard')

		def wdTestInstances = [
			new WidgetDefinition(universalName: "Test Widget Definition 1", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 1", widgetUrl: "http://wikipedia.org", imageUrlSmall: "http://wikipedia.org",
			imageUrlLarge: "http://wikipedia.org", width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://wikipedia.org",
			widgetTypes: tpe),

			new WidgetDefinition(universalName: "Test Widget Definition 2", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 2", widgetUrl: "http://cnn.com", imageUrlSmall: "http://cnn.com", imageUrlLarge: "http://cnn.com",
			width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://cnn.com",
			widgetTypes: tpe),

			new WidgetDefinition(universalName: "Test Widget Definition 3", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 3", widgetUrl: "http://java.sun.com", imageUrlSmall: "http://java.sun.com", imageUrlLarge: "http://java.sun.com",
			width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://java.sun.com",
			widgetTypes: tpe)
		]
		wdTestInstances*.save(flush: true)

		assertEquals 0, marketplaceService.domainMappingService.getAllMappings(wdTestInstances[0], RelationshipType.requires).size()

		marketplaceService.updateWidgetDomainMappings(wdTestInstances[0].widgetGuid, null)
		assertEquals 0, marketplaceService.domainMappingService.getAllMappings(wdTestInstances[0], RelationshipType.requires).size()

		def depGuids = wdTestInstances[1..2]*.widgetGuid

		marketplaceService.updateWidgetDomainMappings(wdTestInstances[0].widgetGuid, depGuids)
		assertEquals 2, marketplaceService.domainMappingService.getAllMappings(wdTestInstances[0], RelationshipType.requires).size()

		marketplaceService.updateWidgetDomainMappings(wdTestInstances[0].widgetGuid, depGuids[0])
		assertEquals 1, marketplaceService.domainMappingService.getAllMappings(wdTestInstances[0], RelationshipType.requires).size()
	}

}
