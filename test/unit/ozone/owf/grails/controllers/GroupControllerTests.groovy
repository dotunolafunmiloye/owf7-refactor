package ozone.owf.grails.controllers

import grails.test.mixin.TestFor

@TestFor(GroupController)
class GroupControllerTests {

	private createGroupListCommandObject() {
		return new GroupListCommand(max: 50, offset: 0, sort: 'userRealName', order: 'ASC')
	}

	void testGroupModifyCommandNotNullRequirements() {
		mockCommandObject(GroupModifyCommand)

		def cmd1 = new GroupModifyCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('data').code
	}

	void testGroupListCommandNotNullRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = new GroupListCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('max').code
		assert 'nullable' == cmd1.errors.getFieldError('offset').code
		assert 'nullable' == cmd1.errors.getFieldError('sort').code
		assert 'nullable' == cmd1.errors.getFieldError('order').code
	}

	void testGroupModifyCommandNotBlankRequirements() {
		mockCommandObject(GroupModifyCommand)

		def cmd1 = new GroupModifyCommand()
		cmd1.data = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('data').code
	}

	void testGroupListCommandNotBlankRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = createGroupListCommandObject()
		cmd1.sort = ''
		cmd1.order = ''
		cmd1.filterOperator = ''
		cmd1.widget_id = ''
		cmd1.dashboard_id = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('sort').code
		assert 'blank' == cmd1.errors.getFieldError('order').code
		assert 'blank' == cmd1.errors.getFieldError('filterOperator').code
		assert 'blank' == cmd1.errors.getFieldError('widget_id').code
		assert 'blank' == cmd1.errors.getFieldError('dashboard_id').code
	}

	void testGroupListCommandMinValueRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = createGroupListCommandObject()
		cmd1.max = 0
		cmd1.offset = -1

		assert !cmd1.validate()
		assert 'min.notmet' == cmd1.errors.getFieldError('max').code
		assert 'min.notmet' == cmd1.errors.getFieldError('offset').code
	}

	void testGroupModifyCommandInListRequirements() {
		mockCommandObject(GroupModifyCommand)

		def cmd1 = new GroupModifyCommand()
		cmd1.tab = 'BOGUS'
		cmd1.update_action = "BOGUS"

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('tab').code
		assert 'not.inList' == cmd1.errors.getFieldError('update_action').code
	}

	void testGroupListCommandInListRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = createGroupListCommandObject()
		cmd1.order = 'BOGUS'
		cmd1.filterOperator = "BOGUS"

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('order').code
		assert 'not.inList' == cmd1.errors.getFieldError('filterOperator').code
	}

	void testGroupListCommandPatternRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = createGroupListCommandObject()
		cmd1.widget_id = 'GARBAGE'
		cmd1.dashboard_id = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('widget_id').code
		assert 'matches.invalid' == cmd1.errors.getFieldError('dashboard_id').code
	}

	void testGroupModifyCommandCustomRequirements() {
		mockCommandObject(GroupModifyCommand)

		def cmd1 = new GroupModifyCommand()
		cmd1.group_id = '1'
		cmd1.tab = null
		cmd1.update_action = 'add'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('group_id').code
		assert 'validator.invalid' == cmd1.errors.getFieldError('update_action').code

		def cmd2 = new GroupModifyCommand()
		cmd2.group_id = '1'
		cmd2.tab = 'users'
		cmd2.update_action = null

		assert !cmd2.validate()
		assert 'validator.invalid' == cmd2.errors.getFieldError('group_id').code
		assert 'validator.invalid' == cmd2.errors.getFieldError('tab').code

		def cmd3 = new GroupModifyCommand()
		cmd3.group_id = null
		cmd3.tab = 'users'
		cmd3.update_action = 'add'

		assert !cmd3.validate()
		assert 'validator.invalid' == cmd3.errors.getFieldError('tab').code
		assert 'validator.invalid' == cmd3.errors.getFieldError('update_action').code
	}

	void testGroupListCommandCustomRequirements() {
		mockCommandObject(GroupListCommand)

		def cmd1 = createGroupListCommandObject()
		cmd1.id = '1'
		cmd1.user_id = '1'
		cmd1.widget_id = UUID.randomUUID().toString()
		cmd1.dashboard_id = UUID.randomUUID().toString()

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('id').code
		assert 'validator.invalid' == cmd1.errors.getFieldError('user_id').code
		assert 'validator.invalid' == cmd1.errors.getFieldError('widget_id').code
		assert 'validator.invalid' == cmd1.errors.getFieldError('dashboard_id').code
	}
}
