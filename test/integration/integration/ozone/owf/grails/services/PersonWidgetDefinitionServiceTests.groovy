package integration.ozone.owf.grails.services

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.services.AutoLoginAccountService
import ozone.owf.grails.test.integration.WidgetDefinitionPostParams

class PersonWidgetDefinitionServiceTests extends GroovyTestCase {

	def widgetDefinitionService
	def personWidgetDefinitionService

	protected void setUp() {
		super.setUp()
		def acctService = new AutoLoginAccountService()
		Person p = new Person(username:'testUserWidgetDefinitionServiceTesting'.toUpperCase(), userRealName: 'foo', enabled:true)
		p.save()
		acctService.autoAccountName = 'testUserWidgetDefinitionServiceTesting'
		acctService.autoRoles = [ERoleAuthority.ROLE_ADMIN.strVal]
		widgetDefinitionService.accountService = acctService
		personWidgetDefinitionService.accountService = acctService
	}

	protected void tearDown() {
		super.tearDown()
	}

	def generatePWDPostParamsA()
	{
		def retVal = [
					"guid":"12345678-1234-1234-1234-1234567890a0"
				]
	}

	def generatePWDPostParamsC()
	{
		def retVal = [
					"guid":"12345678-1234-1234-1234-1234567890a1"
				]
	}

	def widgetNameParams()
	{
		def retVal = [
					"widgetName":"My Widget%"
				]
		new GrailsParameterMap(retVal,null);
	}

	def pwdParams(pwd)
	{
		def retVal = [
					"personWidgetDefinition":pwd
				]
	}

	def guidAndPersonIdParams(guid, personId)
	{
		def retVal = [
					"guid":guid,
					"personId":personId
				]
	}

	def nullPwdParams()
	{
		def retVal = [
					"personWidgetDefinition":null
				]
	}

	void testCreate()
	{
		widgetDefinitionService.create(WidgetDefinitionPostParams.generatePostParamsA())
		def resultOfCreate = personWidgetDefinitionService.create(generatePWDPostParamsA())
		assertTrue resultOfCreate.success
		assertEquals "12345678-1234-1234-1234-1234567890a0", resultOfCreate.personWidgetDefinition.widgetDefinition.widgetGuid
		//This tests show too.
		def resultOfShow = personWidgetDefinitionService.show(generatePWDPostParamsA())
		assertEquals resultOfShow.personWidgetDefinition.widgetDefinition.widgetGuid, resultOfCreate.personWidgetDefinition.widgetDefinition.widgetGuid
	}

	void testUpdateForPersonWidgetDefinitionParameter()
	{
		widgetDefinitionService.create(WidgetDefinitionPostParams.generatePostParamsA())
		def response = personWidgetDefinitionService.create(generatePWDPostParamsA())

		assertEquals "My Widget", response.personWidgetDefinition.widgetDefinition.displayName

		def widgetDefinition = response.personWidgetDefinition.widgetDefinition
		widgetDefinition.displayName = "New Widget Name"

		def personWidgetDefinition = response.personWidgetDefinition
		personWidgetDefinition.widgetDefinition = widgetDefinition

		response = personWidgetDefinitionService.update(pwdParams(personWidgetDefinition))

		assertNotSame "My Widget", response.personWidgetDefinition.widgetDefinition.displayName
		assertEquals "New Widget Name", response.personWidgetDefinition.widgetDefinition.displayName
	}

	void testUpdateForGuidAndPersonIdParameters()
	{
		widgetDefinitionService.create(WidgetDefinitionPostParams.generatePostParamsA())
		def response = personWidgetDefinitionService.create(generatePWDPostParamsA())

		assertEquals "My Widget", response.personWidgetDefinition.widgetDefinition.displayName

		Person person = Person.findByUsername('testUserWidgetDefinitionServiceTesting'.toUpperCase())

		def widgetDefinition = response.personWidgetDefinition.widgetDefinition
		widgetDefinition.displayName = "New Widget Name"

		def personWidgetDefinition = response.personWidgetDefinition
		personWidgetDefinition.widgetDefinition = widgetDefinition

		response = personWidgetDefinitionService.update(guidAndPersonIdParams(widgetDefinition.widgetGuid, person.id))

		assertNotSame "My Widget", response.personWidgetDefinition.widgetDefinition.displayName
		assertEquals "New Widget Name", response.personWidgetDefinition.widgetDefinition.displayName
	}

	void testUpdateForNonExistentWidgetDefinitionGuid()
	{
		Person person = Person.findByUsername('testUserWidgetDefinitionServiceTesting'.toUpperCase())

		shouldFail(OwfException, {
			personWidgetDefinitionService.update(guidAndPersonIdParams('12345678-1234-1234-abcd-1234567890a9', person.id))
		})
	}

	void testList()
	{
		widgetDefinitionService.create(WidgetDefinitionPostParams.generatePostParamsA())
		widgetDefinitionService.create(WidgetDefinitionPostParams.generatePostParamsC())
		personWidgetDefinitionService.create(generatePWDPostParamsA())
		personWidgetDefinitionService.create(generatePWDPostParamsC())

		assertEquals 2, personWidgetDefinitionService.list(widgetNameParams()).personWidgetDefinitionList.size()
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
		widgetDefinitionService.create(mp1)
		widgetDefinitionService.create(mp2)

		WidgetDefinition wd1 = WidgetDefinition.findByWidgetGuid(mp1.widgetGuid)
		WidgetDefinition wd2 = WidgetDefinition.findByWidgetGuid(mp2.widgetGuid)

		assertEquals(countPwd(person), 0)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [], [])
		assertEquals(countPwd(person), 0)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [wd1, wd2])
		assertEquals(countPwd(person), 2)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [wd2], [wd1])
		assertEquals(countPwd(person), 1)

		personWidgetDefinitionService.bulkAssignMultipleWidgetsForSingleUser(person, [], [wd2,wd1])
		assertEquals(countPwd(person), 0)

		// Note that we are not testing the "same widget in both the add and
		// discard pile" case because that's more an abuse of reason than a
		// common expectation.
	}
}