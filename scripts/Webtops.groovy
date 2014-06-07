scriptEnv = "production" //default environment is prod, can be overridden with grails dev maven-deploy

includeTargets << grailsScript ('_GrailsArgParsing')
includeTargets << grailsScript ('_GrailsWar')


def tomcatDirs = ["${basedir}/../catalina-base/catalina_base_ozone", "${basedir}/../catalina-base/catalina_base_ozone2"]
def cfg = new ConfigSlurper().parse(new File("${userHome}/.ozone/all-build.groovy").toURL())

target(deploy: "Deploys WAR file to local Tomcat install (catalina_base_ozone)") {
	parseArguments()
	delete(dir: "${basedir}/target")
	delete(dir: "${basedir}/logs")
	argsMap.clear()
	war()

	cleanTomcat()
}

target(props: "Echo back all properties defined by Ant") {
	tstamp()
	property(file: "${basedir}/application.properties")
	echoproperties()
}

target(security: "Copies security configuration from security project to local Tomcat") {
	tomcatDirs.each { tomcatDir ->
		delete(dir: "${tomcatDir}/resources")
		copy(todir: "${tomcatDir}/resources") {
			fileset(dir: "${basedir}/../ozone-security/resources/owf" ) { include(name: "**/*") }
		}
	}
}

target(cleanTomcat: "Scrubs the Tomcat directory, leaving only the WAR file") {
	tomcatDirs.each { tomcatDir ->
		delete(dir: "${tomcatDir}/logs")
		delete(dir: "${tomcatDir}/temp")
		delete(dir: "${tomcatDir}/work")
		delete(dir: "${tomcatDir}/webapps/owf")

		mkdir(dir: "${tomcatDir}/temp")

		copy(tofile: "${tomcatDir}/webapps/owf.war", file: "${basedir}/target/${grailsAppName}-${metadata.getApplicationVersion()}.war", overwrite: true)
		copy(todir: "${tomcatDir}/resources", file: "${basedir}/src/resources/OwfConfig.groovy", overwrite: true)

	}
}

target(jmeterSetup: "Extracts data from Ivy to a temporary directory for use during JMeter") {
	def ivydir = System.getProperty("ivy.default.ivy.user.dir")
	def perfDataIvyUrl = "${ivydir}/cache/webtops/OzonePerfData/zips/OzonePerfData-1.0.zip"

	mkdir( dir:"${basedir}/test/jmeter/data/" )
	unzip( src:perfDataIvyUrl, dest:"${basedir}/test/jmeter/data/" )
}

target(jmeterData: "Load the database") {
	ant.exec(dir: "", executable: "cmd.exe") {
		ant.arg(line: "/c \"${cfg.mysql.binary} --user=${cfg.ozone.dbUsername} --password=${cfg.ozone.dbPassword} --host=${cfg.ozone.dbServer} --port=${cfg.ozone.dbPort} ${cfg.mysql.database} < ${basedir}/test/jmeter/data/OzonePerfData-${cfg.mysql.dumpfile}.sql\"")
	}
}

target(jmeterReport: "Run all JMeter reports for test results") {
	def parentDirs = []
	if (cfg.jmeter.ouputDirBase) {
		parentDirs.add(cfg.jmeter.ouputDirBase)
	}
	if (cfg.jmeter.scenarioOuputDirBase) {
		parentDirs.add(cfg.jmeter.scenarioOuputDirBase)
	}
	if (parentDirs.isEmpty()) {
		parentDirs.add("${basedir}/test/jmeter/performance/test-reports")
	}

	parentDirs.each { parentDir ->
		def eligibleDirs = new File("${parentDir}").listFiles(new FileFilter() {boolean accept(File pathname) {return pathname.isDirectory()}})
		eligibleDirs.each {
			def reportDir = "${it.path}/html"

			def test = new File(reportDir)
			if (!test.exists()) {
				def resultDir = "${it.path}/jtl"

				ant.mkdir(dir: "${reportDir}")
				ant.xslt(basedir: "${resultDir}", destdir: "${reportDir}", includes: "*.jtl", style: "${cfg.jmeter.home}/extras/jmeter-results-detail-report_21.xsl")
			}
		}
	}
}

target(jmeterRun: "Execute all JMeter tests") {
	ant.path(id: "ant.jmeter.classpath") {
		ant.pathelement(location: "${cfg.jmeter.home}/extras/ant-jmeter-1.0.9.jar")
	}
	ant.taskdef(name: "jmeter", classname: "org.programmerplanet.ant.taskdefs.jmeter.JMeterTask", classpathref: "ant.jmeter.classpath")

	// Filter out the flotsam in the directory to arrive at the tests of interest....
	def tests = new File("${basedir}/test/jmeter/performance").listFiles(new FilenameFilter() {
				// Run all tests.
				boolean accept(File dir, String name) {
					return name.indexOf(".jmx") > -1 &&
					name.indexOf('prep_server') == -1 &&
					name.indexOf('send_metric') == -1 &&
					name.indexOf('scenario_') == -1
				}

				// How to run a single, specific test.
				//				boolean accept(File dir, String name) {
				//					return name.indexOf("login_and_logout.jmx") > -1
				//				}
			}
			)

	def cfgResultDir = cfg.jmeter.ouputDirBase ?: null
	def cfgStampedResults = cfgResultDir ? "${cfgResultDir}/${new Date().time}/jtl" : null
	def localDir = "${basedir}/test/jmeter/performance/test-reports/jtl"
	def resultDir = cfgStampedResults ?: localDir

	ant.delete(dir: "${localDir}")
	ant.mkdir(dir: "${localDir}")
	if (resultDir != localDir) {
		ant.mkdir(dir: "${resultDir}")
	}

	// Run each test....
	tests.each { jmxFile ->
		if (jmxFile.isFile()) {
			jmeter(jmeterhome: "${cfg.jmeter.home}", resultlogdir: "${resultDir}", testplan: "${jmxFile}") {
				property(name: "hostName", value: "${cfg.jmeter.hostName}")
				property(name: "hostPort", value: "${cfg.jmeter.hostPort}")
				property(name: "casHost", value: "${cfg.jmeter.hostName}")
				property(name: "casPort", value: "${cfg.jmeter.hostPort}")
				property(name: "metricHostName", value: "${cfg.jmeter.hostName}")
				property(name: "metricHostPort", value: "${cfg.jmeter.hostPort}")
				property(name: "loopsPerUser", value: "${cfg.jmeter.loopsPerUser}")
				property(name: "loopsPerAdmin", value: "${cfg.jmeter.loopsPerAdmin}")
				property(name: "numAdmins", value: "${cfg.jmeter.numAdmins}")
				property(name: "numUsers", value: "${cfg.jmeter.numUsers}")
				property(name: "numExistingUsers", value: "${cfg.jmeter.numExistingUsers}")
				property(name: "numNewUsers", value: "${cfg.jmeter.numNewUsers}")

				jvmarg(value: "-Xms256m")
				jvmarg(value: "-Xmx1024m")
				jvmarg(value: "-XX:+HeapDumpOnOutOfMemoryError")
			}
		}
	}

	jmeterReport()
}

target(jmeterScenarios: "Execute all JMeter scenario-type tests") {
	ant.path(id: "ant.jmeter.classpath") {
		ant.pathelement(location: "${cfg.jmeter.home}/extras/ant-jmeter-1.0.9.jar")
	}
	ant.taskdef(name: "jmeter", classname: "org.programmerplanet.ant.taskdefs.jmeter.JMeterTask", classpathref: "ant.jmeter.classpath")

	def tests = new File("${basedir}/test/jmeter/performance").listFiles(new FilenameFilter() {
				boolean accept(File dir, String name) {
					return name.indexOf('scenario_1') > -1
				}
			}
			)

	def cfgResultDir = cfg.jmeter.scenarioOuputDirBase ?: null
	def cfgStampedResults = cfgResultDir ? "${cfgResultDir}/${new Date().time}/jtl" : null
	def localDir = "${basedir}/test/jmeter/performance/test-reports/jtl"
	def resultDir = cfgStampedResults ?: localDir

	ant.delete(dir: "${localDir}")
	ant.mkdir(dir: "${localDir}")
	if (resultDir != localDir) {
		ant.mkdir(dir: "${resultDir}")
	}

	// Run each test....
	tests.each { jmxFile ->
		if (jmxFile.isFile()) {
			jmeter(jmeterhome: "${cfg.jmeter.home}", resultlogdir: "${resultDir}", testplan: "${jmxFile}") {
				property(name: "hostName", value: "${cfg.jmeter.hostName}")
				property(name: "hostPort", value: "${cfg.jmeter.hostPort}")
				property(name: "casHost", value: "${cfg.jmeter.hostName}")
				property(name: "casPort", value: "${cfg.jmeter.hostPort}")
				property(name: "metricHostName", value: "${cfg.jmeter.hostName}")
				property(name: "metricHostPort", value: "${cfg.jmeter.hostPort}")

				property(name: "loopsPerUser", value: "${cfg.jmeter.scenarioLoopsPerUser}")
				property(name: "numUsers", value: "${cfg.jmeter.scenarioNumUsers}")
				property(name: "newUsers%", value: "${cfg.jmeter.scenarioNewUsers_pct}")
				property(name: "initialLoginUsers%", value: "${cfg.jmeter.scenarioInitialLoginUsers_pct}")
				property(name: "initialLoginTime", value: "${cfg.jmeter.scenarioInitialLoginTime}")
				property(name: "totalLoginTime", value: "${cfg.jmeter.scenarioTotalLoginTime}")
				property(name: "minDelayBetweenActions", value: "${cfg.jmeter.scenarioMinDelay}")
				property(name: "maxDelayBetweenActions", value: "${cfg.jmeter.scenarioMaxDelay}")
				property(name: "realisticLoginEnabled", value: "${cfg.jmeter.scenarioRealisticLoginEnabled}")
				property(name: "initialLoginEnabled", value: "${cfg.jmeter.scenarioInitialLoginEnabled}")

				jvmarg(value: "-Xms256m")
				jvmarg(value: "-Xmx1024m")
				jvmarg(value: "-XX:+HeapDumpOnOutOfMemoryError")
			}
		}
	}
	jmeterReport()
}

target(usage: "How to use the script") {
	echo "grails webtops command"
	echo "command options are as follows:"
	echo "\tclean\t\t-> scrubs the Tomcat directory, leaving only the WAR file"
	echo "\tjmeterSetup\t-> extracts data from Ivy to a temporary directory for use during JMeter"
	echo "\tjmeterData\t-> load the database"
	echo "\tjmeterReport\t-> run all JMeter reports for test results"
	echo "\tjmeterRun\t-> execute all JMeter tests"
	echo "\tjmeterScenarios\t-> execute all JMeter scenario-type tests"
	echo "\tproperties\t-> echo Gant and application properties to the console"
	echo "\tdeploy\t\t-> deploys WAR file to local Tomcat install (catalina_base_ozone)"
	echo "\tsecurity\t-> deploys security configuration to to local Tomcat install (implies deploy)"
}

target(default: "Default target") {
	parseArguments()

	switch (argsMap["params"][0]) {
		case 'clean':
			cleanTomcat()
			break
		case 'security':
			security()
			deploy()
			break
		case 'deploy':
			deploy()
			break
		case 'properties':
			props()
			break
		case 'jmeterSetup':
			jmeterSetup()
			break
		case 'jmeterData':
			jmeterData()
			break
		case 'jmeterRun':
			jmeterRun()
			break
		case 'jmeterScenarios':
			jmeterScenarios()
			break
		case 'jmeterReport':
			jmeterReport()
			break
		default:
			usage()
	}
}