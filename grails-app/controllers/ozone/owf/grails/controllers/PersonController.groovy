package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException
import ozone.owf.grails.OwfExceptionTypes

class PersonController extends BaseOwfRestController {

	def accountService
	def administrationService

	def index = {
		redirect action: list, params: params
	}

	def list = { PersonListCommand cmd ->
		def statusCode
		def jsonResult

		try {
			if (!accountService.getLoggedInUserIsAdmin()) {
				throw new OwfException(message: 'You are not authorized to see a list of users in the system.', exceptionType: OwfExceptionTypes.Authorization)
			}
			if (cmd.hasErrors()) {
				throw new OwfException(message: 'List command object has invalid data.', exceptionType: OwfExceptionTypes.InputValidation)
			}

			def result = administrationService.listUsersForUserAdminWidget(cmd)
			statusCode = 200
			jsonResult = result as JSON
		}
		catch (OwfException owe) {
			handleError(owe)
			statusCode = owe.exceptionType.normalReturnCode
			jsonResult = "Error during listUsersForUserAdminWidget: " + owe.exceptionType.generalMessage + " " + owe.message
		}

		renderResult(jsonResult, statusCode)
	}

	def delete = {
		if (params.data) {
			params.personUserIDsToDelete = JSON.parse(params.data)
		}

		// Hack to make this work until we have a proper command object in place.
		if (params.personUserIDsToDelete == null) {
			throw new OwfException(message: 'A fatal validation error occurred. personUserIDsToDelete param required. Params: ' + params.toString(),
			exceptionType: OwfExceptionTypes.Validation)
		}

		// Yep, another hack.
		List<Long> lstPersonIdsToDelete = params.personUserIDsToDelete.collect { it.id as long }
		params.adminEnabled = true
		def statusCode
		def jsonResult
		def result
		def methodName

		try {
			if (params.personUserIDsToDelete) {
				methodName = "bulkDeleteUsersForAdmin"
				result = accountService.bulkDeleteUsersForAdmin(lstPersonIdsToDelete)
			}

			// Third time's the charm?  Another hack to keep the API constant for now.
			result.data = params.personUserIDsToDelete
			jsonResult = [msg: result as JSON, status: 200]
		}
		catch (Exception e) {
			jsonResult = handleError(e)
		}

		renderResult(jsonResult)
	}

	/**
	 * Person save action.
	 */
	def createOrUpdate = {
		def result

		try {
			def results = accountService.createOrUpdate(params)
			result = [msg: results as JSON, status: 200]
		}
		catch (Exception e) {
			result = handleError(e)
		}

		renderResult(result)
	}

	def whoami = {
		def curUser = accountService.getLoggedInUser()
		def jsonResult = [currentUserName: curUser.username, currentUser: curUser.userRealName, currentUserPrevLogin: curUser.prevLogin, currentId: curUser.id] as JSON
		renderResult(jsonResult, 200)
	}
}

class PersonListCommand {
	String filters
	String filterOperator
	Integer offset
	Integer max
	Long id
	Long group_id
	String widget_id
	String sort
	String order

	static constraints = {
		max nullable: false, min: 1
		offset nullable: false, min: 0
		sort nullable: false, blank: false
		order nullable: false, blank: false, inList: ['ASC', 'DESC']
		filterOperator nullable: true, blank: false, inList: ['AND', 'OR']
		id nullable: true, validator: { val, obj ->
			(val != null && obj.group_id == null && obj.widget_id == null) || val == null
		}
		group_id nullable: true, validator: { val, obj ->
			(val != null && obj.id == null && obj.widget_id == null) || val == null
		}
		widget_id nullable: true, blank: false,
				matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/,
				validator: { val, obj ->
					(val != null && obj.id == null && obj.group_id == null) || val == null
				}
	}
}
