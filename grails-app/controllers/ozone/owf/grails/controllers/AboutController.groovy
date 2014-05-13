package ozone.owf.grails.controllers

class AboutController {

	def grailsApplication

	def index = {
		def message = grailsApplication.config.about.baseMessage
		def support = grailsApplication.config.about.baseNotice
		def appVersion = grailsApplication.metadata['app.version']
		def grailsVersion = grailsApplication.metadata['app.grails.version']
		def build_number = grailsApplication.metadata['build.number']
		def today = grailsApplication.metadata['build.date']

		render(view: 'about', model: [message: message, support: support, appVersion: appVersion, grailsVersion: grailsVersion,
					build_number: build_number, today: today, commit: grailsApplication.metadata['commit.id']])
	}
}
