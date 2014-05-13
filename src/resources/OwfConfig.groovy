import org.apache.log4j.AsyncAppender
import org.apache.log4j.RollingFileAppender
import org.apache.log4j.net.SocketAppender

// this section may modify any existing spring beans
beans {

}

metrics = [
			enable: true,
			csv: [
				enable:true,
				reportingRate:60
			],
			jmx: [
				enable:true
			]
		]

logDirectory = System.getProperty('CATALINA_NLOGS') ?: 'logs'
log4j = { rootLogger ->
	def lD = this.logDirectory

	// get rid of stdout logging
	rootLogger.removeAllAppenders()

	def logNames = ['output', 'audit', 'access']
	def appenderList = []
	logNames.each {
		def rf = new RollingFileAppender(name: it,
				layout: pattern(conversionPattern: "%5p %d [user: %X{client_cn}] %c - %m%n"),
				maxFileSize: '50MB',
				maxBackupIndex: 10,
				file: "${lD}/log4j_${it}.log")

		def async = new AsyncAppender(name: "async${it}")
		async.addAppender(rf)

		if (it == 'access') {
			rf.layout = pattern(conversionPattern: "%m%n")
		}

		appenderList.add(rf)
		appenderList.add(async)
	}

	if (System.getProperty('logstash.host') && System.getProperty('logstash.port')) {
		def socket = new SocketAppender(
				name: "logstash",
				remoteHost: System.getProperty('logstash.host'),
				port: Integer.parseInt(System.getProperty('logstash.port'))
				)
		appenderList.add(socket)

		def asyncsocket = new AsyncAppender(name: 'asynclogstash')
		asyncsocket.blocking = false
		asyncsocket.addAppender(socket)
		appenderList.add(asyncsocket)
	}

	appenders {
		// Disable the creation of stacktrace.log since we don't log anything to it
		'null' name: 'stacktrace'
		appenderList.each {
			appender it
		}
	}

	info asyncaccess: 'ozone.owf.grails.AuditOWFWebRequestsLogger',
			asyncaudit: ['owf.security.ldap.audit.SecurityAuditLogger','grails.app.filters']
	error asyncoutput: 'org.jgroups'

	if (System.getProperty('logstash.host') && System.getProperty('logstash.port')) {
		root {
			warn 'asyncoutput', 'asynclogstash'
		}
	}
	else {
		root {
			warn 'asyncoutput'
		}
	}

	environments {
		development {
			root {
				warn asyncaccess: 'ozone.owf.grails.AuditOWFWebRequestsLogger',
						asyncaudit: ['owf.security.ldap.audit.SecurityAuditLogger','grails.app.filters']
				warn 'asyncoutput'
			}
		}

		test {
			// Turn off pretty much everything for Selenium.
			off asyncaudit: ['owf.security.ldap.audit.SecurityAuditLogger','grails.app.filters'],
			asyncaccess: 'ozone.owf.grails.AuditOWFWebRequestsLogger'

			//			debug asyncoutput: 'org.hibernate.SQL'
		}
	}
}

about {
	baseMessage = "The <b>OZONE Widget Framework</b> (OWF) is primarily a framework for visually organizing and laying out lightweight web applications (widgets) within a user's browser. The OWF provides infrastructure services to facilitate the development of analytic workflows and presentation tier application integration."
	baseNotice = "To report any bugs, please call  ...    ."
}

environments {
	development {
		mpSyncTrustAll = true
		mpSyncTrustProvidedUrl = true
		about {
			baseNotice = "This is an Alpha version and has not been fully tested. Any data entered into this system may be lost. An upgrade to the final release will not be provided. To report any bugs, please call ... "
		}

		userUpdateMillis = 5000
	}

	test {
		mpSyncTrustAll = true
		mpSyncTrustProvidedUrl = true
		about {
			baseNotice = "This is a Beta version and has not been fully tested. Any data entered into this system may be lost. An upgrade to the final release will not be provided. To report any bugs, please call ... "
		}
	}
}