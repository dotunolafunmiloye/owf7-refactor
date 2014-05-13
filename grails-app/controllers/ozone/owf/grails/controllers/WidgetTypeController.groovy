package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes

class WidgetTypeController extends BaseOwfRestController {

	def modelName = 'widgetType'
	def widgetTypeService
	def accountService

	def list = {
		def statusCode
		def jsonResult
		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: "You must be an admin", exceptionType: OwfExceptionTypes.Authorization)
			}

			def result = widgetTypeService.list(params)
			statusCode = 200
			jsonResult = [success: true, results: result.size(), data: result] as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}
}