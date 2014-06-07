import org.apache.ivy.plugins.resolver.*
import org.apache.ivy.util.url.CredentialsStore
import org.apache.ivy.core.settings.IvySettings

// Not used for running applications.
// Only used for grails commands - eg. db-update-sql, etc.
ozone.dbUsername="@DB_USERNAME@" 
ozone.dbPassword="@DB_PASSWORD@"
ozone.dbServerPort="@DATABASE:PORT@"
ozone.isOffline=true

// Useful when userHome is redirected to a networked location
//ozone.ivy.repo.url="https://@LOCAL_NETWORK_IVY_REPO:PORT@/ivy-repo/no-namespace"

System.setProperty "ivy.default.ivy.user.dir", "C:\\LocalApps\\.ivy2"

private def createWebtopsResolver()
{
	def r=new IBiblioResolver()
	r.name='artifactoryResolver'
	r.root='http://@ARTIFACTORY:PORT@/artifactory/webtops'
	r.m2compatible=true;
	return r;
}

private def createOzoneIvyResolver()
{
	def url = 'http://@ARTIFACTORY:PORT@/artifactory/ozone-ivy-repo'
	def urlResolver = new URLResolver()
	urlResolver.setName('ivysvnresolver')
	urlResolver.addIvyPattern("${url}/[organisation]/[module]/ivys/ivy-[revision].xml")
	urlResolver.addIvyPattern("${url}/[organisation]/[module]/ivy-[revision].xml")
	urlResolver.addArtifactPattern("${url}/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]")
	return urlResolver
}

ozone.resolvers = [
	createWebtopsResolver(),
	createOzoneIvyResolver()
]
