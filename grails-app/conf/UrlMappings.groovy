import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

class UrlMappings {

	static def getAction = {
		GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
		String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
		Map params = webRequest.getParameterMap()

		// parse _method to map to RESTful controller action
		String methodParam = params?."_method"?.toUpperCase()
		if (methodParam)
			method = methodParam
		switch (method) {
			case 'GET' :
				return "list"

			case 'POST':
				return "createOrUpdate"

			case 'PUT':
				return "createOrUpdate"

			case 'DELETE':
				return "delete"

			default:
				return "list"
		}
	}

	static mappings = {
		// IndexController mappings
		"/" {
			controller = "index"
			action = "index"
		}



		// ConfigController mappings
		"/config" {
			controller = "config"
			action = {
				GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
				String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
				Map params = webRequest.getParameterMap()

				return "config"
			}
		}



		// AboutController mappings
		"/about" {
			controller = "about"
			action = "index"
		}



		// PreferenceController mappings
		"/prefs/preference/$namespace?/$path?" {
			controller = "preference"
			action = {
				GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
				String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
				Map params = webRequest.getParameterMap()

				// parse _method to map to RESTful controller action
				String methodParam = params?."_method"?.toUpperCase()
				if (methodParam == 'PUT' || methodParam == 'DELETE' || methodParam == 'GET' || methodParam == 'POST') {
					method = methodParam
				}

				if (params.namespace || params.path) {
					// scan through methods to assign action
					if (method == 'GET') {
						return (params.path)? "show" : "list"
					}
					else if (method == 'POST') {
						return "create"
					}
					else if (method == 'PUT') {
						return "update"
					}
					else if (method == 'DELETE') {
						return "delete"
					}
					else {
						return (params.path)? "show" : "list"
					}
				}
				return "list"
			}
		}

		"/prefs/hasPreference/$namespace/$path" {
			controller = "preference"
			action = { "doesPreferenceExist" }
		}

		"/prefs/server/resources" {
			controller = "preference"
			action = { "serverResources" }
		}



		// PersonWidgetDefinitionController mappings
		"/prefs/widget/$guid?" {
			controller = "personWidgetDefinition"
			action = {
				GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
				String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
				Map params = webRequest.getParameterMap()

				// parse _method to map to RESTful controller action
				String methodParam = params?."_method"?.toUpperCase()
				if (methodParam == 'PUT' || methodParam == 'DELETE' || methodParam == 'GET' || methodParam == 'POST') {
					method = methodParam
				}

				//Perform both Bulk delete and Bulk update...
				//Bulk update...
				if ((params?."widgetGuidsToDelete" != null) && (params?."widgetsToUpdate" != null) && (method == 'PUT')) {
					return "bulkDeleteAndUpdate"
				}

				//Bulk delete...
				if ((params?."widgetGuidsToDelete" != null) && (method == 'DELETE')) {
					return "bulkDelete"
				}

				//Bulk update...
				if ((params?."widgetsToUpdate" != null) && (method == 'PUT')) {
					return "bulkUpdate"
				}

				// scan through methods to assign action
				if (method == 'GET') {
					return (params.guid)? "show" : "list"
				}
				else if (method == 'POST') {
					return "create"
				}
				else if (method == 'PUT') {
					return "update"
				}
				else if (method == 'DELETE') {
					return "delete"
				}
				else {
					return "show"
				}
			}
		}

		"/widget/listUserWidgets" {
			controller = "personWidgetDefinition"
			action = { "listPersonWidgetDefinitions" }
		}

		"/widget/approve" {
			controller = "personWidgetDefinition"
			action = { "approvePersonWidgetDefinitions" }
		}

		"/prefs/widget/listUserAndGroupWidgets" {
			controller = 'personWidgetDefinition'
			action = { 'listUserAndGroupWidgets' }
		}

		"/prefs/widgetList" {
			controller = 'personWidgetDefinition'
			action = { 'widgetList' }
		}

		"/prefs/personWidgetDefinition/dependents" {
			controller = "personWidgetDefinition"
			action = { "dependents" }
		}



		// DashboardController mappings
		"/dashboard/$guid?" {
			controller = "dashboard"
			action = UrlMappings.getAction
		}

		"/prefs/dashboard/$guid?" {
			controller = "dashboard"
			action = {
				GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
				String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
				Map params = webRequest.getParameterMap()

				// parse _method to map to RESTful controller action
				String methodParam = params?."_method"?.toUpperCase()
				if (methodParam == 'PUT' || methodParam == 'DELETE' || methodParam == 'GET' || methodParam == 'POST') {
					method = methodParam
				}

				// Make sure you put this check first or you'll never reach it.
				if ((params?."viewGuidsToDelete" != null) && (params?."viewsToUpdate" != null) && (method == 'PUT')) {
					return "bulkDeleteAndUpdate"
				} else if (params.guid) {
					if (method == 'POST') {
						return "create"
					}
					else if (method == 'PUT') {
						return "update"
					}
				}
			}
		}

		"/dashboard/restore/$guid?" {
			controller = "dashboard"
			action = 'restore'
		}



		// AdministrationController mappings
		"/administration/$action?/$id?" { controller = "administration" }



		// PersonController mappings
		"/user/$id?" {
			controller = "person"
			action = UrlMappings.getAction
		}

		"/prefs/person/whoami" {
			controller = "person"
			action = "whoami"
		}



		// GroupController mappings
		"/group/copyDashboard" {
			controller = "group"
			action = "copyDashboard"
		}

		"/group/$id?" {
			controller = "group"
			action = UrlMappings.getAction
		}



		// WidgetController mappings
		// Legacy re-mapping of old front-end API onto single widget management back-end.
		"/prefs/widgetDefinition" {
			controller = "widgetDefinition"
			action = 'delete'
		}

		// Another legacy re-mapping onto single widget management back-end.
		"/prefs/widgetDefinition/dependents" {
			controller = "widgetDefinition"
			action = { "dependents" }
		}

		"/widget/$widgetGuid/image/$name" {
			controller = "widgetDefinition"
			action = "cachedImage"
		}

		"/widget/$widgetGuid?" {
			controller = "widgetDefinition"
			action = UrlMappings.getAction
		}

		"/widget/export" {
			controller = "widgetDefinition"
			action = "export"
		}

		"/widgetLoadTime" {
			controller = "widgetDefinition"
			action = "saveWidgetLoadTime"
		}



		// WidgetTypeController mappings
		"/widgettype/list" {
			controller = "widgetType"
			action = "list"
		}



		// TestErrorController mappings
		"/testerror/throwerror"{
			controller = 'testError'
			action = 'throwError'
		}



		// ThemeController mappings
		"/images/$img_name**"{
			controller = 'theme'
			action = 'getImageURL'
		}

		"/themes" {
			controller = 'theme'
			action = 'getAvailableThemes'
		}



		// HelpController mappings
		"/helpFiles" {
			controller = 'help'
			action = 'getFiles'
		}



		// MergedDirectoryResourceController mappings
		"/help/$subPath**" {
			controller = 'mergedDirectoryResource'
			action = 'get'

			//the file path on the server for where external help is located
			fileRoot = {
				grailsApplication.config.owf.external.helpPath
			}
			urlRoot = "help"
		}



		// MetricController mappings
		"/metric" {
			controller = 'metric'
			action = {
				GrailsWebRequest webRequest = (GrailsWebRequest) RequestContextHolder.getRequestAttributes()
				String method = webRequest.getCurrentRequest().getMethod().toUpperCase()
				Map params = webRequest.getParameterMap()

				// parse _method to map to RESTful controller action
				String methodParam = params?."_method"?.toUpperCase()
				if (methodParam == 'PUT' || methodParam == 'DELETE' || methodParam == 'GET' || methodParam == 'POST') {
					method = methodParam
				}
				// scan through methods to assign action
				if (method == 'GET') {
					return "list"
				}
				else if (method == 'POST') {
					return "create"
				}
				else {
					return "list"
				}
			}
		}



		// MarketplaceController mappings
		"/marketplace/sync/$guid" {
			controller = 'marketplace'
			action = 'retrieveFromMarketplace'
		}



		// ErrorController mappings
		"500"(controller: 'error')
	}
}
