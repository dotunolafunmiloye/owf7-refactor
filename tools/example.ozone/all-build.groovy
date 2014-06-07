import org.apache.ivy.plugins.resolver.*

ozone.dbUsername=someRandomUsername
ozone.dbPassword=someRandomPassword
ozone.dbServerPort=myserver.example.com:9443
ozone.isOffline=false

// Useful when userHome is redirected to a networked location
System.setProperty "ivy.default.ivy.user.dir", "SomeNonNetworkedLocation"

private def createOzoneIvyResolver() {
	def url = 'https://www.owfgoss.org/svn/repos/ozone/ivy-repo/no-namespace'
	def urlResolver = new URLResolver()
	urlResolver.setName('ivysvnresolver')
	urlResolver.addIvyPattern("${url}/[organisation]/[module]/ivys/ivy-[revision].xml")
	urlResolver.addIvyPattern("${url}/[organisation]/[module]/ivy-[revision].xml")
	urlResolver.addArtifactPattern("${url}/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]")
	def ivySettings = new IvySettings()
	ivySettings.defaultInit()
	urlResolver.settings = ivySettings
	CredentialsStore.INSTANCE.addCredentials("Password Required by Subversion", 'www.owfgoss.org', "owf-build", '0wf-bu1!d')

	return urlResolver
}

private org.apache.ivy.plugins.resolver.DependencyResolver createLocalResolver() {
	def local_dir=System.getProperty("ivy.default.ivy.user.dir") ?: "${userHome}/.ivy2/local"
	def localResolver = new FileSystemResolver()
	localResolver.local = true
	localResolver.name = "localResolver"
	localResolver.m2compatible = false
	localResolver.addIvyPattern("${local_dir}/[organisation]/[module]/ivy-[revision].xml")
	localResolver.addArtifactPattern("${local_dir}/[organisation]/[module]/[type]s/[artifact]-[revision].[ext]")
	def ivySettings = new IvySettings()
	ivySettings.defaultInit()
	localResolver.settings = ivySettings
	return localResolver
}

ozone.resolvers = [
	createLocalResolver(),
	createOzoneIvyResolver()
]
