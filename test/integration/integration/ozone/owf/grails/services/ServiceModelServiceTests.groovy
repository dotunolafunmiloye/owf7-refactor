package integration.ozone.owf.grails.services

import integration.ozone.owf.grails.conf.OWFGroovyTestCase

import org.junit.After
import org.junit.Before
import org.junit.Test

import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class ServiceModelServiceTests extends OWFGroovyTestCase {

	def serviceModelService
	def widgetDefinitionService

	private void setupWidgetsForDependencyChecks() {
		def g = Group.build(name: 'Bogus', displayName: 'Group Ownership of Widget')
		def wt = WidgetType.build(name: 'standard')
		def w1 = WidgetDefinition.build(displayName: 'Widget 1', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 1', widgetType: wt)
		def w2 = WidgetDefinition.build(displayName: 'Widget 2', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 2', widgetType: wt)
		def w3 = WidgetDefinition.build(displayName: 'Widget 3', widgetGuid: UUID.randomUUID().toString(), universalName: 'Garbage 3', widgetType: wt)
		DomainMapping.build(srcId: w1.id, srcType: WidgetDefinition.TYPE, relationshipType: RelationshipType.requires.strVal, destId: w2.id, destType: WidgetDefinition.TYPE)
		DomainMapping.build(srcId: w2.id, srcType: WidgetDefinition.TYPE, relationshipType: RelationshipType.requires.strVal, destId: w3.id, destType: WidgetDefinition.TYPE)
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal, destId: w2.id, destType: WidgetDefinition.TYPE)
	}

	@Override
	protected void setUp() {
		super.setUp()
		serviceModelService.widgetDefinitionServiceBean = widgetDefinitionService
	}

	@Test
	void testPersonAsJSONWithApostropheInUserRealName() {
		def userRealName = "Mike O'Leary"
		def person = Person.build(username: "moleary", userRealName: userRealName, enabled: true)

		assertEquals userRealName, serviceModelService.createServiceModel(person).userRealName
	}

	@Test
	void testWidgetAllRequiredNoOverride() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName("Widget 1")
		def wTest1 = WidgetDefinition.findByDisplayName("Widget 2")
		def wTest2 = WidgetDefinition.findByDisplayName("Widget 3")

		def sm = serviceModelService.createServiceModel(w)
		def arrGuids = [wTest1.widgetGuid, wTest2.widgetGuid]
		assertEquals arrGuids, sm.allRequired
	}

	@Test
	void testWidgetAllRequiredWithOverride() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName("Widget 3")
		def guid = UUID.randomUUID().toString()

		def sm = serviceModelService.createServiceModel(w, [allRequired: [guid]])
		def arrGuids = [guid]
		assertEquals arrGuids, sm.allRequired
	}

	@Test
	void testWidgetDirectRequiredNoOverride() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName("Widget 1")
		def wTest = WidgetDefinition.findByDisplayName("Widget 2")

		def sm = serviceModelService.createServiceModel(w)
		def arrGuids = [wTest.widgetGuid]
		assertEquals arrGuids, sm.directRequired
	}

	@Test
	void testWidgetDirectRequiredWithOverride() {
		setupWidgetsForDependencyChecks()
		def w = WidgetDefinition.findByDisplayName("Widget 2")
		def guid = UUID.randomUUID().toString()

		def sm = serviceModelService.createServiceModel(w, [directRequired: [guid]])
		def arrGuids = [guid]
		assertEquals arrGuids, sm.directRequired
	}

	@Test
	void testWidgetImageUrlAdminWidgetWithOverride() {
		def wt = WidgetType.build(name: WidgetType.ADMIN)
		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget 1",
				widgetUrl: 'http://www.yahoo.com/launch', imageUrlLarge: 'http://www.yahoo.com/imageLarge',
				imageUrlSmall: 'http://www.yahoo.com/imageSmall', height: 500, width: 900, widgetType: wt)

		def sm = serviceModelService.createServiceModel(w, [localImages: true])
		assertEquals "widget/${w.widgetGuid}/image/imageUrlLarge", sm.imageUrlLarge
		assertEquals "widget/${w.widgetGuid}/image/imageUrlSmall", sm.imageUrlSmall
	}

	@Test
	void testWidgetImageUrlMarketplaceWidgetWithOverride() {
		def wt = WidgetType.build(name: WidgetType.MARKETPLACE)
		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget 1",
				widgetUrl: 'http://www.yahoo.com/launch', imageUrlLarge: 'http://www.yahoo.com/imageLarge',
				imageUrlSmall: 'http://www.yahoo.com/imageSmall', height: 500, width: 900, widgetType: wt)

		def sm = serviceModelService.createServiceModel(w, [localImages: true])
		assertEquals 'http://www.yahoo.com/imageLarge', sm.imageUrlLarge
		assertEquals 'http://www.yahoo.com/imageSmall', sm.imageUrlSmall
	}

	@Test
	void testWidgetImageUrlStandardWidgetNoOverride() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)
		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget 1",
				widgetUrl: 'http://www.yahoo.com/launch', imageUrlLarge: 'http://www.yahoo.com/imageLarge',
				imageUrlSmall: 'http://www.yahoo.com/imageSmall', height: 500, width: 900, widgetType: wt)

		def sm = serviceModelService.createServiceModel(w)
		assertEquals 'http://www.yahoo.com/imageLarge', sm.imageUrlLarge
		assertEquals 'http://www.yahoo.com/imageSmall', sm.imageUrlSmall
	}

	@Test
	void testWidgetImageUrlStandardWidgetWithOverride() {
		def wt = WidgetType.build(name: WidgetType.STANDARD)
		def w = WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget 1",
				widgetUrl: 'http://www.yahoo.com/launch', imageUrlLarge: 'http://www.yahoo.com/imageLarge',
				imageUrlSmall: 'http://www.yahoo.com/imageSmall', height: 500, width: 900, widgetType: wt)

		def sm = serviceModelService.createServiceModel(w, [localImages: true])
		assertEquals "widget/${w.widgetGuid}/image/imageUrlLarge", sm.imageUrlLarge
		assertEquals "widget/${w.widgetGuid}/image/imageUrlSmall", sm.imageUrlSmall
	}
}
