package ozone.owf.grails.controllers

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.services.DashboardService

@TestFor(DashboardController)
@Mock(Dashboard)
class DashboardControllerTests {

	void testCreateOrUpdateCommandCustomRequirementsDashboardId() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.dashboard_id = UUID.randomUUID().toString()

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('dashboard_id').code
	}

	void testCreateOrUpdateCommandCustomRequirementsIsGroupDashboard() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.isGroupDashboard = true
		cmd1.user_id = -1

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('isGroupDashboard').code
	}

	void testCreateOrUpdateCommandCustomRequirementsTab() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.tab = 'groups'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('tab').code
	}

	void testCreateOrUpdateCommandCustomRequirementsUpdateAction() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.update_action = 'add'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('update_action').code
	}

	void testCreateOrUpdateCommandCustomRequirementsValidDashboardJson() {
		defineBeans { dashboardService(DashboardService) }

		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.dashboardService = controller.dashboardService
		cmd1.data = '[{"Blah":"Blump", "Blech":"Bump", "Bleep":"Jump"},{}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandCustomRequirementsValidGroupJsonHasIds() {
		defineBeans { dashboardService(DashboardService) }

		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.dashboardService = controller.dashboardService
		cmd1.tab = 'groups'
		cmd1.data = '[{"name":"Group 1"},{}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandCustomRequirementsValidGroupJsonMalformed() {
		defineBeans { dashboardService(DashboardService) }

		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.dashboardService = controller.dashboardService
		cmd1.tab = 'groups'
		cmd1.data = '[{"name":"Group 1", "},{}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandInListRequirements() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.tab = 'BOGUS'
		cmd1.update_action = "BOGUS"

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('tab').code
		assert 'not.inList' == cmd1.errors.getFieldError('update_action').code
	}

	void testCreateOrUpdateCommandMinValueRequirements() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.user_id = 0

		assert !cmd1.validate()
		assert 'min.notmet' == cmd1.errors.getFieldError('user_id').code
	}

	void testCreateOrUpdateCommandNotBlankRequirements() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.data = ''
		cmd1.dashboard_id = ''
		cmd1.tab = ''
		cmd1.update_action = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('data').code
		assert 'blank' == cmd1.errors.getFieldError('dashboard_id').code
		assert 'blank' == cmd1.errors.getFieldError('tab').code
		assert 'blank' == cmd1.errors.getFieldError('update_action').code
	}

	void testCreateOrUpdateCommandNotNullRequirements() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandPatternRequirements() {
		mockCommandObject(DashboardCreateOrUpdateCommand)
		def cmd1 = new DashboardCreateOrUpdateCommand()
		cmd1.dashboard_id = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('dashboard_id').code
	}

	void testDeleteCommandCustomRequirementsValidDashboardJson() {
		mockCommandObject(DashboardDeleteCommand)
		def cmd1 = new DashboardDeleteCommand()
		cmd1.data = '[{"Blah":"Blump", "Blech":"Bump", "Bleep":"Jump"},{}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code
	}

	void testDeleteCommandInListRequirements() {
		mockCommandObject(DashboardDeleteCommand)
		def cmd1 = new DashboardDeleteCommand()
		cmd1.tab = 'BOGUS'
		cmd1.update_action = "BOGUS"

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('tab').code
		assert 'not.inList' == cmd1.errors.getFieldError('update_action').code
	}

	void testDeleteCommandMinValueRequirements() {
		mockCommandObject(DashboardDeleteCommand)
		def cmd1 = new DashboardDeleteCommand()
		cmd1.user_id = 0

		assert !cmd1.validate()
		assert 'min.notmet' == cmd1.errors.getFieldError('user_id').code
	}

	void testDeleteCommandNotBlankRequirements() {
		mockCommandObject(DashboardDeleteCommand)
		def cmd1 = new DashboardDeleteCommand()
		cmd1.data = ''
		cmd1.tab = ''
		cmd1.update_action = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('data').code
		assert 'blank' == cmd1.errors.getFieldError('tab').code
		assert 'blank' == cmd1.errors.getFieldError('update_action').code
	}

	void testDeleteCommandNotNullRequirements() {
		mockCommandObject(DashboardDeleteCommand)
		def cmd1 = new DashboardDeleteCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('data').code
	}

	void testListCommandCustomRequirementsAdminEnabled() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.adminEnabled = true

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('adminEnabled').code
	}

	void testListCommandCustomRequirementsAdminEnabledRequiresKnownUseCase() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.adminEnabled = true
		cmd1.max = 50
		cmd1.offset = 0
		cmd1.order = 'ASC'
		cmd1.sort = 'name'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('adminEnabled').code
	}

	void testListCommandCustomRequirementsFilterOperator() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.filterOperator = 'OR'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('filterOperator').code
	}

	void testListCommandCustomRequirementsFiltersEmptyJson() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.filterOperator = 'OR'
		cmd1.filters = '[]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('filters').code
	}

	void testListCommandCustomRequirementsFiltersRequireOperator() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.filters = '[]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('filters').code
	}

	void testListCommandCustomRequirementsFiltersWrongJson() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.filterOperator = 'OR'
		// Missing the filterValue
		cmd1.filters = '[{"filterField":"name"},{"filterField":"description","filterValue":"Group Dashboard 1 (33)"}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('filters').code
	}

	void testListCommandCustomRequirementsGroupIdExcludesGroupDashboard() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.group_id = 1
		cmd1.isGroupDashboard = true

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('group_id').code
	}

	void testListCommandCustomRequirementsGroupIdRequiresTab() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.group_id = 1
		cmd1.tab = null

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('group_id').code
	}

	void testListCommandCustomRequirementsIdNoFilterAllowed() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.id = UUID.randomUUID().toString()
		cmd1.filters = 'test'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('id').code
	}

	void testListCommandCustomRequirementsIdNoFilterOperatorAllowed() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.id = UUID.randomUUID().toString()
		cmd1.filterOperator = 'OR'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('id').code
	}

	void testListCommandCustomRequirementsMax() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.max = 40

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('max').code
	}

	void testListCommandCustomRequirementsOffset() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.offset = 40

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('offset').code
	}

	void testListCommandCustomRequirementsOrder() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.order = 'ASC'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('order').code
	}

	void testListCommandCustomRequirementsSort() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.sort = 'name'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('sort').code
	}

	void testListCommandCustomRequirementsUserIdExcludesGroupDashboard() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.user_id = 1
		cmd1.isGroupDashboard = true

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('user_id').code
	}

	void testListCommandCustomRequirementsUserIdRequiresTab() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.user_id = 1
		cmd1.tab = null

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('user_id').code
	}

	void testListCommandMaxValueRequirements() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.max = 51

		assert !cmd1.validate()
		assert 'max.exceeded' == cmd1.errors.getFieldError('max').code
	}

	void testListCommandMinValueRequirements() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.max = 0
		cmd1.offset = -1
		cmd1.group_id = 0
		cmd1.user_id = 0

		assert !cmd1.validate()
		assert 'min.notmet' == cmd1.errors.getFieldError('max').code
		assert 'min.notmet' == cmd1.errors.getFieldError('offset').code
		assert 'min.notmet' == cmd1.errors.getFieldError('group_id').code
		assert 'min.notmet' == cmd1.errors.getFieldError('user_id').code
	}

	void testListCommandNotBlankRequirements() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.order = ''
		cmd1.sort = ''
		cmd1.id = ''
		cmd1.tab = ''
		cmd1.filterOperator = ''
		cmd1.filters = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('order').code
		assert 'blank' == cmd1.errors.getFieldError('sort').code
		assert 'blank' == cmd1.errors.getFieldError('id').code
		assert 'blank' == cmd1.errors.getFieldError('tab').code
		assert 'blank' == cmd1.errors.getFieldError('filterOperator').code
		assert 'blank' == cmd1.errors.getFieldError('filters').code
	}

	void testListCommandNotInListRequirements() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.order = 'BOGUS'
		cmd1.sort = 'BOGUS'
		cmd1.tab = 'BOGUS'
		cmd1.filterOperator = 'BOGUS'

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('order').code
		assert 'not.inList' == cmd1.errors.getFieldError('sort').code
		assert 'not.inList' == cmd1.errors.getFieldError('tab').code
		assert 'not.inList' == cmd1.errors.getFieldError('filterOperator').code
	}

	void testListCommandPatternRequirements() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()
		cmd1.id = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('id').code
	}

	void testListCommandRaw() {
		mockCommandObject(DashboardListCommand)
		def cmd1 = new DashboardListCommand()

		assert cmd1.validate()
	}

	void testPrefBulkDeleteUpdateCommandCustomRequirementsUpdateBadJson() {
		mockCommandObject(DashboardPrefBulkDeleteUpdateCommand)

		def cmd1 = new DashboardPrefBulkDeleteUpdateCommand()
		cmd1.viewGuidsToDelete = '[]'
		// Comma shouldn't be on the end, isdefault is missing value.
		cmd1.viewsToUpdate = '	[{"guid":"da75828-bf21-ac93-2040-8969b6e6fad7","isdefault":},]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('viewsToUpdate').code
	}

	void testPrefBulkDeleteUpdateCommandCustomRequirementsUpdateMalformedGuid() {
		mockCommandObject(DashboardPrefBulkDeleteUpdateCommand)

		def cmd1 = new DashboardPrefBulkDeleteUpdateCommand()
		cmd1.viewGuidsToDelete = '[]'
		// First GUID is slightly malformed (missing char in first octet).
		cmd1.viewsToUpdate = '	[{"guid":"da75828-bf21-ac93-2040-8969b6e6fad7","isdefault":true,"name":"Bogus Two"},{"guid":"f26895b7-d582-47fb-9bf6-00587708ec0f","isdefault":false,"name":"1-Dashboard (2)"},{"guid":"6028708d-1908-98f1-de97-a5895109923e","isdefault":false,"name":"Bogus One"}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('viewsToUpdate').code
	}

	void testPrefBulkDeleteUpdateCommandCustomRequirementsUpdateMissingData() {
		mockCommandObject(DashboardPrefBulkDeleteUpdateCommand)

		def cmd1 = new DashboardPrefBulkDeleteUpdateCommand()
		cmd1.viewGuidsToDelete = '[]'
		// First dashboard is missing the name attribute.
		cmd1.viewsToUpdate = '	[{"guid":"da75828-bf21-ac93-2040-8969b6e6fad7","isdefault":true},{"guid":"f26895b7-d582-47fb-9bf6-00587708ec0f","isdefault":false,"name":"1-Dashboard (2)"},{"guid":"6028708d-1908-98f1-de97-a5895109923e","isdefault":false,"name":"Bogus One"}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('viewsToUpdate').code
	}

	void testPrefBulkDeleteUpdateCommandNotBlankRequirements() {
		mockCommandObject(DashboardPrefBulkDeleteUpdateCommand)

		def cmd1 = new DashboardPrefBulkDeleteUpdateCommand()
		cmd1.viewGuidsToDelete = ''
		cmd1.viewsToUpdate = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('viewGuidsToDelete').code
		assert 'blank' == cmd1.errors.getFieldError('viewsToUpdate').code
	}

	void testPrefBulkDeleteUpdateCommandNotNullRequirements() {
		mockCommandObject(DashboardPrefBulkDeleteUpdateCommand)

		def cmd1 = new DashboardPrefBulkDeleteUpdateCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('viewGuidsToDelete').code
		assert 'nullable' == cmd1.errors.getFieldError('viewsToUpdate').code
	}

	void testPrefCreateOrUpdateCommandNotBlankRequirements() {
		mockCommandObject(DashboardPrefCreateOrUpdateCommand)

		def cmd1 = new DashboardPrefCreateOrUpdateCommand()
		cmd1.guid = ''
		cmd1.layoutConfig = ''
		cmd1.name = ''
		cmd1.state = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('guid').code
		assert 'blank' == cmd1.errors.getFieldError('layoutConfig').code
		assert 'blank' == cmd1.errors.getFieldError('name').code
		assert 'blank' == cmd1.errors.getFieldError('state').code
	}

	void testPrefCreateOrUpdateCommandNotNullRequirements() {
		mockCommandObject(DashboardPrefCreateOrUpdateCommand)

		def cmd1 = new DashboardPrefCreateOrUpdateCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('guid').code
		assert 'nullable' == cmd1.errors.getFieldError('layoutConfig').code
		assert 'nullable' == cmd1.errors.getFieldError('name').code
		assert 'nullable' == cmd1.errors.getFieldError('state').code
	}

	void testPrefCreateOrUpdateCommandPatternRequirements() {
		mockCommandObject(DashboardPrefCreateOrUpdateCommand)

		def cmd1 = new DashboardPrefCreateOrUpdateCommand()
		cmd1.guid = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('guid').code
	}

	void testRestoreCommandNotBlankRequirements() {
		mockCommandObject(DashboardRestoreCommand)

		def cmd1 = new DashboardRestoreCommand()
		cmd1.guid = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('guid').code
	}

	void testRestoreCommandNotNullRequirements() {
		mockCommandObject(DashboardRestoreCommand)

		def cmd1 = new DashboardRestoreCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('guid').code
	}

	void testRestoreCommandPatternRequirements() {
		mockCommandObject(DashboardRestoreCommand)

		def cmd1 = new DashboardRestoreCommand()
		cmd1.guid = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('guid').code
	}
}
