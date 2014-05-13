package ozone.owf.grails.controllers

import grails.test.mixin.TestFor

@TestFor(PersonController)
class PersonControllerTests {

	private createPersonListCommandObject() {
		return new PersonListCommand(max: 50, offset: 0, sort: 'userRealName', order: 'ASC')
	}

	void testPersonListCommandNotNullRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = new PersonListCommand()

		assert !plc1.validate()
		assert 'nullable' == plc1.errors.getFieldError('max').code
		assert 'nullable' == plc1.errors.getFieldError('offset').code
		assert 'nullable' == plc1.errors.getFieldError('sort').code
		assert 'nullable' == plc1.errors.getFieldError('order').code
	}

	void testPersonListCommandNotBlankRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = createPersonListCommandObject()
		plc1.sort = ''
		plc1.order = ''
		plc1.filterOperator = ''
		plc1.widget_id = ''

		assert !plc1.validate()
		assert 'blank' == plc1.errors.getFieldError('sort').code
		assert 'blank' == plc1.errors.getFieldError('order').code
		assert 'blank' == plc1.errors.getFieldError('filterOperator').code
		assert 'blank' == plc1.errors.getFieldError('widget_id').code
	}

	void testPersonListCommandMinValueRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = createPersonListCommandObject()
		plc1.max = 0
		plc1.offset = -1

		assert !plc1.validate()
		assert 'min.notmet' == plc1.errors.getFieldError('max').code
		assert 'min.notmet' == plc1.errors.getFieldError('offset').code
	}

	void testPersonListCommandInListRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = createPersonListCommandObject()
		plc1.order = 'BOGUS'
		plc1.filterOperator = "BOGUS"

		assert !plc1.validate()
		assert 'not.inList' == plc1.errors.getFieldError('order').code
		assert 'not.inList' == plc1.errors.getFieldError('filterOperator').code
	}

	void testPersonListCommandPatternRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = createPersonListCommandObject()
		plc1.widget_id = 'GARBAGE'

		assert !plc1.validate()
		assert 'matches.invalid' == plc1.errors.getFieldError('widget_id').code
	}

	void testPersonListCommandCustomRequirements() {
		mockCommandObject(PersonListCommand)

		def plc1 = createPersonListCommandObject()
		plc1.id = '1'
		plc1.group_id = '1'
		plc1.widget_id = UUID.randomUUID().toString()

		assert !plc1.validate()
		assert 'validator.invalid' == plc1.errors.getFieldError('id').code
		assert 'validator.invalid' == plc1.errors.getFieldError('group_id').code
		assert 'validator.invalid' == plc1.errors.getFieldError('widget_id').code
	}
}
