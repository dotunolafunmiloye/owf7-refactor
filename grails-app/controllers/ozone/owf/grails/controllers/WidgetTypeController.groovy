package ozone.owf.grails.controllers

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import ozone.owf.grails.OwfException

class WidgetTypeController extends BaseOwfRestController {
	def modelName = 'widgetType'
	def widgetTypeService
	
	def list = {
		def statusCode
        def jsonResult
        try {
          def result = widgetTypeService.list(params)
          statusCode = 200
          jsonResult = result as JSON
        }
        catch (OwfException owe) {
            handleError(owe)
            statusCode = owe.exceptionType.normalReturnCode
            jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
        }
		
        renderResult(jsonResult, statusCode)
	}
}