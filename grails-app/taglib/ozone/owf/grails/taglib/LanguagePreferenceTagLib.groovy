package ozone.owf.grails.taglib

import ozone.owf.grails.OwfException

class LanguagePreferenceTagLib {

	static namespace = 'lang'
	def preferenceService

	def preference = { attrs ->
		def retVal
		if (attrs['lang'] == null) {

			def lang = request.locale.toString()
			def result = null

			try {
				result = preferenceService.show([namespace:"owf", path:"language"])
			}
			catch (OwfException owe) {
				if ('INFO' == owe.logLevel) {
					log.info(owe)
				}
				else if ('DEBUG' == owe.logLevel) {
					log.debug(owe)
				}
				else {
					log.error(owe)
				}
			}

			//check against preference
			if (result?.preference?.value != null) {
				lang = result.preference.value
			}

			session.setAttribute("language",lang)

			retVal = """
<script type=\"text/javascript\" src=\"${resource(dir: 'js/lang', file: 'ozone-lang-'+session.getAttribute("language")+'.js', base: '.')}\"></script>
<script type=\"text/javascript\" src=\"${resource(dir: 'js-lib/ext-4.0.7/locale', file: 'ext-lang-'+ (session.getAttribute('language') == 'en_US' ? 'en' : session.getAttribute('language'))+'.js', base: '.' )}\"></script>
            """
		}
		else {
			retVal = """
<script type=\"text/javascript\" src=\"${resource(dir: 'js/lang', file: 'ozone-lang-'+attrs['lang']+'.js', base: '.')}\"></script>
<script type=\"text/javascript\" src=\"${resource(dir: 'js-lib/ext-4.0.7/locale', file: 'ext-lang-'+ (attrs['lang'] == 'en_US' ? 'en' : attrs['lang'])+'.js', base: '.' )}\"></script>
            """
		}
		out << retVal
	}

}
