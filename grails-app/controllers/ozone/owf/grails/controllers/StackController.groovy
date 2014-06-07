package ozone.owf.grails.controllers

import ozone.owf.grails.OwfException
import grails.converters.JSON

class StackController extends BaseOwfRestController {
    
    def stackService


    def list = {
        def statusCode
        def jsonResult

        try {
            def result = stackService.list(params)
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
    
    def createOrUpdate = {
        def jsonResult

        try {
            def result = stackService.createOrUpdate(params)
            jsonResult = [msg: result as JSON, status: 200 ]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }
        
        renderResult(jsonResult)
        

	
    }
    
    def delete = {
        def jsonResult

        try {
            def result = stackService.delete(params)
            jsonResult = [msg: result as JSON, status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }
        
        renderResult(jsonResult)
        

    }
    
    def export = {
        def result

        try {
            def filename = params.filename ? params.filename :"stack_descriptor"

            //Set content-disposition so browser is expecting a file
            response.setHeader("Content-disposition", "attachment; filename=" + filename + ".html")

            def stackDescriptor = stackService.export(params)
            response.outputStream.write(stackDescriptor.getBytes("UTF-8"))
            response.outputStream.flush()
        }
        catch (Exception e) {
            //Set content-disposition back to text to relay the error
            response.setHeader("Content-disposition", "")

            result = handleError(e)
            renderResult(result)
        }
        

    }

    def importStack = {
        def jsonResult

        try {
            def result = stackService.importStack(params)
            jsonResult = [msg: result as JSON, status: 200]
        }
        catch (Exception e) {
            jsonResult = handleError(e)
        }

        renderResult(jsonResult)
        

    }
	
	def restore = {
		def jsonResult

		try {
			def result = stackService.restore(params)
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}
		
		renderResult(jsonResult)
		

	}
}
