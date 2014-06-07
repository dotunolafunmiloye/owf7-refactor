import grails.util.Environment

import org.apache.ivy.plugins.resolver.*

System.setProperty "ivy.checksums", ""

def resolverConfig = new ConfigSlurper().parse(new File("${userHome}/.ozone/all-build.groovy").toURL()).merge(
		new ConfigSlurper().parse(new File("${userHome}/.ozone/${appName}-build.groovy").toURL())
		);
ozone.resolvers=resolverConfig.ozone.resolvers ?: [];

grails.project.plugins.dir="${basedir}/plugins"
grails.work.dir='.grails-work'

grails.config.base.webXml="file:///${basedir}/src/resources/web_no_cas.xml"
// cas stuff added via the ozone-deploy plugin

coverage {
	exclusions = [
		"**/org/apache/log4j/**",
		"changelog*/**"
	]
}


// Load some of our dependency info from ivy.properties.
// This way we can keep our ant build and groovy build in sync

def props = new Properties()
new File("application.properties").withInputStream {
	stream -> props.load(stream)
}
def config = new ConfigSlurper().parse(props)

// This closure is passed the command line arguments used to start the
// war process.
grails.war.copyToWebApp = { args ->
	//certain dev files don't need to be in a production war
	if (Environment.current == Environment.PRODUCTION) {
		fileset(dir: "web-app") {
			exclude(name:"themes/*_all*.*")
			exclude(name:"themes/template/**")
			exclude(name:"themes/**/sass/**")
			exclude(name:"examples/**")

			exclude(name: "js-min/**")

			exclude(name: "js-lib/ext-4.0.7/ext-all-debug-w-comments.js")
			exclude(name: "js-lib/ext-4.0.7/ext-all-dev.js")
			exclude(name: "js-lib/ext-4.0.7/ext-dev.js")
			exclude(name: "js-lib/ext-4.0.7/build/**")
			exclude(name: "js-lib/ext-4.0.7/builds/**")
			exclude(name: "js-lib/ext-4.0.7/deploy/**")
			exclude(name: "js-lib/ext-4.0.7/docs/**")
			exclude(name: "js-lib/ext-4.0.7/examples/**")
			exclude(name: "js-lib/ext-4.0.7/jsbuilder/**")
			exclude(name: "js-lib/ext-4.0.7/overview/**")
			exclude(name: "js-lib/ext-4.0.7/pkgs/**")
			exclude(name: "js-lib/ext-4.0.7/resources/themes/images/access/**")
			exclude(name: "js-lib/ext-4.0.7/resources/themes/images/gray/**")
			exclude(name: "js-lib/ext-4.0.7/src/**")
			exclude(name: "js-lib/ext-4.0.7/welcome/**")

			if (!System.properties.includeJsTests || !System.properties.includeJsTests.toString().toBoolean()) {
				exclude(name: "js-doh/**")
				exclude(name: "js-lib/dojo-release-*/**")
			}
		}
	}
	else {
		//not production include all files
		fileset(dir: "web-app") {
		}
	}
}

//these may be ant file patterns
def warExcludes = [
	'WEB-INF/lib/ant-1.7.0.jar',
	'WEB-INF/lib/ant-launcher-1.7.0.jar',
	'WEB-INF/lib/log4j-1.2.16.jar',
	'WEB-INF/classes/owf-log4j.xml',
	'WEB-INF/templates/**',
	'WEB-INF/tools/**',
	'**/*.rb',
	'**/*.scss',
	'**/.gitignore',
	'images/**',
	'login/**',
	'WEB-INF/lib/tomcat*.jar',
	// this class is only used for development to simulate login so we don't need cas
	// remove it from the war so it doesn't get out
	'WEB-INF/classes/AutoLoginAccountService.class',
	'WEB-INF/lib/*-sources.jar',
	'WEB-INF/lib/*-javadoc.jar',
	'WEB-INF/lib/jetty-6.1.21.jar',
	'WEB-INF/lib/jetty-naming-6.1.21.jar',
	'WEB-INF/lib/jetty-plus-6.1.21.jar',
	'WEB-INF/lib/jetty-util-6.1.21.jar',
	"WEB-INF/lib/owf-security-extras-*.zip",
	"WEB-INF/lib/owf-security-project-*.zip",
	'WEB-INF/lib/servlet-api-2.5-20081211.jar',
	'WEB-INF/lib/jasper-compiler-5.5.15.jar',
	'WEB-INF/lib/jasper-compiler-jdt-5.5.15.jar',
	'WEB-INF/lib/jasper-runtime-5.5.15.jar',
	'WEB-INF/lib/jsp-api-2.0-6.1.21.jar',
	'WEB-INF/lib/standard-1.1.2.jar',
	'WEB-INF/lib/core-3.1.1.jar',
	'WEB-INF/lib/log4j-1.2.9.jar',
	'WEB-INF/lib/spring-expression-3.0.1.RELEASE.jar',
	'plugins/**'

]
grails.war.resources = { stagingDir ->
	delete(dir: "${stagingDir}", includeemptydirs: true) {
		warExcludes.each {exclude ->
			include(name: "${exclude}")
		}
	}
}

grails.project.dependency.resolution = {
	def isDisconnected = System.getenv('OZONE_DISCONNECTED_BUILD') ?: System.getProperty('OZONE_DISCONNECTED_BUILD') ?:resolverConfig.ozone.isOffline

	// inherit Grails' default dependencies
	inherits( "global" ) {
		// uncomment to disable ehcache
		// excludes 'ehcache'
		excludes 'slf4j'
		excludes 'commons-logging'
	}
	log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
	repositories {

		if (isDisconnected) {
			println "Disconnected build using ${ozone.resolvers}"
			inherit false
		} else {
			println "Online build using ${ozone.resolvers}"
		}
		grailsPlugins()
		grailsHome()

		ozone.resolvers.each {
			resolver it
		}
	}
	dependencies {
		// specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

		compile ( 'org.springframework.security:spring-security-core:3.0.2.RELEASE',
				"${config.owf.security.org}:${config.owf.security.module}:7.6.+",
				'unboundid:unboundid-ldapsdk:se'
				) {
					excludes 'servlet-api', 'spring-aop','spring-beans', 'spring-core', 'spring-context', 'spring-tx', 'spring-web', 'jstl', 'jsp', 'standard',
							'jasper-compiler', 'jasper-compiler-jdt', 'jasper-runtime', 'spring-jdbc', 'spring-test', 'cglib-nodep', 'ehcache', 'ehcache-parent',
							'jsr250-api', 'log4j'
				}


		runtime (
				//needed for code-coverage plugin
				//'asm:asm:3.3.1',
				//'net.sourceforge.cobertura:cobertura:1.9.4.1',
				//needed for code-coverage plugin

				'org.jasig.cas:cas-client-core:3.1.3',
				'log4j:apache-log4j-extras:1.1',
				'log4j:log4j:1.2.17',
				'org.springframework.security:spring-security-core:3.0.2.RELEASE',
				'org.springframework.security:spring-security-cas-client:3.0.2.RELEASE',
				'org.springframework.security:spring-security-config:3.0.2.RELEASE',
				'net.sf.ehcache:ehcache-jgroupsreplication:1.4',

				//ldap dependencies
				'org.springframework.security:spring-security-ldap:3.0.2.RELEASE'
				){
					excludes  'spring-aop','spring-beans', 'spring-core', 'spring-context', 'spring-tx', 'spring-web', 'jstl', 'jsp', 'standard',
							'jasper-compiler', 'jasper-compiler-jdt', 'jasper-runtime', 'bsh'
				}

		test (
				'webtops:OzonePerfData:1.0'
			 )

		//only include these jdbc drivers for non production
		if (Environment.current != Environment.PRODUCTION) {
			runtime 'com.oracle:ojdbc14:10.2.0.1.0'
			runtime 'mysql:mysql-connector-java:5.1.6'
			runtime 'net.sourceforge.jtds:jtds:1.2.4'
			runtime 'postgresql:postgresql:8.4-701.jdbc3'
		}

		// HTTP Client
		compile('org.apache.httpcomponents:httpcore:4.1.1', 'org.apache.httpcomponents:httpclient:4.1.1')

		// Webtops Deps
		def yammerVersion="3.0.0-BETA1"
		compile(
				"com.yammer.metrics:metrics-core:${yammerVersion}",
				"com.yammer.metrics:metrics-jvm:${yammerVersion}"
				) {
					transitive = false
//					excludes 'slf4j'
				}
		compile('ozone:ozone-metric-tools:1.0.+') {
			changing = true
//					excludes 'slf4j'
		}

	}

}
