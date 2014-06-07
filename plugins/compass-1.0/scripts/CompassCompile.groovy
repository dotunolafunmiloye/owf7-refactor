
target(default:"Compile the theme") {
    depends(clean, compile)
}
target(clean:"Clean out things") {
    ant.delete(dir:"output")
}

target(compile:"Compile the directory") {
	compassPluginCompile()
}

private compileThemes = { dir ->
	dir.eachFile { file ->
		if(file.isDirectory() && file.name.endsWith(".theme")) {
			ant.echo(message: "Building ${file}")
			ant.java(classname:"org.jruby.Main", 
					fork:true, 
					failonerror:true, 
					classpathref: 'jruby.classpath',
					dir: new File(file.getPath() + '/sass')
			) {
					env(key: "GEM_HOME", value: "${compassPluginDir}/libs/gems")
					arg(value: "--1.8")
					arg(value:"${compassPluginDir}/libs/gems/bin/compass")
					arg(value:"compile")
			}
		}
	}
}

public void compassPluginCompile() {
	ant.path(id: 'jruby.classpath') { 
		pathelement(location: "${compassPluginDir}/libs/jruby-complete-1.7.0.jar") 
	}
	ant.echo(message: "compiling stylesheets in internal themes dir")
	compileThemes(new File("web-app/themes"))
	ant.echo(message: "compiling stylesheets in external themes dir")
	compileThemes(new File("themes"))

}