package ozone.owf.grails.controllers

import grails.test.mixin.TestFor

@TestFor(AboutController)
class AboutControllerTests {

	void testIndexData() {
		controller.index()
		assert view == "/about/about"

		// These come from the OwfConfig.groovy file, so if you get a failure, check there.
		assert model.message.contains('OZONE Widget Framework')
		assert model.support.contains('This is a Beta version and has not been fully tested')

		// These come from application.properties, so if you get a failure, check there.
		assert model.appVersion == '7.5'
		assert model.grailsVersion == '2.1.1'
	}
}
