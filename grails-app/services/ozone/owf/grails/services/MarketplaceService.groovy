package ozone.owf.grails.services

import grails.converters.JSON

import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.params.AllClientPNames
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient

import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

class MarketplaceService {

	private class TrustAllCerts implements TrustStrategy {
		boolean isTrusted(X509Certificate[] chain, String authType) {
			return true
		}
	}

	def domainMappingService
	def grailsApplication

	// Performs some of the function of addExternalWidgetsToUser, found in the
	// WidgetDefinitionService.
	public List<WidgetDefinition> addListingsToDatabase(stMarketplaceJson) {
		// The set could be greater than one in length because widgets do have
		// dependencies.

		def updatedWidgets = stMarketplaceJson.collect { obj ->
			def widgetDefinition = WidgetDefinition.findByWidgetGuid(obj.widgetGuid, [cache:true])
			if (widgetDefinition == null) {
				log.info "Creating new widget definition for ${obj.widgetGuid}"
				widgetDefinition = new WidgetDefinition()
			}
			widgetDefinition.displayName = obj.displayName
			widgetDefinition.description = obj.description
			widgetDefinition.height = obj.height as Integer
			widgetDefinition.imageUrlLarge = obj.imageUrlLarge
			widgetDefinition.imageUrlSmall = obj.imageUrlSmall
			widgetDefinition.universalName = obj.universalName ?: obj.widgetGuid
			widgetDefinition.widgetGuid = obj.widgetGuid
			widgetDefinition.widgetUrl = obj.widgetUrl
			widgetDefinition.widgetVersion = obj.widgetVersion
			widgetDefinition.width = obj.width as Integer
			widgetDefinition.singleton = obj.singleton
			widgetDefinition.visible = (obj.widgetUrl.equals(null) || obj.widgetUrl.isAllWhitespace()) ? false : obj.visible
			widgetDefinition.background = obj.background
			widgetDefinition.descriptorUrl = obj.descriptorUrl
			widgetDefinition.widgetType = WidgetType.findByName('standard')
			widgetDefinition.save(flush: true, failOnError: true)
			return widgetDefinition
		}
		// Yes, re-reading the set.  We need to add requirements after all widgets have been added
		stMarketplaceJson.each { obj ->
			// Same comment as before: this only applies to an updated OMP baseline which
			// supports synchronization. Older baselines will have "obj" as an instance of
			// WidgetDefinition.
			if (obj instanceof Map && obj.containsKey("directRequired")) {
				updateWidgetDomainMappings(obj.widgetGuid, obj.directRequired)
			}
		}
		return updatedWidgets
	}

	def updateWidgetDomainMappings(guid, def dependentGuids = []) {
		def widget = WidgetDefinition.findByWidgetGuid(guid)
		if (!widget) {
			return
		}

		if (!dependentGuids) {
			domainMappingService.deleteAllMappings(widget)
			return
		}

		def stWidgetsToSet = new HashSet()
		stWidgetsToSet.addAll(dependentGuids)

		// What among the list of dependent GUIDs are already mapped?  Don't
		// bother trying to drop and re-add those....
		Set existingDeps = [] as Set
		Set existingGuids = [] as Set
		def mappings = domainMappingService.getAllMappings(widget, RelationshipType.requires)
		mappings.each {
			existingDeps.add(it.destId)
		}

		if (!existingDeps.isEmpty()) {
			def crtExistingGuids = WidgetDefinition.createCriteria()
			def rsltExistingGuids = crtExistingGuids.list() {
				'in'('id', existingDeps)
				projections {
					property("widgetGuid")
				}
			} as Set
			existingGuids.addAll(rsltExistingGuids)
		}

		// Part one, add any widgets we don't already have.
		stWidgetsToSet.removeAll(existingGuids)
		if (stWidgetsToSet) {
			def crtWidgetsToAdd = WidgetDefinition.createCriteria()
			def rsltWidgetsToAdd = crtWidgetsToAdd.list() {
				'in'('widgetGuid', stWidgetsToSet)
			}.each {
				domainMappingService.createMapping(widget, RelationshipType.requires, it)
			}
		}

		// Part two, remove anything we should no longer map.
		stWidgetsToSet.clear()
		stWidgetsToSet.addAll(dependentGuids)
		existingGuids.removeAll(dependentGuids)
		if (!existingGuids.isEmpty()) {
			def crtWidgetsToDrop = WidgetDefinition.createCriteria()
			def rsltWidgetsToDrop = crtWidgetsToDrop.list() {
				'in'('widgetGuid', existingGuids)
			}.each {
				domainMappingService.deleteMapping(widget, RelationshipType.requires, it)
			}
		}

	}

	// We allow for widgets to mutually refer to one another, which would normally result in
	// a stack overflow using a recursive algorithm.  Hence the "seen" set below to guard
	// against re-processing.
	public HashSet buildWidgetListFromMarketplace(guid, def mpSourceUrl = null, def seen = new HashSet()) {
		def widgetJsonMarketplace = new HashSet()

		def obj = getObjectListingFromMarketplace(guid, mpSourceUrl)
		seen.add(guid)
		obj?.directRequired?.each {
			if (!seen.contains(it)) {
				widgetJsonMarketplace.addAll(buildWidgetListFromMarketplace(it, mpSourceUrl, seen))
			}
		}
		if (obj)
			widgetJsonMarketplace.add(obj)
		widgetJsonMarketplace
	}

	private getObjectListingFromMarketplace(guid, mpSourceUrl) {
		// In some scenarios, the MP url could be null (for example, save a service item listing
		// in marketplace triggering an update in OWF). In these cases, build a list of possible
		// marketplaces to check for the GUID.  Those can be found by looking in widget definitions
		// for marketplace type listings and getting the URLs.
		def setMpUrls = new HashSet()
		if (mpSourceUrl && grailsApplication.config.mpSyncTrustProvidedUrl) {
			setMpUrls.add(mpSourceUrl)
		} else {
			def eligibles = WidgetDefinition.withCriteria {
				widgetType {
					eq 'name', WidgetType.MARKETPLACE
				}
			}
			def eligibleUrls = eligibles*.widgetUrl[0]
			if (eligibleUrls.contains(mpSourceUrl)) {
				setMpUrls.add(mpSourceUrl)
			} else {
				setMpUrls.addAll(eligibleUrls)
			}
		}

		// Some initial setup pertaining to getting certs ready for
		// use (presuming SSL mutual handshake between servers).
		def ompObj
		setMpUrls.find { mpUrl ->
			// Check each configured marketplace and stop when we get a match.
			def baseUrl = mpUrl.indexOf('?') > -1 ? mpUrl[0..(mpUrl.indexOf('?') -1)] : mpUrl
			def httpGet = new HttpGet("${baseUrl.endsWith('/') ? baseUrl : baseUrl + '/'}public/descriptor/${guid}")

			HttpClient client = new DefaultHttpClient()
			client.params.setParameter(AllClientPNames.SO_TIMEOUT, grailsApplication.config.mpSyncHostTimeout)
			try {
				if (httpGet.URI.string.contains('https:')) {
					def keyStoreFileName = System.properties['javax.net.ssl.keyStore']
					def keyStorePw = System.properties['javax.net.ssl.keyStorePassword']
					def keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
					def trustStoreFileName = System.properties['javax.net.ssl.trustStore']
					def trustStorePw = System.properties['javax.net.ssl.trustStorePassword']
					def trustStore = KeyStore.getInstance(KeyStore.getDefaultType())

					def trustStream = new FileInputStream(new File(trustStoreFileName))
					trustStore.load(trustStream, trustStorePw.toCharArray())
					trustStream.close()

					def keyStream = new FileInputStream(new File(keyStoreFileName))
					keyStore.load(keyStream, keyStorePw.toCharArray())
					keyStream.close()

					def factory
					if (grailsApplication.config.mpSyncTrustAll) {
						factory = new SSLSocketFactory(SSLSocketFactory.TLS, keyStore, keyStorePw, trustStore, new SecureRandom(), new TrustAllCerts(), SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
						log.warn "getObjectListingFromMarketplace: trusting all SSL hosts and certificates"
					} else {
						factory = new SSLSocketFactory(keyStore, keyStorePw, trustStore)
						log.info "getObjectListingFromMarketplace: requesting from Marketplace with strict cert verification"
					}

					URL u = new URL(httpGet.URI.string)
					def port = (u.port > -1) ? u.port : 443
					def sch = new Scheme("https", port as int, factory)
					client.connectionManager.schemeRegistry.register(sch)
				}

				def httpResponse = client.execute(httpGet)
				if (httpResponse.entity?.contentType?.value.contains("json")) {
					log.info "Received JSON response from MP (${httpGet.URI.string}); success"
					def handler = new BasicResponseHandler()
					def strJson = handler.handleResponse(httpResponse)
					ompObj = JSON.parse(strJson)
				}
				else {
					log.warn "Received non-parseable response from MP, content type -> ${httpResponse.entity.contentType}"
				}
			} catch (all) {
				// Log and eat
				log.warn "The following stack trace may not actually denote an error and comes from an attempted sync with a marketplace instance (${httpGet.URI.string})"
				log.warn all
			}
			finally {
				client.getConnectionManager().shutdown()
			}

			// Break out on first match
			if (ompObj) {
				return true
			}
		}
		if (ompObj) {
			return ompObj?.data[0]
		}
		else {
			return null
		}
	}

}
