
println "User home is ${userHome}"

def resolverConfig = new ConfigSlurper().parse(new File("${userHome}/.ozone/all-build.groovy").toURL()).merge(
		new ConfigSlurper().parse(new File("${userHome}/.ozone/owf-server-build.groovy").toURL())
		)
ozone.resolvers = resolverConfig.ozone.resolvers ?: []

def props = new Properties()
new File("application.properties").withInputStream { stream ->
	props.load(stream)
}
def config = new ConfigSlurper().parse(props)

grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
grails.project.war.file = "target/${appName}-${appVersion}.war"
// Eliminate the base web.xml spec when we have the plugin (webxml) working.
//grails.config.base.webXml = "file:///${basedir}/src/resources/web_no_cas.xml"

// This closure is passed the command line arguments used to start the war process.
grails.war.copyToWebApp = { args ->
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

	inherits( "global" )
	checksums false
	log "warn"
	repositories {
		if (isDisconnected) {
			flatDir name: 'Zipped Plugins', dirs: "${basedir}/.zips"
			println "| Disconnected build using ${ozone.resolvers}"
			inherit false
		}
		else {
			println "| Online build using ${ozone.resolvers}"
		}
		grailsPlugins()
		grailsHome()

		ozone.resolvers.each { resolver it }
	}

	dependencies {
		runtime 'mysql:mysql-connector-java:5+', 'net.sf.ehcache:ehcache-jgroupsreplication:1.4'
		compile ('org.springframework.security:spring-security-core:3.0.+', 'org.springframework.security:spring-security-web:3.0.+', 'org.springframework.security:spring-security-config:3.0.+') {
			excludes 'commons-logging'
		}
		//compile ("${config.owf.security.org}:${config.owf.security.module}:7.6.+", 'unboundid:unboundid-ldapsdk:se') {
		//	transitive = false
		//}

		// HTTP Client
		compile('org.apache.httpcomponents:httpcore:4.1.1', 'org.apache.httpcomponents:httpclient:4.1.1') {
			transitive = false
		}

		// Webtops Deps
		def yammerVersion = "3+"
		compile("com.yammer.metrics:metrics-core:${yammerVersion}", "com.yammer.metrics:metrics-jvm:${yammerVersion}" ) {
			excludes 'slf4j-api'
		}
		//compile('ozone:ozone-metric-tools:1.1+') {
		//	excludes 'slf4j-api'
		//	changing = true
		//}
	}

	plugins {
		build ":tomcat:$grailsVersion"
		//		build ':compass:1.1'
		// See the webxml plugin below....
	//	build ':ozone-deploy:0.1'
		//		build ':webxml:1.4+'
		build (':code-coverage:1.2.5') {
			excludes 'log4j', 'ant'
		}

		compile ':cache:1.0.0'
                compile 'org/ozoneplatform:aml-commons-security:3.2.0'


		// Remove UIPerformance when we're ready to go with the native resources plugin.
		runtime ':ui-performance:1.2.2'
		// Unremark this when we're ready to move to JQuery.
		//		runtime ":jquery:1.8.0"
		// Unremark these when we're ready to move off UI Performance
		//		compile ":resources:1.1.6"
		//		runtime ":zipped-resources:1.0"
		//		runtime ":cached-resources:1.0"
		//		runtime ":yui-minify-resources:0.1.4"
		runtime ':pretty-time:0.3'
		runtime ":hibernate:$grailsVersion"
		// Remove DatabaseMigration 1.0 when we have the dependency for version 1.1 worked out.  See below.
		runtime ':database-migration:1.0'
		// Unremark this when we have the Liquibase 2.0.5 JAR (v. 2.0.1 which comes with the 1.0 version of the plugin).
		//        runtime ":database-migration:1.1"

		test ':build-test-data:1.1.0'
	}
}
