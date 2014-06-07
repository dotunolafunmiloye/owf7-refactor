package ozone.owf.grails.controllers

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.domain.Person

class AdministrationController extends BaseOwfRestController {
	
    def accountService
	def widgetDefinitionService
	def administrationService
	def preferenceService
	def dashboardService
	def personWidgetDefinitionService

	def admin= {
		render(view:'admin',model:[accountService:accountService])
	}
	
	/*
	 *	    Widget Definitions
	 */
	
	def listWidgetDefinitions = {
			def statusCode
			def jsonResult

			try {
				def result = widgetDefinitionService.list(params)
				statusCode = 200
				def gotFromTargetProperty = result.get('widgetDefinition')
				def widgetDefinitionList = new JSONArray()
				result.widgetDefinition.collect { widgetDefinitionList.add(serviceModelService.createServiceModel(it)) }
				if (result.count != null)
				{
					jsonResult = [success:result.success, results: result.count, data : widgetDefinitionList] as JSON
				}
				else
				{
					jsonResult = widgetDefinitionList as JSON
				}
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def deleteWidgetDefinitions = {
			def statusCode
			def jsonResult
			def result
			def methodName = null
			def modelName = 'widgetDefinition'

			try {
				if (params.widgetGuidsToDelete != null && params.widgetGuidsToDelete.length() > 0)
				{
					methodName = 'bulkDelete'
					result = widgetDefinitionService.bulkDelete(params)
				}
				else
				{
					methodName = 'delete'
					result = widgetDefinitionService.delete(params)
				}
				statusCode = 200
				def gotFromTargetProperty = result.get(modelName)
				jsonResult = getJsonResult(result, modelName, params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during " + methodName + ": " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def editWidgetDefinition = {
      def model = administrationService.getWidgetDefinitionEditEntities(params);
      def configMap = [
              allUsers: model.allUsers,
              readOnly: model.readOnly,
              actionType: model.actionType,
              createOrUpdateWidgetDefinitionSubmitUrl: g.createLink(controller:'administration', action:'createOrUpdateWidgetDefinition'),
              widgetDefinition: serviceModelservice.createServiceModel(model.widgetDefinition)
      ];

      render configMap as JSON
	}
	
	def createOrUpdateWidgetDefinition = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = administrationService.createOrUpdateWidgetDefinition(params)
				statusCode = 200
				def createdList = new JSONArray()
				result.created.collect { createdList.add(Person.get(it.id).username) }
				def updatedList = new JSONArray()
				result.updated.collect { updatedList.add(Person.get(it.id).username) }
				def deletedList = new JSONArray()
				result.deleted.collect { deletedList.add(Person.get(it).username) }
				jsonResult = [success:result.success, widgetDefinition: serviceModelService.createServiceModel(result.widgetDefinition), created:createdList , deleted:deletedList , updated:updatedList] as JSON
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode

				if (owe.exceptionType.equals(OwfExceptionTypes.Validation_UniqueConstraint)) {
					jsonResult = [
								title: 'Error',
								message: owe.message
							] as JSON
				}
				else {
					jsonResult = "Error during createOrUpdateWidgetDefinition: " + owe.exceptionType.generalMessage + " " + owe.message
				}
			}

			renderResult(jsonResult, statusCode)
	}
	
	/*
	 *	    Preferences
	 */
	
	def editPreference = {
      def model = administrationService.getPreferenceEditEntities(params);
      def configMap = [
              preference_path: model.preference.path,
              preference_user_id: model.preference.user.id,
              preference_user_username: model.preference.user.username,
              preference_namespace: model.preference.namespace,
              editPreferenceSubmitUrl: g.createLink(controller:'administration', action:'updatePreference'),
              wdFormPanel_preference_path_value: model.preference.path,
              wdFormPanel_preference_namespace_value: model.preference.namespace,
              wdFormPanel_preference_value_value:model.preference.value
      ];

      render configMap as JSON
	}
	
	def updatePreference = {
			def statusCode
			def jsonResult

			try {
				def result = administrationService.updatePreference(params)
				statusCode = 200
				jsonResult = getJsonResult(result, 'preference', params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during updatePreference: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def addCopyPreference = {

      def model = administrationService.getPreferenceAddCopyEntities(params);
      def configMap = [
              preference_path: model.preference.path,
              preference_user_id: model.preference.user.id,
              preference_user_username: model.preference.user.username,
              preference_namespace: model.preference.namespace,
              allUsers: model.allUsers,
              actionType: model.actionType,
              addCopyPreferenceSubmitUrl: g.createLink(controller:'administration', action:'addCopyPreferenceSubmit'),
              wdFormPanel_preference_path_value: model.preference.path,
              wdFormPanel_preference_namespace_value: model.preference.namespace,
              wdFormPanel_preference_value_value:model.preference.value
      ];

      render configMap as JSON
	}
	
	def addCopyPreferenceSubmit = {
			def statusCode
			def jsonResult

			try {
				def result = administrationService.clonePreference(params)
				statusCode = 200
				jsonResult = [success:result.success, namespace: result.resultOfClone.preference.namespace, path:result.resultOfClone.preference.path,  assignedCount: result.assignedTo.size() ] as JSON
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during clonePreference: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def listPreferences = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = preferenceService.list(params)
				statusCode = 200
				def preferenceList = new JSONArray()
				result.preference.collect { preferenceList.add(serviceModelService.createServiceModel(it)) }
				if (result.count != null)
				{
					jsonResult = [success:result.success, results: result.count, rows : preferenceList] as JSON
				}
				else
				{
					jsonResult = preferenceList as JSON
				}
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def deletePreferences = {
			params.adminEnabled = true

			def statusCode
			def jsonResult
			def result
			def methodName

			try {
				if (params.preferencesToDelete != null && params.preferencesToDelete.length() > 0)
				{
					methodName = "bulkDeleteForAdmin"
					result = preferenceService.bulkDeleteForAdmin(params)
				}
				else if (params.data != null && params.data.length() > 0)
				{
					methodName = "bulkDeleteForAdmin"
					params.preferencesToDelete = params.data
					result = preferenceService.bulkDeleteForAdmin(params)
				}
				else
				{
					methodName = "delete"
					result = preferenceService.delete(params)
				}

				statusCode = 200
				jsonResult = getJsonResult(result, 'preference', params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during " + methodName + ": " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	
	/*
	 *		Dashboards
	 */
	
	def editDashboard = {

      def model = administrationService.getDashboardEditEntities(params);
      def configMap = [
              dashboard_name: model.dashboard.name,
              dashboard_user_username: model.dashboard.user.username,
              actionType: model.actionType,
              editDashboardSubmitUrl: g.createLink(controller: 'administration', action: 'updateDashboard'),
              wdFormPanel_dashboard_name_value: model.dashboard.name,
              wdFormPanel_dashboard_guid_value: model.dashboard.guid,
              wdFormPanel_dashboard_layout_value: model.dashboard.layout.toUpperCase()[0] + model.dashboard.layout[1..-1],
              wdFormPanel_dashboard_isdefault_checked: model.dashboard.isdefault,

              //this is a string field which has json as a string in it - yes it is weird
              wdFormPanel_dashboard_state_value: model.dashboard.state.collect {serviceModelService.createServiceModel(it) as JSON}.toString()
      ];

      render configMap as JSON
	}
	
	def listDashboards = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = dashboardService.list(params)
				statusCode = 200
				def dashboardList = new JSONArray()
				if (result.count != null)
				{
					jsonResult = [success:result.success, results: result.count, rows : result.dashboardList] as JSON
				}
				else
				{
					jsonResult = dashboardList as JSON
				}
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def updateDashboard = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = dashboardService.update(params)
				statusCode = 200
				jsonResult = getJsonResult(result, "dashboard", params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during update: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def deleteDashboards = {
			params.adminEnabled = true

			def statusCode
			def jsonResult
			def result
			def methodName

			try {
				if (params.viewGuidsToDelete != null && params.viewGuidsToDelete.length() > 0)
				{
					methodName = "bulkDeleteForAdmin"
					result = dashboardService.bulkDeleteForAdmin(params)
				}
				else
				{
					methodName = "delete"
					result = dashboardService.delete(params)
				}

				statusCode = 200
				jsonResult = getJsonResult(result, 'dashboard', params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during " + methodName + ": " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def addCopyDashboard = {

      def model = administrationService.getDashboardAddCopyEntities(params);
      def configMap = [
              dashboard_name: model.dashboard.name,
              dashboard_user_username: model.dashboard.user.username,
              allUsers: model.allUsers,
              actionType: model.actionType,
              addCopyDashboardSubmitUrl: g.createLink(controller: 'administration', action: 'addCopyDashboardSubmit'),
              wdFormPanel_dashboard_name_value: model.dashboard.name,
              wdFormPanel_dashboard_guid_value: model.dashboard.guid,
              wdFormPanel_dashboard_isdefault_checked: model.dashboard.isdefault
      ];

      render configMap as JSON
	}
	
	def addCopyDashboardSubmit = {
			def statusCode
			def jsonResult

			try {
				def result = administrationService.cloneDashboards(params)
				statusCode = 200
				jsonResult = [success:result.success, name: result.name, assignedCount: result.assignedTo.size() ] as JSON
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during cloneDashboards: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	/*
	 *		Users (Persons)
	 */
	 
	def editPerson = {
      def model = administrationService.getPersonEditEntities(params);
      def configMap = [
              person_username: model.person.username,
              userRoleStoreUrl: g.createLink(controller:'administration', action: 'listPersonRoles'),
              userCurrentRoles: model.person.authorities,
              personFormPanelUrl: g.createLink(controller: 'administration', action: 'createOrUpdatePerson'),
              actionType: model.actionType,
              personFormPanel_username_value: model.person.username,
              personFormPanel_userRealName_value: model.person.userRealName,
              //personFormPanel_passwd_value: model.person.passwd,
              personFormPanel_email_value: model.person.email,
              roleCount: model.roleCount
      ];

      render configMap as JSON
	}
	
	def createOrUpdatePerson = {
			def statusCode
			def jsonResult

			try {
				def result = administrationService.createOrUpdatePerson(params)
				statusCode = 200
				jsonResult = [success:result.success, person:result.person, errormsg:result.errormsg] as JSON
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during createOrUpdatePerson: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def deletePersons = {
			params.adminEnabled = true

			def statusCode
			def jsonResult
			def result
			def methodName

			try {
				if (params.personUserIDsToDelete != null && params.personUserIDsToDelete.length() > 0)
				{
					methodName = "bulkDeleteUsersForAdmin"
					result = accountService.bulkDeleteUsersForAdmin(params)
				}
				else
				{
					methodName = "deleteUser"
					result = accountService.deleteUser(params)
				}

				statusCode = 200
				jsonResult = getJsonResult(result, 'person', params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during " + methodName + ": "+ owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	/*
	 *		User Roles (Person Roles)
	 */
	def listPersonRoles = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = administrationService.listPersonRoles(params)
				statusCode = 200
				def personRoleList = new JSONArray()
				result.personRoleList.each {
					personRoleList.add(it.authority)
				}

				//result.personRoleList.collect { personRoleList.add(it.toServiceModel()) }

				if (result.count != null)
				{
					jsonResult = [success:result.success, results: result.count, rows : personRoleList] as JSON
				}
				else
				{
					jsonResult = personRoleList as JSON
				}
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during listPersonRoles: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	/*
	 *	    Widget Access (Person Widget Definitions)
	 */
	
	def applyPersonWidgetDefinitions = {

      def model = administrationService.getPersonWidgetDefinitionApplyEntities(params);
      def configMap = [
              allUsers: model.allUsers,
              allWidgets: model.allWidgets,
              submitUrl: g.createLink(controller: 'administration', action: 'applyUsersAndPersonWidgetDefinitions'),
      ];

      render configMap as JSON
	}
	
	def applyUsersAndPersonWidgetDefinitions = {
			params.adminEnabled = true

			def statusCode
			def jsonResult

			try {
				def result = personWidgetDefinitionService.applyUsersAndPersonWidgetDefinitions(params)
				statusCode = 200
				jsonResult = getJsonResult(result, 'personWidgetDefinition', params)
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during applyUsersAndPersonWidgetDefinitions: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	
	def listPersonWidgetDefinitions = {
			def statusCode
			def jsonResult

			try {
				def result = personWidgetDefinitionService.listForAdmin(params)
				statusCode = 200
				def personWidgetDefinitionList = new JSONArray()
				result.personWidgetDefinitionList.collect { personWidgetDefinitionList.add(serviceModelservice.createServiceModel(it)) }
				if (result.count != null)
				{
					jsonResult = [success:result.success, results: result.count, rows : personWidgetDefinitionList] as JSON
				}
				else
				{
					jsonResult = personWidgetDefinitionList as JSON
				}
			}
			catch (OwfException owe) {
				handleError(owe)
				statusCode = owe.exceptionType.normalReturnCode
				jsonResult = "Error during listForAdmin: " + owe.exceptionType.generalMessage + " " + owe.message
			}

			renderResult(jsonResult, statusCode)
	}
	 
}
