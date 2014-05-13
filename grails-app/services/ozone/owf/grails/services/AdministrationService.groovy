package ozone.owf.grails.services

import grails.converters.JSON

import org.hibernate.CacheMode

import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes
import ozone.owf.grails.controllers.PersonListCommand
import ozone.owf.grails.domain.Person

class AdministrationService {

	def accountService
	def preferenceService
	def dashboardService
	def serviceModelService

	def listUsersForUserAdminWidget(PersonListCommand cmd) {
		// Handle the filter criteria generation here.  Would be a little easier, perhaps, with detached criteria
		// but they don't become available until Grails 2.0+.  Alas....
		def createFilterCriteriaForUserAdminWidget = { filters ->
			JSON.parse(filters).each {
				ilike it.filterField, "%${it.filterValue}%"
			}
		}

		def criteria = Person.createCriteria()

		// Query options
		def opts = [:]
		opts.offset = cmd.offset
		opts.max = cmd.max
		def personList = criteria.list(opts) {
			if (cmd.id) {
				eq("id", cmd.id)
			}

			// This will be assigned if we're looking at the Group Editor widget's "Users" tab.
			if (cmd.group_id) {
				groups {
					eq("id", cmd.group_id)
				}
			}

			// This will be assigned if we're looking at the Widget Editor widget's "Users" tab.
			if (cmd.widget_id) {
				personWidgetDefinitions {
					widgetDefinition {
						eq("widgetGuid", cmd.widget_id)
					}

					// only list widgets that are explicitly assigned
					// to this user
					eq("userWidget", true)
				}
			}

			if (cmd.filters) {
				// The delegate for that needs to be the delegate for this....  Meaning that the
				// criteria instance we're creating is the target for the method calls in the
				// closure we're calling here.  Got all that?  If you forget this line, you'll get
				// a nasty surprise at run time when the code goes looking for a 'between' method
				// or a 'ilike' method in the service class.
				createFilterCriteriaForUserAdminWidget.delegate = delegate
				if (cmd.filterOperator.toUpperCase() == 'OR') {
					or {
						createFilterCriteriaForUserAdminWidget(cmd.filters)
					}
				}
				else {
					createFilterCriteriaForUserAdminWidget(cmd.filters)
				}
			}

			if (cmd.sort) {
				order(cmd.sort, cmd.order.toLowerCase())
			}
			cacheMode(CacheMode.GET)
		}

		def processedList = personList.collect { p ->
			serviceModelService.createServiceModel(p, [
						totalGroups: p.groups?.size(),
						totalWidgets: p.personWidgetDefinitions?.size(),
						totalDashboards: p.dashboards?.size()
					])
		}

		return [success: true, data: processedList, results: personList.totalCount]
	}

	def updatePreference(params) {
		ensureAdmin()
		if (params.data != null) {
			def data = JSON.parse(params.data)[0]
			params.namespace = data.namespace
			params.originalNamespace = data.originalNamespace
			params.path = data.path
			params.originalPath = data.originalPath
			params.userid = Person.findByUsername(data.username).id
			params.value = data.value
		}
		if (params.namespace != params.originalNamespace || params.path != params.originalPath) {
			//Namespace and path are part of the identity of the preference.
			//If they changed 'em, we need to delete the old preference.
			def tempParams = new HashMap()
			tempParams.namespace = params.originalNamespace
			tempParams.path = params.originalPath
			tempParams.userid = params.userid
			preferenceService.delete(tempParams)
		}
		preferenceService.update(params)
	}

	def clonePreference(params) {
		ensureAdmin()
		def resultOfClone = null
		def assignedTo

		if (params.checkedTargets) {
			def checkedTargets = []
			if (params.checkedTargets.class.name != "java.lang.String") {
				params.checkedTargets.each { checkedTargets << it }
			}
			else {
				checkedTargets << params.checkedTargets
			}

			checkedTargets.each {
				resultOfClone = preferenceService.deepClone(params, it)
			}
			assignedTo = checkedTargets
		}
		else if (params.data) {
			def data = JSON.parse(params.data)[0]
			def userId = Person.findByUsername(data['username']).id
			resultOfClone = preferenceService.deepClone(data, userId)
			assignedTo = [data['username']]
		}

		return [success: true, resultOfClone: resultOfClone, assignedTo: assignedTo]
	}

	private def ensureAdmin() {
		if (!accountService.getLoggedInUserIsAdmin()) {
			throw new OwfException(message: "You must be an admin", exceptionType: OwfExceptionTypes.Authorization)
		}
	}
}
