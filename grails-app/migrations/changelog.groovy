import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

//main changelog file
//includes other changelog files which are organized by version
databaseChangeLog = {

	//previous version change logs go here
	include file: 'changelog_webtops.groovy'

}
