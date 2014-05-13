package ozone.owf.grails.services

class BaseService {

	def accountService

	def routeRequest(doAsAdmin, doAsUser, params) {
		if (accountService.getLoggedInUserIsAdmin() && (params.adminEnabled?.toString()?.toBoolean())) {
			return doAsAdmin(params)
		}
		else {
			return doAsUser(params)
		}
	}

	// base CRUD operations

	def show(params) { routeRequest this.&showForAdmin, this.&showForUser, params }
	def delete(params) { routeRequest this.&deleteForAdmin, this.&deleteForUser, params }
	def update(params) { routeRequest this.&updateForAdmin, this.&updateForUser, params }

	// default empty list/show method

	def showForAdmin(params) { [] }; def showForUser(params) { [] }
}