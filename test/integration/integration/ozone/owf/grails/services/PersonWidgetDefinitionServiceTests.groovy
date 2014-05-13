package integration.ozone.owf.grails.services

import integration.WidgetDefinitionPostParams
import integration.ozone.owf.grails.conf.DataClearingTestCase

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.DomainMapping
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.AutoLoginAccountService

class PersonWidgetDefinitionServiceTests extends DataClearingTestCase {

	def personWidgetDefinitionService
	def widgetDefinitionService

	private void createGroupWidgetData(int maxNumberOfWidgets) {
		def standardWidgetType = WidgetType.build(name: 'standard')

		def g = Group.build(name: "Widget Group", displayName: "Widget Group")

		def widgets = (1..maxNumberOfWidgets).collect {
			WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget ${it}", widgetUrl: 'http://www.yahoo.com',
					imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500, width: 900, widgetType: standardWidgetType)
		}

		widgets.each { widget ->
			DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal,
					destType: WidgetDefinition.TYPE, destId: widget.id)
		}

		def p = Person.build(username: "testGroup".toUpperCase(), userRealName: "T. Group")
		g.addToPeople(p)
		g.save(flush: true)
	}


	@Override
	public void setUp() {
		super.setUp()

		def acctService = new AutoLoginAccountService()
		Person p = new Person(username: 'testUserWidgetDefinitionServiceTesting'.toUpperCase(), userRealName: 'foo', enabled: true).save()
		acctService.autoAccountName = 'testUserWidgetDefinitionServiceTesting'
		acctService.autoRoles = [ERoleAuthority.ROLE_ADMIN.strVal]
		widgetDefinitionService.accountService = acctService
		personWidgetDefinitionService.accountService = acctService
	}

	void testBulkAssignMultipleWidgetsForSingleUser() {
		def countPwd = { person ->
			def ct = PersonWidgetDefinition.withCriteria {
				eq('person', person)
				projections {
					rowCount()
				}
			}
			ct[0]
		}

		Person person = Person.findByUsername('testUserWidgetDefinitionServiceTesting'.toUpperCase())

		// Generate a couple widgets
		def mp1 = WidgetDefinitionPostParams.generatePostParamsA()
		def mp2 = WidgetDefinitionPostParams.generatePostParamsC()
		WidgetDefinition.build(mp1)
		WidgetDefinition.build(mp2)

		WidgetDefinition wd1 = WidgetDefinition.findByWidgetGuid(mp1.widgetGuid)
		WidgetDefinition wd2 = WidgetDefinition.findByWidgetGuid(mp2.widgetGuid)

		assertEquals(countPwd(person), 0)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [], [])
		assertEquals(countPwd(person), 0)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [wd1, wd2])
		assertEquals(countPwd(person), 2)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [wd2], [wd1])
		assertEquals(countPwd(person), 1)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [], [wd2, wd1])
		assertEquals(countPwd(person), 0)

		// Note that we are not testing the "same widget in both the add and
		// discard pile" case because that's more an abuse of reason than a
		// common expectation.
	}

	void testCreate() {
		WidgetDefinition.build(WidgetDefinitionPostParams.generatePostParamsA())
		def resultOfCreate = personWidgetDefinitionService.create(["guid":"12345678-1234-1234-1234-1234567890a0"])
		assertTrue resultOfCreate.success
		assertEquals "12345678-1234-1234-1234-1234567890a0", resultOfCreate.personWidgetDefinition.widgetDefinition.widgetGuid
		//This tests show too.
		def resultOfShow = personWidgetDefinitionService.show(["guid":"12345678-1234-1234-1234-1234567890a0"])
		assertEquals resultOfShow.personWidgetDefinition.widgetDefinition.widgetGuid, resultOfCreate.personWidgetDefinition.widgetDefinition.widgetGuid
	}

	void testList() {
		WidgetType.build(name: 'standard')
		def wd1 = WidgetDefinition.build(
				widgetGuid: java.util.UUID.randomUUID().toString(),
				widgetVersion: "1.0",
				displayName: "My Widget 1",
				widgetUrl: "http://foo.com/widget",
				imageUrlSmall: "http://foo.com/widget/images/small.jpg",
				imageUrlLarge: "http://foo.com/widget/images/large.jpg",
				width: 200,
				height: 200,
				widgetType: WidgetType.findByName('standard')
				)

		def wd2 = WidgetDefinition.build(
				widgetGuid: java.util.UUID.randomUUID().toString(),
				widgetVersion: "1.0",
				displayName: "My Widget 2",
				widgetUrl: "http://foo.com/widget",
				imageUrlSmall: "http://foo.com/widget/images/small.jpg",
				imageUrlLarge: "http://foo.com/widget/images/large.jpg",
				width: 200,
				height: 200,
				widgetType: WidgetType.findByName('standard')
				)

		def pwd1 = PersonWidgetDefinition.build(
				person: widgetDefinitionService.accountService.getLoggedInUser(),
				widgetDefinition: wd1,
				visible: true,
				pwdPosition: 1
				)

		def pwd2 = PersonWidgetDefinition.build(
				person: widgetDefinitionService.accountService.getLoggedInUser(),
				widgetDefinition: wd2,
				visible: true,
				pwdPosition: 2
				)

		assertEquals 2, WidgetDefinition.createCriteria().count() { ilike('displayName', 'My Widget%') }
		assertEquals 2, PersonWidgetDefinition.createCriteria().count() { eq('person', widgetDefinitionService.accountService.getLoggedInUser()) }

		def results = personWidgetDefinitionService.list(new GrailsParameterMap(["widgetName": "My Widget%"], null))
		assertEquals 2, results.personWidgetDefinitionList.size()
	}

	void testSetGroupWidgetsForLoggedInUserAddsAllWidgets() {
		def widgetCount = 5
		createGroupWidgetData(widgetCount)
		def p = Person.findByUsername("testGroup".toUpperCase())

		// Effectively like a brand-new user.  Make sure they have five widgets.
		personWidgetDefinitionService.setGroupWidgetsForLoggedInUser(p)

		assertEquals 5, PersonWidgetDefinition.countByPerson(p)
	}

	void testSetGroupWidgetsForLoggedInUserAddsAllWidgetsOnlyOnce() {
		def widgetCount = 5
		createGroupWidgetData(widgetCount)
		def p = Person.findByUsername("testGroup".toUpperCase())

		// Need to create another group which owns one of the widgets, thus creating the situation where we could conceivably
		// add the same widget to the user twice because of the multi-mapped group -> widget.  The code should block this
		// from happening.
		def g = Group.build(name: 'Widget Extra Group', displayName: 'Widget Extra Group')
		g.addToPeople(p)

		def w = WidgetDefinition.findByDisplayName("Widget 1")
		DomainMapping.build(srcId: g.id, srcType: Group.TYPE, relationshipType: RelationshipType.owns.strVal,
				destType: WidgetDefinition.TYPE, destId: w.id)

		personWidgetDefinitionService.setGroupWidgetsForLoggedInUser(p)

		assertEquals 5, PersonWidgetDefinition.countByPerson(p)
	}

	void testSetGroupWidgetsForLoggedInUserRemovesExcessWidgets() {
		def widgetCount = 5
		createGroupWidgetData(widgetCount)
		def p = Person.findByUsername("testGroup".toUpperCase())

		// Need to create some additional widgets and personal widgets which are described as group widgets in the pwd table,
		// but aren't part of the group mapping that the user has.  Then, verify that these are deleted.
		def extraWidgets = ((widgetCount + 1)..(widgetCount + 3)).collect {
			WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget ${it}", widgetUrl: 'http://www.yahoo.com',
					imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500, width: 900)
		}

		def maxPos = widgetCount
		extraWidgets.each { widget ->
			PersonWidgetDefinition.build(person: p, widgetDefinition: widget, visible: true, userWidget: false, groupWidget: true,
					favorite: false, disabled: false, pwdPosition: maxPos++)
		}

		personWidgetDefinitionService.setGroupWidgetsForLoggedInUser(p)

		assertEquals 5, PersonWidgetDefinition.countByPerson(p)
	}

	void testSetGroupWidgetsForLoggedInUserUpdatesUserWidgets() {
		def widgetCount = 5
		createGroupWidgetData(widgetCount)
		def p = Person.findByUsername("testGroup".toUpperCase())

		// Need to create some additional widgets and personal widgets which are described as group widgets AND user widgets
		// in the pwd table, but aren't part of the group mapping that the user has.  Then, verify that these are updated to
		// show that they are, in fact, no longer group widgets.
		def extraWidgets = ((widgetCount + 1)..(widgetCount + 3)).collect {
			WidgetDefinition.build(widgetGuid: UUID.randomUUID().toString(), displayName: "Widget ${it}", widgetUrl: 'http://www.yahoo.com',
					imageUrlLarge: 'http://www.yahoo.com', imageUrlSmall: 'http://www.yahoo.com', height: 500, width: 900)
		}

		def maxPos = widgetCount
		extraWidgets.each { widget ->
			PersonWidgetDefinition.build(person: p, widgetDefinition: widget, visible: true, userWidget: true, groupWidget: true,
					favorite: false, disabled: false, pwdPosition: maxPos++)
		}

		personWidgetDefinitionService.setGroupWidgetsForLoggedInUser(p)

		assertEquals 8, PersonWidgetDefinition.countByPerson(p)
		assertEquals 3, PersonWidgetDefinition.countByPersonAndUserWidget(p, true)
		assertEquals 0, PersonWidgetDefinition.countByPersonAndUserWidgetAndGroupWidget(p, true, true)
	}

	void testUpdateForGuidAndPersonIdParameters() {
		WidgetDefinition.build(WidgetDefinitionPostParams.generatePostParamsA())
		def response = personWidgetDefinitionService.create(["guid":"12345678-1234-1234-1234-1234567890a0"])

		assertEquals "My Widget", response.personWidgetDefinition.widgetDefinition.displayName

		Person person = Person.findByUsername('testUserWidgetDefinitionServiceTesting'.toUpperCase())

		def widgetDefinition = response.personWidgetDefinition.widgetDefinition
		widgetDefinition.displayName = "New Widget Name"

		def personWidgetDefinition = response.personWidgetDefinition
		personWidgetDefinition.widgetDefinition = widgetDefinition

		response = personWidgetDefinitionService.update(["guid": widgetDefinition.widgetGuid, "personId": person.id])

		assertNotSame "My Widget", response.personWidgetDefinition.widgetDefinition.displayName
		assertEquals "New Widget Name", response.personWidgetDefinition.widgetDefinition.displayName
	}

	void testUpdateForNonExistentWidgetDefinitionGuid() {
		Person person = Person.findByUsername('testUserWidgetDefinitionServiceTesting'.toUpperCase())

		shouldFail(OwfException, {
			personWidgetDefinitionService.update(["guid": '12345678-1234-1234-abcd-1234567890a9', "personId": person.id])
		})
	}

	void testUpdateForPersonWidgetDefinitionParameter() {
		WidgetDefinition.build(WidgetDefinitionPostParams.generatePostParamsA())
		def response = personWidgetDefinitionService.create(["guid":"12345678-1234-1234-1234-1234567890a0"])

		assertEquals "My Widget", response.personWidgetDefinition.widgetDefinition.displayName

		def widgetDefinition = response.personWidgetDefinition.widgetDefinition
		widgetDefinition.displayName = "New Widget Name"

		def personWidgetDefinition = response.personWidgetDefinition
		personWidgetDefinition.widgetDefinition = widgetDefinition

		response = personWidgetDefinitionService.update(["personWidgetDefinition": personWidgetDefinition])

		assertNotSame "My Widget", response.personWidgetDefinition.widgetDefinition.displayName
		assertEquals "New Widget Name", response.personWidgetDefinition.widgetDefinition.displayName
	}
}
