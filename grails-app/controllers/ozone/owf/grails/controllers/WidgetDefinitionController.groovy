package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException

class WidgetDefinitionController extends BaseOwfRestController {
	
    def accountService
    def widgetDefinitionService
	
    def modelName = 'widgetDefinition'
	
    def show = {
        def statusCode
        def jsonResult

        try {
            def result = widgetDefinitionService.show(params)
            statusCode = 200
            jsonResult = getJsonResult(result, modelName, params)
        }
        catch (OwfException owe) {
            handleError(owe)
            statusCode = owe.exceptionType.normalReturnCode
            jsonResult = "Error during show: " + owe.exceptionType.generalMessage + " " + owe.message
        }
		
        renderResult(jsonResult, statusCode)
		

    }

    def list = {
        def statusCode
        def jsonResult

        try {
          def result = widgetDefinitionService.list(params)
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
	
    def create = {
        def statusCode
        def jsonResult

        try {
            def result = widgetDefinitionService.createOrUpdate(params)
            jsonResult = [msg: getJsonResult(result, modelName, params), status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }
		
        renderResult(jsonResult)
		

    }
	
    def update = {
        def statusCode
        def jsonResult

        try {
            def result = widgetDefinitionService.createOrUpdate(params)
            jsonResult = [msg: getJsonResult(result, modelName, params), status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }
		
        renderResult(jsonResult)
		

    }
	
    def delete = {
        def statusCode
        def jsonResult

        try {
            def result = widgetDefinitionService.delete(params)
            jsonResult = [msg: result as JSON, status: 200]
        }
        catch (Exception e)
        {
            jsonResult = handleError(e)
        }
		
        renderResult(jsonResult)
		

    }

    def bulkDelete = {
        def statusCode
        def jsonResult

        try {
            def result = widgetDefinitionService.bulkDelete(params)
            jsonResult = [msg: result as JSON, status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }
		
        renderResult(jsonResult)
		

    }
    
    def dependents = {
        
        def jsonResult

        try
        {
            def result = widgetDefinitionService.getDependents(params)
            
            jsonResult = [msg: result as JSON, status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
            
        }
        
        renderResult(jsonResult)
        

    }
}