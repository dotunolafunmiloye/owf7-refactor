package ozone.owf.grails.controllers

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray
import ozone.owf.grails.OwfException
import ozone.owf.grails.domain.Dashboard


class PrefsDashboardController extends BaseOwfRestController {

  	def dashboardService
	def modelName = 'dashboard'
	
	def show = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.show(params)
			statusCode = 200
            jsonResult = result.dashboard as JSON
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
			def result = dashboardService.list(params)
			statusCode = 200
            jsonResult = result.dashboardList as JSON
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
			def result = dashboardService.create(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during create: " + owe.exceptionType.generalMessage + " " + owe.message
		}


				
		renderResult(jsonResult, statusCode)
	}

	def update = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.update(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during update: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)
	}

	def delete = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.delete(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during delete: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)
	}
	
    def restore = {
      def statusCode
      def jsonResult

      try {
          def result = dashboardService.restore(params)
          statusCode = 200
          jsonResult = result.dashboard as JSON
      }
      catch (OwfException owe) {
          handleError(owe)
          statusCode = owe.exceptionType.normalReturnCode
          jsonResult = "Error during restore: " + owe.exceptionType.generalMessage + " " + owe.message
      }
	  


      renderResult(jsonResult, statusCode)
    }

	def bulkDeleteAndUpdate = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.bulkDeleteAndUpdate(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkDeleteAndUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)
	}

	def bulkUpdate = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.bulkUpdate(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)
	}

	def bulkDelete = {	
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.bulkDelete(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during bulkDelete: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)	
	}
  

  	def getdefault = {
		def statusCode
		def jsonResult

		try {
			def result = dashboardService.getDefault(params)
			statusCode = 200
			jsonResult = getJsonResult(result, modelName, params)
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during getDefault: " + owe.exceptionType.generalMessage + " " + owe.message
		}
		

		
		renderResult(jsonResult, statusCode)
  	}

//  	def setdefault = {
//		def statusCode
//		def jsonResult
//        StopWatch stopWatch = null;
//
//        if (log.isInfoEnabled()) {
//          stopWatch = new StopWatch();
//          stopWatch.start();
//          log.info("Executing dashboardService: setDefault");
//        }
//		try {
//			def result = dashboardService.setDefault(params)
//			statusCode = 200
//			jsonResult = getJsonResult(result, modelName, params)
//		}
//		catch (OwfException owe) {
//			handleError(owe)
//			statusCode = owe.exceptionType.normalReturnCode
//			jsonResult = "Error during setDefault: " + owe.exceptionType.generalMessage + " " + owe.message
//		}
//		
//		if (log.isInfoEnabled()) {
//			log.info("Executed dashboardService: setDefault in "+stopWatch);
//		}
//		
//		renderResult(jsonResult, statusCode)
//  	}
}