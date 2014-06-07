package ozone.owf.grails.services

/*
 * This service looks for the specified resource in two places.  
 * First, it looks in the external directory specified in fileRoot.
 * If not found, it then looks where the file would be if it were static
 * content from the war, as specified by urlRoot
 * If neither location are successful, it returns null
 */
class MergedDirectoryResourceService {

    def grailsApplication

    def getResource(fileRoot, urlRoot, subPath) {
        def appContext = grailsApplication.mainContext

        if (subPath.contains('..')) {
            log.info("Refusing to retrieve resource from subPath containing '..': ${subPath}")
            return null //subpath comes from end-user, should not trust it
        }

//        log.debug("Searching for ${subPath} in ${fileRoot} and ${urlRoot}")        

        def resource = appContext.getResource("${fileRoot}/${subPath}")
        if (!resource.exists()) {
//            log.debug("Resource not found in ${fileRoot}")
            resource = appContext.getResource("${urlRoot}/${subPath}")

            if (!resource.exists()) {
//                log.debug("Resource not found in ${urlRoot}")
                resource = null
            }
        }

//        log.debug("Resource = ${resource}")
        return resource
    }

    /*
     * Similar to getResource, but can have glob patterns in its parameters.
     * Returns all matches
     */ 
    def getResources(fileRoot, urlRoot, subPath) {
        def appContext = grailsApplication.mainContext

        if (subPath.contains('..')) {
            log.info("Refusing to retrieve resource from subPath containing '..': ${subPath}")
            return [] //subpath comes from end-user, should not trust it
        }

		def coll = new HashSet() // Enforcing uniqueness on the resources seems reasonable
		def filColl
		def urlColl
		try {
			filColl = appContext.getResources("${fileRoot}/${subPath}")
		} catch (IOException ioE) {/* Eat this as it simply means the file path wasn't found, which could be okay. */}
		try {
			urlColl = appContext.getResources("${urlRoot}/${subPath}")
		} catch (IOException ioE) {/* Eat this as it simply means the path wasn't found in the WAR, which could be okay. */}

		if (filColl) {
			coll.addAll(filColl as Collection)
		}
		if (urlColl) {
			coll.addAll(urlColl as Collection)
		}

		coll
    }
}
