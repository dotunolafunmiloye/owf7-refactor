package integration.ozone.owf.grails.conf

import grails.test.GrailsUrlMappingsTestCase

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

public class UrlMappingsTests extends GrailsUrlMappingsTestCase {

	// IndexController mappings
	// Testing here to make sure regardless of the method used the result is always the same.
	void testIndexControllerBase() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/', controller: 'index', action: 'index')
	}

	void testIndexControllerBasePost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/', controller: 'index', action: 'index')
	}

	void testIndexControllerBasePut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/', controller: 'index', action: 'index')
	}

	void testIndexControllerBaseDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/', controller: 'index', action: 'index')
	}

	// ConfigController mappings
	// Testing here to make sure regardless of the method used the result is always the same.
	void testConfigControllerBase() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/config', controller: 'config', action: 'config')
	}

	void testConfigControllerBasePost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/config', controller: 'config', action: 'config')
	}

	void testConfigControllerBasePut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/config', controller: 'config', action: 'config')
	}

	void testConfigControllerBaseDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/config', controller: 'config', action: 'config')
	}

	// AboutController mappings
	// Testing here to make sure regardless of the method used the result is always the same.
	void testAboutControllerBase() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/about', controller: 'about', action: 'index')
	}

	void testAboutControllerBasePost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/about', controller: 'about', action: 'index')
	}

	void testAboutControllerBasePut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/about', controller: 'about', action: 'index')
	}

	void testAboutControllerBaseDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/about', controller: 'about', action: 'index')
	}


	// PreferenceController mappings
	// Testing here to tease out whether the method and params values always point to the right location.
	void testPreferenceControllerNamespacePathGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1', controller: 'preference', action: 'show') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExtraSlashGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1/', controller: 'preference', action: 'show') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespaceGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference/namespace1', controller: 'preference', action: 'list') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespaceExtraSlashGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference/namespace1/', controller: 'preference', action: 'list') { namespace = 'namespace1' }
	}

	void testPreferenceControllerBaseGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBaseExtraSlashGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/preference/', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBasePost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBaseExtraSlashPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference/', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBasePut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBaseExtraSlashPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference/', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBaseDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerBaseExtraSlashDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference/', controller: 'preference', action: 'list')
	}

	void testPreferenceControllerNamespacePost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference/namespace1', controller: 'preference', action: 'create') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespaceExtraSlashPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference/namespace1/', controller: 'preference', action: 'create') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespacePathPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1', controller: 'preference', action: 'create') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExtraSlashPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1/', controller: 'preference', action: 'create') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference/namespace1', controller: 'preference', action: 'update') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespaceExtraSlashPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference/namespace1/', controller: 'preference', action: 'update') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespacePathPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1', controller: 'preference', action: 'update') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExtraSlashPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1/', controller: 'preference', action: 'update') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespaceDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference/namespace1', controller: 'preference', action: 'delete') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespaceExtraSlashDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference/namespace1/', controller: 'preference', action: 'delete') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespacePathDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1', controller: 'preference', action: 'delete') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExtraSlashDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1/', controller: 'preference', action: 'delete') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespaceOdd() {
		sendHttpRequestMethod('head')
		assertForwardUrlMapping('/prefs/preference/namespace1', controller: 'preference', action: 'list') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespaceExtraSlashOdd() {
		sendHttpRequestMethod('head')
		assertForwardUrlMapping('/prefs/preference/namespace1/', controller: 'preference', action: 'list') { namespace = 'namespace1' }
	}

	void testPreferenceControllerNamespacePathOdd() {
		sendHttpRequestMethod('head')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1', controller: 'preference', action: 'show') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExtraSlashOdd() {
		sendHttpRequestMethod('head')
		assertForwardUrlMapping('/prefs/preference/namespace1/path1/', controller: 'preference', action: 'show') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExistGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/hasPreference/namespace1/path1', controller: 'preference', action: 'doesPreferenceExist') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExistPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/hasPreference/namespace1/path1', controller: 'preference', action: 'doesPreferenceExist') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExistPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/hasPreference/namespace1/path1', controller: 'preference', action: 'doesPreferenceExist') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathExistDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/hasPreference/namespace1/path1', controller: 'preference', action: 'doesPreferenceExist') {
			namespace = 'namespace1'
			path = 'path1'
		}
	}

	void testPreferenceControllerNamespacePathResourcesGet() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/server/resources', controller: 'preference', action: 'serverResources')
	}

	void testPreferenceControllerNamespacePathResourcesPost() {
		sendHttpRequestMethod('post')
		assertForwardUrlMapping('/prefs/server/resources', controller: 'preference', action: 'serverResources')
	}

	void testPreferenceControllerNamespacePathResourcesPut() {
		sendHttpRequestMethod('put')
		assertForwardUrlMapping('/prefs/server/resources', controller: 'preference', action: 'serverResources')
	}

	void testPreferenceControllerNamespacePathResourcesDelete() {
		sendHttpRequestMethod('delete')
		assertForwardUrlMapping('/prefs/server/resources', controller: 'preference', action: 'serverResources')
	}






	/**
	 * Integration Tests For Url Mappings To Person Widget Definitions
	 */
	void testAdminPersonWidgetDefinitionsShowActionForGuidParams() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'show')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionsListAction() {
		sendHttpRequestMethod('get')
		assertForwardUrlMapping('/prefs/widget', controller:'personWidgetDefinition', action:'list')
	}

	void testAdminPersonWidgetDefinitionsCreateActionForGuidParams() {
		sendHttpRequestMethod("post")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'create')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionsUpdateActionForGuidParams() {
		sendHttpRequestMethod("put")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'update')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionsDeleteActionForGuidParams() {
		sendHttpRequestMethod("delete")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'delete')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionsBulkDeleteActionForGuidParams() {
		sendHttpRequestMethod("delete", "widgetGuidsToDelete")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'bulkDelete')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionsBulkUpdateActionForGuidParams() {
		sendHttpRequestMethod("put", "widgetsToUpdate")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'bulkUpdate')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminPersonWidgetDefinitionBulkDeleteAndUpdateActionForGuidParams() {
		sendHttpRequestMethod("put", "widgetGuidsToDelete", "widgetsToUpdate")
		assertForwardUrlMapping('/prefs/widget/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'personWidgetDefinition', action:'bulkDeleteAndUpdate')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testWidgetListMapping()  {
		assertForwardUrlMapping('/prefs/widgetList', controller:'personWidgetDefinition', action:'widgetList')
	}

	/**
	 * Integration Tests For Url Mappings To Dashboard
	 */
	void testAdminDashboardCreateActionForGuidParams() {
		sendHttpRequestMethod("post")
		assertForwardUrlMapping('/prefs/dashboard/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'dashboard', action:'create')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminDashboardUpdateActionForGuidParams() {
		sendHttpRequestMethod("put")
		assertForwardUrlMapping('/prefs/dashboard/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'dashboard', action:'update')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	void testAdminDashboardBulkDeleteActionPathParams() {
		sendHttpRequestMethod("put", "viewGuidsToDelete", "viewsToUpdate")
		assertForwardUrlMapping('/prefs/dashboard/0c5435cf-4021-4f2a-ba69-dde451d12551', controller:'dashboard', action:'bulkDeleteAndUpdate')
		{ guid = '0c5435cf-4021-4f2a-ba69-dde451d12551' }
	}

	/**
	 * Test Admin url mapping for person controller
	 */
	void testAdminUrlMappingForPersonController() {
		assertForwardUrlMapping('/prefs/person/whoami', controller:'person', action:'whoami')
	}

	/**
	 * Test testerror url mapping for throwError action
	 */
	void testTestErrorUrlMappingWithThrowErrorAction() {
		assertForwardUrlMapping('/testerror/throwerror', controller:'testError', action:'throwError')
	}

	/**
	 * Url Mapping Integration Tests Helper Methods
	 */
	private void sendHttpRequestMethod(httpMethod) {
		GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
		String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
		Map params = webRequest.getParameterMap()
		params["_method"] = httpMethod
	}

	private void sendHttpRequestMethod(httpMethod, bulkAction) {
		GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
		String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
		Map params = webRequest.getParameterMap()
		params["_method"] = httpMethod
		params[bulkAction] = bulkAction
	}

	private void sendHttpRequestMethod(httpMethod, params1, params2) {
		GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
		String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
		Map params = webRequest.getParameterMap()
		params["_method"] = httpMethod
		params[params1] = params1
		params[params2] = params2
	}

}
