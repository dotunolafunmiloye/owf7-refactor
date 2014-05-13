package ozone.owf.grails.controllers

import grails.converters.JSON

import org.codehaus.groovy.grails.web.json.JSONArray

import ozone.owf.grails.OwfException

class AdministrationController extends BaseOwfRestController {

	def accountService
	def widgetDefinitionService
	def administrationService
	def preferenceService
	def dashboardService
	def personWidgetDefinitionService

	def admin = {
		render(view: 'admin', model: [accountService: accountService])
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

	def addCopyPreferenceSubmit = {
		def statusCode
		def jsonResult

		try {
			def result = administrationService.clonePreference(params)
			statusCode = 200
			jsonResult = [success: result.success, namespace: result.resultOfClone.preference.namespace, path: result.resultOfClone.preference.path, assignedCount: result.assignedTo.size()] as JSON
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
			if (result.count != null) {
				jsonResult = [success:result.success, results: result.count, rows : preferenceList] as JSON
			}
			else {
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
			if (params.preferencesToDelete != null && params.preferencesToDelete.length() > 0) {
				methodName = "bulkDeleteForAdmin"
				result = preferenceService.bulkDeleteForAdmin(params)
			}
			else if (params.data != null && params.data.length() > 0) {
				methodName = "bulkDeleteForAdmin"
				params.preferencesToDelete = params.data
				result = preferenceService.bulkDeleteForAdmin(params)
			}
			else {
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
}
