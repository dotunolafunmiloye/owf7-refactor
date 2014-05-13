package ozone.owf.grails.controllers

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin

import org.junit.Assert

@TestMixin(GrailsUnitTestMixin)
class PersonWidgetDefinitionControllerTests {

	void testListUserAndGroupWidgetsCommandNullableRequirements() {
		mockCommandObject(PersonWidgetDefinitionListUserAndGroupWidgetsCommand)
		def cmd1 = new PersonWidgetDefinitionListUserAndGroupWidgetsCommand()

		assert cmd1.validate()
	}

	void testListUserAndGroupWidgetsCommandNotBlankRequirements() {
		mockCommandObject(PersonWidgetDefinitionListUserAndGroupWidgetsCommand)
		def cmd1 = new PersonWidgetDefinitionListUserAndGroupWidgetsCommand()
		cmd1.widgetGuid = ''
		cmd1.widgetName = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('widgetGuid').code
		assert 'blank' == cmd1.errors.getFieldError('widgetName').code
	}

	void testListUserAndGroupWidgetsCommandMatchesRequirements() {
		mockCommandObject(PersonWidgetDefinitionListUserAndGroupWidgetsCommand)
		def cmd1 = new PersonWidgetDefinitionListUserAndGroupWidgetsCommand()
		cmd1.widgetGuid = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('widgetGuid').code
	}

	void testListUserAndGroupWidgetsCommandCustomRequirementsGuidOrName() {
		mockCommandObject(PersonWidgetDefinitionListUserAndGroupWidgetsCommand)
		def cmd1 = new PersonWidgetDefinitionListUserAndGroupWidgetsCommand()
		cmd1.widgetGuid = UUID.randomUUID().toString()
		cmd1.widgetName = 'GARBAGE'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('widgetGuid').code
		assert 'validator.invalid' == cmd1.errors.getFieldError('widgetName').code
	}
}
