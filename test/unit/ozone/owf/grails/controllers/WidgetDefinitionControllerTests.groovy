package ozone.owf.grails.controllers

import grails.converters.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.web.json.JSONObject

import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.WidgetDefinitionService

@TestFor(WidgetDefinitionController)
@Mock([WidgetDefinition, WidgetType])
class WidgetDefinitionControllerTests {

	void testCachedImageCommandInListRequirements() {
		mockCommandObject(WidgetCachedImageCommand)
		def cmd1 = new WidgetCachedImageCommand()
		cmd1.name = 'GARBAGE'

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('name').code
	}

	void testCachedImageCommandMatchesRequirements() {
		mockCommandObject(WidgetCachedImageCommand)
		def cmd1 = new WidgetCachedImageCommand()
		cmd1.widgetGuid = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('widgetGuid').code
	}

	void testCachedImageCommandNotBlankRequirements() {
		mockCommandObject(WidgetCachedImageCommand)
		def cmd1 = new WidgetCachedImageCommand()
		cmd1.name = ''
		cmd1.widgetGuid = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('widgetGuid').code
		assert 'blank' == cmd1.errors.getFieldError('name').code
	}

	void testCachedImageCommandNotNullRequirements() {
		mockCommandObject(WidgetCachedImageCommand)
		def cmd1 = new WidgetCachedImageCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('widgetGuid').code
		assert 'nullable' == cmd1.errors.getFieldError('name').code
	}

	void testCreateOrUpdateCommandCustomJsonGroupOrUserDataRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.tab = 'groups'
		cmd1.data = '[{"name":"Group 1"},{"id": 1}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code

		def cmd2 = new WidgetCreateOrUpdateCommand()
		cmd2.widgetDefinitionService = controller.widgetDefinitionService
		cmd2.tab = 'groups'
		cmd2.widget_id = UUID.randomUUID().toString()
		cmd2.update_action = 'add'
		cmd2.data = '[{"id": 0}]'

		assert !cmd2.validate()
		assert 'validator.invalid' == cmd2.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandCustomJsonWidgetDataRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.data = '[{"name":"Group 1"},{}]'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandCustomTabFieldPresentRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.tab = 'groups'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('tab').code

		def cmd2 = new WidgetCreateOrUpdateCommand()
		cmd2.widgetDefinitionService = controller.widgetDefinitionService
		cmd2.tab = 'groups'
		cmd2.update_action = 'add'

		assert !cmd2.validate()
		assert 'validator.invalid' == cmd2.errors.getFieldError('tab').code

		def cmd3 = new WidgetCreateOrUpdateCommand()
		cmd3.widgetDefinitionService = controller.widgetDefinitionService
		cmd3.tab = 'groups'
		cmd3.widget_id = UUID.randomUUID().toString()

		assert !cmd3.validate()
		assert 'validator.invalid' == cmd3.errors.getFieldError('tab').code
	}

	void testCreateOrUpdateCommandCustomUpdateActionFieldPresentRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.update_action = 'add'

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('update_action').code

		def cmd2 = new WidgetCreateOrUpdateCommand()
		cmd2.widgetDefinitionService = controller.widgetDefinitionService
		cmd2.update_action = 'add'
		cmd2.tab = 'groups'

		assert !cmd2.validate()
		assert 'validator.invalid' == cmd2.errors.getFieldError('update_action').code

		def cmd3 = new WidgetCreateOrUpdateCommand()
		cmd3.widgetDefinitionService = controller.widgetDefinitionService
		cmd3.update_action = 'add'
		cmd3.widget_id = UUID.randomUUID().toString()

		assert !cmd3.validate()
		assert 'validator.invalid' == cmd3.errors.getFieldError('update_action').code
	}

	void testCreateOrUpdateCommandCustomWidgetIdFieldPresentRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.widget_id = UUID.randomUUID().toString()

		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('widget_id').code

		def cmd2 = new WidgetCreateOrUpdateCommand()
		cmd2.widgetDefinitionService = controller.widgetDefinitionService
		cmd2.widget_id = UUID.randomUUID().toString()
		cmd2.tab = 'groups'

		assert !cmd2.validate()
		assert 'validator.invalid' == cmd2.errors.getFieldError('widget_id').code

		def cmd3 = new WidgetCreateOrUpdateCommand()
		cmd3.widgetDefinitionService = controller.widgetDefinitionService
		cmd3.widget_id = UUID.randomUUID().toString()
		cmd3.update_action = 'add'

		assert !cmd3.validate()
		assert 'validator.invalid' == cmd3.errors.getFieldError('widget_id').code
	}

	void testCreateOrUpdateCommandInListRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.tab = 'bogus'
		cmd1.update_action = 'bogus'

		assert !cmd1.validate()
		assert 'not.inList' == cmd1.errors.getFieldError('tab').code
		assert 'not.inList' == cmd1.errors.getFieldError('update_action').code
	}

	void testCreateOrUpdateCommandMatchesRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.widget_id = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('widget_id').code
	}

	void testCreateOrUpdateCommandNotBlankRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.tab = ''
		cmd1.update_action = ''
		cmd1.widget_id = ''
		cmd1.data = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('tab').code
		assert 'blank' == cmd1.errors.getFieldError('update_action').code
		assert 'blank' == cmd1.errors.getFieldError('widget_id').code
		assert 'blank' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandNotNullRequirements() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('data').code
	}

	void testCreateOrUpdateCommandWidgetCreateDataRemapping() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		String createOrUpdateCreateJson = '''[
			{
				"id":"",
				"name":"Bogus",
				"originalName":"",
				"version":"0.9",
				"description":"A messed up widget.",
				"url":"https://www.yahoo.com",
				"headerIcon":"https://www.yahoo.com/img.gif",
				"image":"https://www.yahoo.com/launch.gif",
				"width":500,
				"height":625,
				"widgetGuid":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
				"universalName":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
				"maximized":"",
				"minimized":"",
				"x":"",
				"y":"",
				"visible":true,
				"definitionVisible":"",
				"background":false,
				"disabled":"",
				"editable":"",
				"tags":[],
				"singleton":false,
				"allRequired":"",
				"directRequired":"",
				"userId":"",
				"userRealName":"",
				"totalUsers":"",
				"totalGroups":"",
				"widgetTypes":[{"id":1,"name":"standard"}],
				"descriptorUrl":"",
				"title":"Bogus",
				"groups":""
			}
		]'''

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.data = createOrUpdateCreateJson

		JSONObject objReference = JSON.parse(cmd1.data)[0]

		cmd1.validate()
		assertNotNull cmd1.parsedWidgetJson
		assertNull cmd1.parsedPrincipalLikeJson
		cmd1.parsedWidgetJson.each {
			assertTrue it.isNewWidget
			assertNull it.version
			assertNull it.name
			assertNull it.url
			assertNull it.headerIcon
			assertNull it.image
			assertNull it.id
			assertNull it.widgetTypes

			assertEquals it.widgetVersion, objReference.version
			assertEquals it.displayName, objReference.name
			assertEquals it.widgetUrl, objReference.url
			assertEquals it.imageUrlSmall, objReference.headerIcon
			assertEquals it.imageUrlLarge, objReference.image
			assertEquals it.widgetTypeName, objReference.widgetTypes[0].name
		}
	}

	void testCreateOrUpdateCommandWidgetUpdateDataRemapping() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		String createOrUpdateUpdateJson = '''[
			{
				"id":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
				"name":"Bogus",
				"originalName":"",
				"version":"0.9.9",
				"description":"A messed up widget.",
				"url":"https://www.yahoo.com",
				"headerIcon":"https://www.yahoo.com/img.gif",
				"image":"https://www.yahoo.com/launch.gif",
				"width":500,
				"height":625,
				"widgetGuid":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
				"universalName":"14af5f76-eaf3-46c2-f6d6-f3fcf62779d3",
				"maximized":false,
				"minimized":false,
				"x":0,
				"y":0,
				"visible":true,
				"definitionVisible":true,
				"background":false,
				"disabled":"",
				"editable":"",
				"tags":[],
				"singleton":false,
				"allRequired":[],
				"directRequired":[],
				"userId":"",
				"userRealName":"",
				"totalUsers":1,
				"totalGroups":4,
				"widgetTypes":[{"id":1,"name":"standard"}],
				"descriptorUrl":null,
				"title":"Bogus",
				"groups":""
			}
		]'''

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.data = createOrUpdateUpdateJson

		cmd1.validate()
		assertNotNull cmd1.parsedWidgetJson
		assertNull cmd1.parsedPrincipalLikeJson
		cmd1.parsedWidgetJson.each {
			assertFalse it.isNewWidget
			// Additional assertions about the returned data are covered by the create test.
		}
	}

	void testCreateOrUpdateCommandPrincipalDataRemapping() {
		defineBeans {
			widgetDefinitionService(WidgetDefinitionService)
		}

		mockCommandObject(WidgetCreateOrUpdateCommand)
		def cmd1 = new WidgetCreateOrUpdateCommand()
		cmd1.widgetDefinitionService = controller.widgetDefinitionService
		cmd1.tab = 'groups'
		cmd1.widget_id = UUID.randomUUID().toString()
		cmd1.update_action = 'add'
		cmd1.data = '[{"id": 1}]'

		cmd1.validate()
		assertNotNull cmd1.parsedPrincipalLikeJson
		assertNull cmd1.parsedWidgetJson
	}

	void testDependentsCommandCustomRequirementsIdsAreGuids() {
		mockCommandObject(WidgetDependentsCommand)
		def cmd1 = new WidgetDependentsCommand()

		cmd1.ids = [UUID.randomUUID().toString(), 'Bogus', UUID.randomUUID().toString()]
		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('ids').code
	}

	void testDependentsCommandCustomRequirementsIdsNotEmpty() {
		mockCommandObject(WidgetDependentsCommand)
		def cmd1 = new WidgetDependentsCommand()

		cmd1.ids = []
		assert !cmd1.validate()
		assert 'validator.invalid' == cmd1.errors.getFieldError('ids').code
	}

	void testExportCommandMatchesRequirements() {
		mockCommandObject(WidgetExportCommand)
		def cmd1 = new WidgetExportCommand()
		cmd1.id = 'GARBAGE'

		assert !cmd1.validate()
		assert 'matches.invalid' == cmd1.errors.getFieldError('id').code
	}

	void testExportCommandNotBlankRequirements() {
		mockCommandObject(WidgetExportCommand)
		def cmd1 = new WidgetExportCommand()
		cmd1.filename = ''
		cmd1.id = ''

		assert !cmd1.validate()
		assert 'blank' == cmd1.errors.getFieldError('filename').code
		assert 'blank' == cmd1.errors.getFieldError('id').code
	}

	void testExportCommandNotNullRequirements() {
		mockCommandObject(WidgetExportCommand)
		def cmd1 = new WidgetExportCommand()

		assert !cmd1.validate()
		assert 'nullable' == cmd1.errors.getFieldError('filename').code
		assert 'nullable' == cmd1.errors.getFieldError('id').code
	}
}
