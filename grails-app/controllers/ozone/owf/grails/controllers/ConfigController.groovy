package ozone.owf.grails.controllers

import grails.converters.JSON

/**
 * User controller.
 */
class ConfigController {

	def accountService
	def grailsApplication
	def preferenceService
	def themeService
	def serviceModelService

	def config = {
		def curUser = accountService.getLoggedInUser()

		def widgetNamesResults = preferenceService.show([namespace: 'owf.custom.widgetprefs', path: 'widgetNames'])
		def widgetNames = widgetNamesResults.preference?.value ? widgetNamesResults.preference.value : [:] as JSON
		def bannerStateResults = preferenceService.show([namespace: 'owf.banner', path: 'state'])
		def bannerState = bannerStateResults.preference?.value ? bannerStateResults.preference.value : [:] as JSON

		def pDate = new Date()
		def pDateString = null
		if (curUser.prevLogin != null) {
			pDate = curUser.prevLogin
		}
		pDateString = prettytime.display(date: pDate).toString()
		if ("1 day ago".equalsIgnoreCase(pDateString)) { pDateString = 'Yesterday' }

		def emailString = curUser.email != null ? curUser.email : ''

		def isAdmin = accountService.getLoggedInUserIsAdmin()

		def curUserResult = [displayName: curUser.username, userRealName: curUser.userRealName,
					prevLogin: pDate, prettyPrevLogin: pDateString, id: curUser.id, groups: [], email: emailString,
					isAdmin: isAdmin] as JSON

		def themeResults = themeService.getCurrentTheme()
		def theme = [:]

		//use only key information
		theme["themeName"] = themeResults["name"]
		theme["themeContrast"] = themeResults["contrast"]
		theme["themeFontSize"] = themeResults["owf_font_size"]

		//copy owf section of grails config, removing sensitive properties
		def conf = grailsApplication.config.owf.clone()
		conf.customHeaderFooter.header = conf.customHeaderFooter.header.value.toString().replace('/context', request.contextPath)
		conf.metric = conf.metric.findAll {
			! (it.key in ['keystorePass', 'truststorePass', 'keystorePath', 'truststorePath'])
		}

		render(view: 'config_js',
				model: [
					user: curUserResult,
					widgetNames: widgetNames,
					bannerState: bannerState,
					currentTheme: theme as JSON,
					conf: conf as JSON
				],
				contentType: 'text/javascript')
	}
}
