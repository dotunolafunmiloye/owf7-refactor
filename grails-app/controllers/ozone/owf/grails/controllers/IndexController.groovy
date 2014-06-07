package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException

/**
 * User controller.
 */
class IndexController {
	def dashboardService;
	def personWidgetDefinitionService;
	def index = {
		
		def dashboardsResult,
			widgetsResult,
			dashboards = [],
			widgets = [];


		try {
			dashboardsResult =  dashboardService.list(params)
			dashboards =  dashboardsResult.dashboardList;
		}
		catch (OwfException owe) {
			handleError(owe)
		}

	
		try {
			widgetsResult = personWidgetDefinitionService.list(params);
			widgets = widgetsResult.personWidgetDefinitionList;
		}
		catch(OwfException owe) {
			handleError(owe)
		}

		render(view: "index", model: [
			dashboards: dashboards,
			widgets: widgets
		]);



	}
}
