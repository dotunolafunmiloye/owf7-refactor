import java.text.SimpleDateFormat

void compileStyleSheets(dir) {
	def ant = new AntBuilder()   // create an antbuilder
	if (System.properties['os.name'].toLowerCase().contains('windows')) {
		ant.exec(
				failonerror: "true",
				dir: "${dir}/themes",
				executable: 'cmd') {
					arg(value: "/c")
					arg(value: "compile_all_themes.bat")
				}
	}
	else {
		ant.exec(
				failonerror: "true",
				dir: "${dir}/themes",
				executable: 'sh') {
					arg(value: "compile_all_themes.sh")
				}
	}

	//delete cache files
	ant.delete(includeemptydirs:true) {
		fileset(dir:"${dir}") {
			include(name:"**/.sass-cache/**")
		}
	}

	println "finished compiling sass stylesheets"
}

eventRunAppHttpsStart = {
	def baseWebDir = "${basedir}/web-app"

	if (!System.properties.skipSassCompile) {
		println "compiling sass stylesheets - ruby, compass, and sass must be installed"
		println "compiling stylesheets in web-app"
		compileStyleSheets(baseWebDir)
	}
}

eventRunAppStart = {
	def baseWebDir = "${basedir}/web-app"

	if (!System.properties.skipSassCompile) {
		println "compiling sass stylesheets - ruby, compass, and sass must be installed"
		println "compiling stylesheets in web-app"
		compileStyleSheets(baseWebDir)
	}
}

eventCreateWarStart = { name, stagingDir ->
	ant.copy(todir: "${stagingDir}/help", includeemptydirs: false) {
		fileset(dir: "${basedir}/docs-internal") {
			exclude(name: "Group Management*/**")
			exclude(name: "libraries/**")
			exclude(name: "*Management for Administrators/**")
			exclude(name: "**/*.pdf")
			exclude(name: "Welcome To The New Build.*")
			exclude(name: "Instructions*.*")
			exclude(name: "OWF A*.*")
			exclude(name: "OWF C*.*")
			exclude(name: "OWF D*.*")
		}
	}

	ant.mkdir(dir: "${stagingDir}/help/Ozone Help Videos")
	ant.mkdir(dir: "${stagingDir}/help/Ozone Help Docs")
	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/marketplace-overview/") {
		fileset(dir: "${stagingDir}/help/Help Videos/Marketplace Overview/")
	}
	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/marketplace-overview/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/switcher-overview/") {
		fileset(dir: "${stagingDir}/help/Help Videos/Switcher Overview Video/")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/switcher-overview/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/owf-accessibility/") {
		fileset(dir: "${stagingDir}/help/Help Videos/OWF Accessibility/")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/owf-accessibility/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/user-dropdown-menu/") {
		fileset(dir: "${stagingDir}/help/Help Videos/User Drop-down Menu Video/")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/user-dropdown-menu/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/dashboard-designer/") {
		fileset(dir: "${stagingDir}/help/Help Videos/Dashboard Designer/")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/dashboard-designer/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/general-overview/") {
		fileset(dir: "${stagingDir}/help/Help Videos/OWF Overview")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/general-overview/") {
			include(name: "**/*.html")
			include(name: "**/expressInstall.swf")
		}
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Videos/widget overview/") {
		fileset(dir: "${stagingDir}/help/Help Videos/widget-overview")
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Ozone Help Videos/widget overview/") {
			include(name: "**/expressInstall.swf")
			include(name: "**/*.html")
		}
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/help/Help Videos/Group Manager/")
		fileset(dir: "${stagingDir}/help/Help Videos/Widget Management for Administrators/")
		fileset(dir: "${stagingDir}/help/Help Videos/User Manager Overview for Administrators/")
		fileset(dir: "${stagingDir}/help/Ozone Help Videos")
	}

	ant.move(todir: "${stagingDir}/help/Ozone Help Docs") {
		fileset(dir: "${stagingDir}/help/") {
			include(name: "**/*.doc")
		}
	}

	ant.echo(message: "creating owf-all.jar")

	//jar up all files in WEB-INF/classes and put into WEB-INF/lib/owf-all.jar
	ant.jar(destfile: "${stagingDir}/WEB-INF/lib/owf-all.jar", update: false) {
		fileset(dir: "${stagingDir}/WEB-INF/classes") {
			exclude(name: "**/gsp_*.*")
			exclude(name: "**/*.properties")
			exclude(name: "**/*.xml")
			exclude(name: "changelog*")
			exclude(name: "**/changelog*/**")
		}
	}

	ant.delete(includeemptydirs: true) {
		fileset(dir: "${stagingDir}/WEB-INF/classes") {
			exclude(name: "**/gsp_*.*")
			exclude(name: "**/*.properties")
			exclude(name: "**/*.xml")
		}
	}

	ant.echo(message: "finished creating owf-all.jar")

	ant.delete(includeemptydirs: true, failonerror: false) {
		fileset(dir: "${stagingDir}/plugins")
	}

	ant.echo(message: "copying database changelogs to war")
	ant.copy(todir: "${stagingDir}/WEB-INF/classes/migrations") {
		fileset(dir: "${basedir}/grails-app/migrations") { include(name: "changelog*.groovy") }
	}
	ant.echo(message: "finished copying database changelogs to war")

	ant.copy(todir: "${stagingDir}", overwrite: true) {
		fileset(dir: "${basedir}/src/resources/branding") {
			include(name: "**/*")
		}
	}

	ant.echo(message: 'Copying ehcache config file')
	ant.copy(file: "${basedir}/src/resources/ehcache.xml", todir: "${stagingDir}/WEB-INF/classes")

	def buildNumber = System.getenv('BUILD_NUMBER')
	if (buildNumber) {
		ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
			entry(key: "build.number", value: buildNumber)
		}
	}

	def sdfParse = new SimpleDateFormat('yyyy-MM-dd_HH-mm-ss')
	def buildDate = System.getenv('BUILD_ID') ?: sdfParse.format(new Date())
	if (buildDate) {
		def sdfEmit = new SimpleDateFormat("MMMM d, yyyy 'at' h:mm a")
		Date buildDateCleaned = null
		try {
			buildDateCleaned = sdfParse.parse(buildDate)

			if (buildDateCleaned) {
				ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
					entry(key: "build.date", value: sdfEmit.format(buildDateCleaned))
				}
			}
		} catch (all) {}
	}

	def commitId = System.getenv('GIT_COMMIT')
	if (commitId) {
		ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
			entry(key: "commit.id", value: commitId)
		}
	}

	def ctx = "/" + System.getProperty('APP_CONTEXT') ?: "owf"
	ant.propertyfile(file: "${stagingDir}/WEB-INF/classes/application.properties") {
		entry(key: "app.context", value: ctx)
	}
}

eventCleanEnd = { it ->
	ant.delete(includeemptydirs: true, failonerror: false) {
		fileset(dir: "${basedir}/logs")
		fileset(dir: "${basedir}/target")
		fileset(dir: "${basedir}/web-app/js-min")
		fileset(dir: "${basedir}/web-app/themes/*.theme/css")
	}
}
