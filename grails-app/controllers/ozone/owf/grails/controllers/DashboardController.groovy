package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException
import org.codehaus.groovy.grails.web.json.JSONObject

class DashboardController extends BaseOwfRestController {

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
            jsonResult = [success:result.success, results: result.count, data : result.dashboardList] as JSON
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

    def createOrUpdate = {
        def statusCode
        def jsonResult
        try {

            if (params.data != null) {
                def results = []

                //this call is to update associatons
                if (params.update_action != null && params.update_action != '') {
                    params.guid = params.dashboard_id
                    results = dashboardService.addOrRemove(params)
                }
                else {
                    def dashboards = JSON.parse(params.data)
                    dashboards.each {
                        def args = [:]

                        args['name'] = it.name
                        args['guid'] = it.guid
                        args['isdefault'] = it.isdefault
                        args['locked'] = it.locked
                        
                        if (params.user_id != null && params.user_id != '') {
                            args['personId'] = params.user_id
                        }
                        args['description'] = it.description
						
                        args['layoutConfig'] = it.layoutConfig?.toString();

                        args['isGroupDashboard'] = params.isGroupDashboard ? params.isGroupDashboard?.toString()?.toBoolean() : false
                        if (args['isGroupDashboard']) {
                            args['dashboardPosition'] = 1;
                        } else {
                            args['dashboardPosition'] = it.dashboardPosition;
                        }
                        args['adminEnabled'] = params.adminEnabled ? params.adminEnabled?.toString()?.toBoolean() : false

                        //tell service to recreate state ids
                        args.regenerateStateIds = true

                        def serviceResults = dashboardService.createOrUpdate(args)
                        if (serviceResults) {
                            results << serviceModelService.createServiceModel(serviceResults.dashboard, [isGroupDashboard:args.isGroupDashboard?.toString()?.toBoolean()])
                        }
                    }
                }

                statusCode = 200
                jsonResult = [success:true,data:results] as JSON
            }
        }
        catch (OwfException owe) {
            handleError(owe)
            statusCode = owe.exceptionType.normalReturnCode
            jsonResult = "Error during createOrUpdate: " + owe.exceptionType.generalMessage + " " + owe.message
        }

       

        renderResult(jsonResult, statusCode)
    }

    def delete = {
        def statusCode
        def jsonResult
        try {
            if (params.data != null) {
                def results = []
                def dashboards = JSON.parse(params.data)
                dashboards.each {
                    def args = [:]

                    args['guid'] = it.guid
                    if (it.user_id != null) {
                        args['personId'] = it.user_id
                    }
                    args['isGroupDashboard'] = it.isGroupDashboard
                    args['adminEnabled'] = params.adminEnabled ? params.adminEnabled?.toString()?.toBoolean() : false

                    def serviceResults = dashboardService.delete(args)
                    if (serviceResults) {
                        results << serviceModelService.createServiceModel(serviceResults.dashboard, [isGroupDashboard:args.isGroupDashboard?.toString()?.toBoolean()])
                    }
                }
                jsonResult = [success:true,data:results] as JSON
            }
            else {
                def result = dashboardService.delete(params)
                jsonResult = getJsonResult(result, modelName, params)
            }
          statusCode = 200
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
            jsonResult = result as JSON
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

}
