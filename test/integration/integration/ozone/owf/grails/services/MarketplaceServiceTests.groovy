package integration.ozone.owf.grails.services

import integration.ozone.owf.grails.conf.DataClearingTestCase
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

/**
 * The marketplace service uses GORM criteria queries and these cannot be unit
 * tested using mock objects (or, at least, not easily). Moving those tests into
 * this class instead.
 */
class MarketplaceServiceTests extends DataClearingTestCase {

	def marketplaceService

	void testUpdateWidgetDomainMappings() {
		def tpe = WidgetType.findByName('standard')

		def wdTestInstances = [
			WidgetDefinition.build(universalName: "Test Widget Definition 1", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 1", widgetUrl: "http://wikipedia.org", imageUrlSmall: "http://wikipedia.org",
			imageUrlLarge: "http://wikipedia.org", width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://wikipedia.org"),

			WidgetDefinition.build(universalName: "Test Widget Definition 2", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 2", widgetUrl: "http://cnn.com", imageUrlSmall: "http://cnn.com", imageUrlLarge: "http://cnn.com",
			width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://cnn.com"),

			WidgetDefinition.build(universalName: "Test Widget Definition 3", widgetGuid: java.util.UUID.randomUUID().toString(),
			displayName: "Test Widget Definition 3", widgetUrl: "http://java.sun.com", imageUrlSmall: "http://java.sun.com", imageUrlLarge: "http://java.sun.com",
			width: 200, height: 200, widgetVersion: "1.0", descriptorUrl: "http://java.sun.com")
		]

		// Strangely enough, if you try to add the widget type to each individual instance above, it tries to add from the
		// widget_type side, which then fails because the owning side is, of course, the widget_definition side.  This has
		// been the case for a long time, but with the change from M:M to M:1 the former code fails.  Doing it as shown below
		// works, though.  Chalking this up to the vagaries of Hibernate, Grails on Hibernate, GORM and the test data plugin.
		wdTestInstances*.widgetType = tpe
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
