package ozone.owf.grails.controllers

import ozone.owf.grails.OwfException

class IndexController {

	def accountService
	def dashboardService
	def personWidgetDefinitionService

	// Choke, cough....  When we move the definition of the service model into the same class file as the domain,
	// then we can retire this awful dependency on SMS.  Alas, that's a bigger ask than one simple performance
	// modification.
	def serviceModelService

	def index = {
		def user = accountService.getLoggedInUser()

		def dashboardsNew = []
		def widgets = []

		try {
			def rawDashboards = dashboardService.listDashboardsForSingleUser(user)
			dashboardsNew = rawDashboards.collect { dashboard ->
				def args = [:]
				args['isGroupDashboard'] = false
				dashboard.toServiceModel(args)
			}
		}
		catch (OwfException owe) {
			handleError(owe)
		}

		try {
			def personalWidgets = personWidgetDefinitionService.listWidgetsForLoggedInUser(user)
			widgets = personalWidgets.collect { pwd ->
				serviceModelService.createServiceModel(pwd)
			}
		}
		catch (OwfException owe) {
			handleError(owe)
		}

		render(view: "index", model: [dashboards: dashboardsNew, widgets: widgets])
	}
}
