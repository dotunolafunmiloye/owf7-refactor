package ozone.owf.grails.taglib

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

class ThemeCssTagLib implements ApplicationContextAware {

	static namespace = 'owfCss'

	def themeService
	def applicationContext

	def defaultCssPath = {
		out << themeService.getCurrentTheme().css.toString()
	}

	def getCurrentThemeName = {
		out << themeService.getCurrentTheme().name.toString()
	}

	void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext
	}
}
