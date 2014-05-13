package ozone.owf.grails.services

import java.security.cert.X509Certificate

import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.X509Certificate

import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element

import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.scheme.Scheme
import org.apache.http.conn.ssl.SSLSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils

class CachedImage {
	byte[] content
	String contentType
	CachedImage(HttpEntity e) {
		content = EntityUtils.toByteArray(e)
		contentType = e.contentType
	}
}

/**
 * Service for caching icons.
 */
class ImageCacheService {

	Cache cache
	SSLSocketFactory socketFactory

	ImageCacheService() {
		cache = CacheManager.getInstance().getCache("imageCache")
		if (cache == null) {
			cache = new Cache("imageCache", 100, false, false, 3600, 3600)
			CacheManager.getInstance().addCache(cache)
		}
	}

	CachedImage getImage(String url) {
		Element e = cache.get(url)
		if (e != null) {
			return e.getObjectValue()
		} else {
			return cacheImage(url)
		}
	}

	CachedImage cacheImage(String url) {
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

		socketFactory = new SSLSocketFactory(SSLSocketFactory.TLS,
				keyStore, keyStorePw, trustStore,
				new SecureRandom(),
				new TrustStrategy() {
					boolean isTrusted(X509Certificate[] chain, String authType) {
						return true
					}
				},
				SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
				)

		DefaultHttpClient client = new DefaultHttpClient()
		CachedImage image = null

		String urlUsed
		if (url.substring(0, 5) != 'http') {
			def host = System.getProperty('ozone.host')
			def port = Integer.parseInt(System.getProperty('ozone.port') ?: '443')
			urlUsed = "https://${host}${port == 443 ? '' : ':' + port}/owf/${url}"
		}
		else {
			urlUsed = url
		}

		if (urlUsed.contains('https:')) {
			URL u = new URL(urlUsed)
			def port = (u.port > -1) ? u.port : 443
			client.connectionManager.schemeRegistry.register(new Scheme("https", port as int, socketFactory))
		}

		try {
			def httpGet = new HttpGet(urlUsed)
			def httpResponse = client.execute(httpGet)

			if (httpResponse.entity?.contentType?.value.contains("image")) {
				image = new CachedImage(httpResponse.entity)
				cache.put(new Element(urlUsed, image))
			} else {
				log.warn "Refusing to cache a non-image from ${urlUsed} with content-type ${httpResponse.entity.contentType}"
			}
		} catch (all) {
			// Log and eat
			log.warn "Error while trying to cache image ${urlUsed}", all
		}
		return image
	}
}
