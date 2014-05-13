//main changelog file
//includes other changelog files which are organized by version
databaseChangeLog = {

	//previous version change logs go here
	include file: 'changelog_3.7.0.groovy'
	include file: 'changelog_3.8.0.groovy'
	include file: 'changelog_3.8.1.groovy'
	include file: 'changelog_4.0.0.groovy'
	include file: 'changelog_5.0.0.groovy'
	include file: 'changelog_6.0.0.groovy'
	include file: 'changelogWebtop_6.0.0.groovy'
	include file: 'changelog_7.0.0.groovy'
	include file: 'changelog_Webtop_7.0.0_POST_enable_cluster.groovy'
	include file: 'changelog_Webtop_7.5.0.groovy'

}
