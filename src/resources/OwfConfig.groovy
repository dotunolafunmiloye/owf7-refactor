import org.apache.log4j.AsyncAppender
import org.apache.log4j.net.SocketAppender
import org.apache.log4j.rolling.FixedWindowRollingPolicy
import org.apache.log4j.rolling.RollingFileAppender
import org.apache.log4j.rolling.SizeBasedTriggeringPolicy


// this section may modify any existing spring beans
beans {

}

metrics =[
			enable: true,
			csv: [
				enable:true,
				reportingRate:60
			],
			jmx: [
				enable:true
			]
		]

logDirectory = System.getProperty('CATALINA_NLOGS') ?: 'logs';
log4j = { rootLogger ->
	// get rid of stdout logging
	rootLogger.removeAllAppenders()

	appenders {
		// Disable the creation of stacktrace.log since we don't log anything to it
		'null' name: 'stacktrace'

		def logNames = ['output', 'audit', 'access']
		def appenderList = []
		logNames.each {
			def rf = new org.apache.log4j.rolling.RollingFileAppender(
					name: it,
					layout: pattern(conversionPattern: "%5p %d [user: %X{client_cn}] %c - %m%n"),
					rollingPolicy: new FixedWindowRollingPolicy(
					activeFileName: "${logDirectory}/log4j_${it}.log",
					fileNamePattern: "${logDirectory}/log4j_${it}.%i.log",
					minIndex:1,
					maxIndex:5),
					triggeringPolicy: new SizeBasedTriggeringPolicy(maxFileSize: 1048576 * 10)
					)

			def async = new org.apache.log4j.AsyncAppender(name: "async${it}")
			async.addAppender(rf)

			if (it == 'access') {
				rf.layout = pattern(conversionPattern: "%m%n")
			}

			appenderList.add(rf)
			appenderList.add(async)
		}

		appenderList.each {
			appender it
		}
	}

	info asyncaccess: 'ozone.owf.grails.AuditOWFWebRequestsLogger',
			asyncaudit: ['owf.security.ldap.audit.SecurityAuditLogger','grails.app.filters']

	root {
		warn 'asyncoutput'
	}

	environments {
		development {
			root {
				info 'asyncoutput'
			}
		}

		test {
			// Turn off pretty much everything for Selenium.
			off asyncaudit: ['owf.security.ldap.audit.SecurityAuditLogger','grails.app.filters'],
			asyncaccess: 'ozone.owf.grails.AuditOWFWebRequestsLogger'
		}

		production {
			if (System.getProperty('logstash.host') && System.getProperty('logstash.port')) {
				appender sa =  new SocketAppender (
						name: "Logstash",
						remoteHost: System.getProperty('logstash.host'),
						port: Integer.parseInt(System.getProperty('logstash.port'))
						)
				def async = new AsyncAppender(name: "asyncLogstash")
				async.addAppender(sa)
				async.setBlocking(false)

				root {
					warn 'asyncoutput', 'asyncLogstash'
				}
			}
		}
	}
}

environments {
	development {
		// MP Synchronization
		// Added to support server-server communication
		mpSyncTrustAll = true

		// MP Synchronization
		// Added to allow or disallow the trusting of a supplied MP URL.  Enable this
		// only when you know that the MP which serves listing information is trustworthy
		// or you could open Ozone to a deliberate "poisoning" of the widget defintions.
		mpSyncTrustProvidedUrl = true
	}

	test {
		// MP Synchronization
		// Added to support server-server communication
		mpSyncTrustAll = true

		// MP Synchronization
		// Added to allow or disallow the trusting of a supplied MP URL.  Enable this
		// only when you know that the MP which serves listing information is trustworthy
		// or you could open Ozone to a deliberate "poisoning" of the widget defintions.
		mpSyncTrustProvidedUrl = true
	}
}