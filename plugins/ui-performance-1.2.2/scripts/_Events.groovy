import grails.util.GrailsUtil

/**
 * In 1.0.x this is called after the staging dir is prepared but before the war is packaged.
 */
eventWarStart = { name ->
	if (name instanceof String || name instanceof GString) {
		versionResources name, stagingDir
	}
}

/**
 * In 1.1 this is called after the staging dir is prepared but before the war is packaged.
 */
eventCreateWarStart = { name, stagingDir ->
    def ant = new AntBuilder()
    def baseWebDir = "${basedir}/web-app"

    //copy external themes into the war folder
    //so that uiperformance will run on them
    ant.copy(todir:"${stagingDir}/themes", includeemptydirs: false) {
        fileset(dir: "${baseWebDir}/themes") {
			include(name: "**/css/**/*")
			include(name: "**/images/**/*")
			include(name: "**/theme.json")
			exclude(name: "template/**")
			exclude(name: "**/.gitignore")
		}
    }
	ant.delete(includeemptydirs: true, failonerror: false) {
		fileset(dir: "${stagingDir}/themes/common/lib")
		fileset(dir: "${stagingDir}/themes/common/stylesheets")
	}

    ant.copy(todir:"${stagingDir}/js-plugins") {
        fileset(dir: "${basedir}/js-plugins")
    }

	versionResources name, stagingDir

    //keep old owf bundle name for compatibility
    ant.copy(todir: "${stagingDir}/js-min") {
      fileset(dir: "${stagingDir}/js") {
        include(name: "owf-widget__v*.js")
        exclude(name: "owf-widget__v*gz.js")
      }
      regexpmapper(from: '^(.*)$', to: "owf-widget-min.js")
    }
    ant.copy(file : "${stagingDir}/js/owf-widget.js", tofile: "${stagingDir}/js-min/owf-widget-debug.js")

//    ant.copy(todir: "${stagingDir}/js-min") {
//      fileset(dir: "${stagingDir}/js") {
//        include(name: "owf-server__v*.js")
//        exclude(name: "owf-server__v*gz.js")
//      }
//      regexpmapper(from: '^(.*)$', to: "owf-server-min.js")
//    }
//    ant.copy(file : "${stagingDir}/js/owf-server.js", tofile: "${stagingDir}/js-min/owf-server-debug.js")

    //copy back into our webapp for grails run mode
    ant.copy(todir: "${baseWebDir}/js-min") {
      fileset(dir: "${stagingDir}/js") {
        include(name: "owf-widget__v*.js")
        exclude(name: "owf-widget__v*gz.js")
      }
      regexpmapper(from: '^(.*)$', to: "owf-widget-min.js")
    }
    ant.copy(file : "${stagingDir}/js/owf-widget.js", tofile: "${baseWebDir}/js-min/owf-widget-debug.js")

//    ant.copy(todir: "${baseWebDir}/js-min") {
//      fileset(dir: "${stagingDir}/js") {
//        include(name: "owf-server__v*.js")
//        exclude(name: "owf-server__v*gz.js")
//      }
//      regexpmapper(from: '^(.*)$', to: "owf-server-min.js")
//    }
//    ant.copy(file : "${stagingDir}/js/owf-server.js", tofile: "${baseWebDir}/js-min/owf-server-debug.js")

    //build classpath for our jar
    //ant.path(id:"classpath") {
    //  fileset(dir:"${stagingDir}/WEB-INF/lib") {
    //    include(name:"*.*")
    //  }
    //  fileset(dir:"${stagingDir}/WEB-INF/tools") {
    //    include(name:"*.*")
    //  }
    //}

    //convert to string
    //ant.pathconvert(property:"classpath.string",pathsep:" ") {
    //  path(refid:'classpath')
    //  map(from:"${stagingDir}/WEB-INF/tools", to:"./")
    //  map(from:"${stagingDir}/WEB-INF/lib/", to:"../lib/")
    //}

    //create createBundles.jar
    //ant.mkdir(dir:"${stagingDir}/WEB-INF/tools")
    //ant.jar(destfile:"${stagingDir}/WEB-INF/tools/createWebBundles.jar") {
    //  manifest {
    //    attribute(name:"Main-Class",value:"com.studentsonly.grails.plugins.uiperformance.util.CreateWebBundles")
    //      attribute(name:"Class-Path",value:'${classpath.string} ./ ../classes/')
    //  }
    //  zipfileset(src:"${stagingDir}/WEB-INF/lib/owf-all.jar") {
    //    include(name:"com/studentsonly/grails/plugins/uiperformance/util/CreateWebBundles*.*")
    //  }
    //}
	
	ant.delete(includeemptydirs: true, failonerror: false) {
		fileset(dir: "${stagingDir}") {
			include(name: "WEB-INF/templates/**")
			include(name: "WEB-INF/tools")
		}
	}
}

void versionResources(name, stagingDir) {
	def classLoader = Thread.currentThread().contextClassLoader
	classLoader.addURL(new File(classesDirPath).toURL())

	def config = new ConfigSlurper(GrailsUtil.environment).parse(classLoader.loadClass('Config')).uiperformance
	def enabled = config.enabled
	enabled = enabled instanceof Boolean ? enabled : true

	if (!enabled) {
		println "\nUiPerformance not enabled, not processing resources\n"
		return
	}

	println "\nUiPerformance: versioning resources ...\n"

	String className = 'com.studentsonly.grails.plugins.uiperformance.ResourceVersionHelper'
	def helper = Class.forName(className, true, classLoader).newInstance()
	helper.version stagingDir, basedir
}
