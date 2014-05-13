import grails.util.BuildSettingsHolder

// BuildSettingsHolder.settings acts as a proxy for "are we running in a build?" rather than as a war
if (BuildSettingsHolder.settings) {
	grails.config.locations = [
		"file:src/resources/OwfConfig.groovy",
		"file:${userHome}/.ozone/all-build.groovy",
		"file:${userHome}/.ozone/owf-server-build.groovy"
	]
} else {
	grails.config.locations = [
		"classpath:OwfConfig.groovy"
	]
}

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination
grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [
			all:           '*/*',
			atom:          'application/atom+xml',
			css:           'text/css',
			csv:           'text/csv',
			form:          'application/x-www-form-urlencoded',
			html:          ['text/html','application/xhtml+xml'],
			js:            'text/javascript',
			json:          ['application/json', 'text/json'],
			multipartForm: 'multipart/form-data',
			rss:           'application/rss+xml',
			text:          'text/plain',
			xml:           ['text/xml', 'application/xml']
		]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
// enable Sitemesh preprocessing of GSP pages
grails.views.gsp.sitemesh.preprocess = true
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

environments {
	development {
		grails.logging.jul.usebridge = true
	}
	production {
		grails.logging.jul.usebridge = false
	}
}

stamp {
	audit {
		createdBy = "createdBy"
		createdDate = "createdDate"
		editedBy = "editedBy"
		editedDate = "editedDate"
	}
}

server.version = appVersion
server.baseVersion = appVersion?.toString()?.split("-")[0]

uiperformance.enabled = true
uiperformance.processImages = false
uiperformance.inclusionsForCaching = [
	//        "**/examples/*.html",
	"**/*.ico",
	"**/*.jpg",
	"**/*.png",
	"**/*.gif"
]
uiperformance.exclusions = [
	"**/help/**",
	"**/sampleWidgets/**",
	"**/jsunit/**",
	"**/js-test/**",
	"**/js-doh/**",
	"**/js-lib/**",
	"**/js-min/**"

]


def owfServerBaseJavascript= [
	'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed',
	'../js-lib/ext-4.0.7/ext-all-debug',
	'../js-lib/jquery/jquery-1.8.0',
	'../js-lib/pnotify-1.2.0/jquery.pnotify',
	'../js-lib/stubconsole',
	'../js-plugins/Banner',
	'../js-plugins/Dashboard',
	'../js-plugins/DashboardContainer',
	'../js-plugins/WidgetBase',
	'../js-plugins/WidgetPanel',
	'../js-plugins/WidgetWindow',
	'../js-plugins/pane/Pane',
	'../js-plugins/pane/AccordionPane',
	'../js-plugins/pane/DesktopPane',
	'../js-plugins/pane/FitPane',
	'../js-plugins/pane/PortalPane',
	'../js-plugins/pane/TabbedPane',
	'../js-lib/patches/QuickTipOverrides',
	'../js-lib/patches/DragDropManagerOverrides',
	'../js-lib/patches/EventObjectImplOverrides',
	'../js-lib/patches/DragZoneOverrides',
	'../js-lib/patches/DragTrackerOverrides',
	'../js-lib/patches/ElementOverrides',
	'../js-lib/patches/ZIndexManagerOverrides',
	'../js-lib/patches/GridOverrides',
	'../js-lib/patches/ButtonOverrides',
	'../js-lib/patches/ComboBoxOverrides',
	'../js-lib/patches/BorderLayoutAnimation',
	'../js-lib/patches/menuAlign',
	'../js-lib/patches/CreateInterceptor',
	'../js-lib/patches/PluginMerge',
	'../js-lib/patches/TextFieldOverrides',
	'../js-lib/patches/TreeViewOverrides',
	'../js-lib/patches/LoadMaskOverrides',
	'../js-lib/patches/LegendItemOverrides',
	'../js-lib/patches/GridScroller',
	'events/Events',
	'components/util/History',
	'components/util/InstanceVariablizer',
	'components/keys/HotKeys',
	'components/keys/KeyMap',
	'components/keys/MoveKeyMap',
	'components/keys/HotKeyComponent',
	'components/keys/KeyMoveable',
	'components/focusable/Focusable',
	'components/focusable/CircularFocus',
	'components/focusable/FocusableGridPanel',
	'components/focusable/EscCloseHelper',
	'components/draggable/DraggableWidgetView',
	'components/tree/SimpleTreeColumn',
	'components/resizer/NoCollapseSplitter',
	'ux/layout/component/form/MultiSelect',
	'ux/layout/component/form/ItemSelector',
	'ux/form/MultiSelect',
	'ux/form/ItemSelector',
	'ux/MessageBoxPlus',
	'ux/OWFVtypes',
	'../js-lib/log4javascript/log4javascript',
	'util/version',
	'util/util',
	'util/widget_utils',
	'util/log',
	'lang/ozone-lang',
	'lang/DateJs/globalization/en-US',
	'util/error',
	'layout/manage_widgets_container',
	'data/models/LayoutType',
	'ux/tab_panel',
	'ux/RadioColumn',
	'ux/CheckColumn',
	'ux/App',
	'ux/Module',
	'ux/Portal',
	'ux/ComboBox',
	'ux/DashboardSplitter',
	'ux/AutoHideLoadMask',
	'ux/menu/Item',
	'util/transport',
	'ux/layout/container/boxOverflow/Menu',
	'pref/preference',
	'../js-lib/shindig/util',
	'../js-lib/shindig/json',
	'../js-lib/shindig/rpc',
	'../js-lib/shindig/pubsub_router',

	//for kernel widget compatibility
	'kernel/kernel-rpc-base',

	'eventing/Container',
	'intents/WidgetIntentsContainer',
	'launcher/WidgetLauncherContainer',
	'marketplace/AddWidgetContainer',
	'dd/WidgetDragAndDropContainer',
	'util/output',
	'util/guid',
	'data/JsonProxy',
	'data/OWFTransportProxy',
	'data/OWFStore',
	'components/keys/KeyEventing',
	'components/tip/QuickTip',
	'components/tip/ToolTip',
	'components/widget/WidgetIframeComponent',
	'components/focusable/FocusCatch',
	'components/widget/WidgetBase',
	'components/widget/WidgetPanel',
	'components/widget/WidgetToolbarItem',
	'data/models/State',
	'data/stores/StateStore',
	'data/models/Dashboard',
	'data/stores/DashboardStore',
	'data/ModelIdGenerator',
	'data/models/WidgetDefinition',
	'data/stores/WidgetStore',
	'data/models/Group',
	'data/stores/GroupStore',
	'layout/create_view_container',
	'components/button/UserMenuButton',
	'components/focusable/Focusable',
	'components/layout/SearchBoxLayout',
	'components/form/field/SearchBox',
	'components/grid/column/TextColumn',
	'components/layout/container/boxOverflow/Menu',
	'components/layout/container/HBox',
	'components/layout/BufferedCardLayout',
	'components/layout/PinnableAccordion',
	'components/panel/WidgetHeader',
	'components/panel/WidgetTool',
	'components/tab/WidgetTabPanel',
	'components/tab/WidgetTabBar',
	'components/tab/WidgetTab',
	'components/toolbar/WidgetToolbar',
	'components/marketplace/SortablePagingToolbar',
	'components/focusable/FocusableView',
	'components/view/TemplateEventDataView',
	'components/window/ModalWindow',
	'components/window/WidgetSwitcher',
	'components/window/ManagerWindow',
	'components/window/AdminToolsWindow',
	'components/window/MarketplaceWindow',
	'components/window/MetricWindow',
	'components/window/AlertWindow',
	'components/window/CreateDashboardWindow',
	'components/window/ProfileWindow',
	'components/dashboarddesigner/DraggableView',
	'components/dashboarddesigner/BaseLayout',
	'components/dashboarddesigner/LayoutType',
	'components/dashboarddesigner/Pane',
	'components/dashboarddesigner/SidePanel',
	'components/dashboarddesigner/WorkingArea',
	'components/dashboarddesigner/DashboardDesigner',
	'components/theming/ThemeBrowser',
	'components/theming/ThemeInfoPanel',
	'components/theming/ThemeSwitcherWindow',
	'components/dashboard/DashboardContainer',
	'components/dashboard/Dashboard',
	'components/window/DashboardSwitcher',
	'components/window/HelpWindow',
	'components/launchMenu/DDView',
	'components/launchMenu/Carousel',
	'components/launchMenu/AdvancedSearchPanel',
	'components/launchMenu/WidgetView',
	'components/launchMenu/WidgetViewContainer',
	'components/launchMenu/LaunchMenu',
	'components/pane/Pane',
	'components/pane/DesktopPane',
	'components/pane/AccordionPane',
	'components/pane/PortalPane',
	'components/pane/TabbedPane',
	'components/pane/FitPane',
	'components/window/SettingsWindow',
	'components/view/ToolDataView',
	'components/view/TagCloud',
	'components/button/ShareButton',
	'components/banner/Banner',
	'components/widget/WidgetPortlet',
	'components/widget/WidgetWindow',
	'state/WidgetStateStoreProvider',
	'state/WidgetStateContainer',
	'marketplace/MPListingsRetriever',
	'marketplace/MPCategoryRetriever',
	'marketplace/MPListingsAPI',
	'marketplace/MPCategoryAPI',
	'components/marketplace/MarketplaceWindow',
	'components/marketplace/MPWidgetDetailsPanel',
	'chrome/WidgetChromeContainer',
	'components/widget/DeleteWidgetsPanel',
	'metrics/BaseMetrics',
	//this patch file should be at the end
	'../js-lib/patches/RemoveListenerCaptureBugOverrides'

]


//baseDir exists then use svn version num as part of the version number
def basedir = BuildSettingsHolder.settings?.baseDir
if (basedir != null) {
	uiperformance.determineVersion = { it ->
		def version = System.getenv('SVN_REVISION')

		//if SVN_REVISION is not defined (it is typically only defined by jenkins),
		//pick a random number instead
		if (!version) {
			version = new Random().nextInt()
		}

		if (version.toString().charAt(0) != '-' ) version = '-' + version
		uiperformance.exclusions << "**/*${server.version + version}*"
		server.version + version
	}
}

uiperformance.continueAfterMinifyJsError = true
uiperformance.keepOriginals = true
uiperformance.deleteMinified = false
uiperformance.bundles = [
	[
		type: 'js',
		name: 'owf-widget',

		//custom fields for createBundles grails script
		minifiedName: 'owf-widget-min',
		debugName: 'owf-widget-debug',
		alternateDestDir: 'js-min',
		//custom fields for createBundles grails script

		files: [
			'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed',
			'util/pageload',
			'util/version',
			'util/util',
			'util/guid',
			'components/keys/HotKeys',
			'components/keys/KeyEventSender',
			'lang/ozone-lang',
			'lang/DateJs/globalization/en-US',
			'util/transport',
			'util/widget_utils',
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'../js-lib/log4javascript/log4javascript',
			'util/log',
			'pref/preference',
			'eventing/Widget',
			'intents/WidgetIntents',
			'chrome/WidgetChrome',
			'dd/WidgetDragAndDrop',
			'launcher/WidgetLauncher',
			'state/WidgetStateHandler',
			'state/WidgetState',
			'eventing/WidgetProxy',
			'kernel/kernel-client',
			'metrics/BaseMetrics',
			'widget/Widget',
			'widget/widgetInit'
		]
	],
	[
		type: 'js',
		name: 'owf-admin-widget',

		files: [
			'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed',
			'../js-lib/ext-4.0.7/ext-all-debug',
			'../js-lib/log4javascript/log4javascript',
			'../js-lib/patches/BorderLayoutAnimation',
			'../js-lib/patches/menuAlign',
			'../js-lib/patches/menuBlankImage',
			'../js-lib/patches/firefox_computed_style_on_hidden_elements_patch',
			'../js-lib/patches/CreateInterceptor',
			'../js-lib/patches/PluginMerge',
			'../js-lib/patches/TextFieldOverrides',
			'../js-lib/patches/GridScroller',
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'ux/menu/Item',
			'util/version',
			'util/log',
			'util/pageload',
			'util/transport',
			'util/util',
			'util/widget_utils',
			'lang/ozone-lang',
			'eventing/Widget',
			'pref/preference',
			'components/focusable/FocusableGridPanel',
			'components/keys/HotKeys',
			'components/keys/KeyEventSender',
			'components/util/InstanceVariablizer',
			'launcher/WidgetLauncher',
			'state/WidgetStateHandler',
			'state/WidgetState',
			'chrome/WidgetChrome',
			'dd/WidgetDragAndDrop',
			'data/OWFTransportProxy',
			'data/OWFStore',
			'components/focusable/CircularFocus',
			'components/focusable/Focusable',
			'components/layout/SearchBoxLayout',
			'components/form/field/SearchBox',
			'components/admin/AdminEditorAddWindow',
			'components/admin/WidgetAlerts',
			'components/admin/ManagementPanel',
			'widget/Widget',
			'widget/widgetInit'
		]
	],
	[
		type: 'js',
		name: 'owf-marketplace-approval-widget',

		files: [
			'../js/data/models/WidgetDefinition',
			'../js/data/stores/AdminWidgetStore',
			'../js/data/stores/WidgetApprovalStore',
			'../js/components/admin/grid/WidgetApprovalsGrid',
			'../js/components/admin/widget/WidgetDetailPanel',
			'../js/components/admin/widget/ApprovePanel',
			'../js/components/admin/widget/WidgetApprovalPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-dashboard-edit-widget',

		files: [
			'util/guid',
			'../js/data/models/Dashboard',
			'../js/data/stores/AdminDashboardStore',
			'../js/data/models/Group',
			'../js/data/stores/GroupStore',
			'../js/components/admin/grid/GroupsGrid',
			'../js/components/admin/EditWidgetPanel',
			'../js/components/admin/GroupsTabPanel',
			'../js/components/admin/PropertiesPanel',
			'../js/components/admin/dashboard/DashboardEditPropertiesTab',
			'../js/components/admin/dashboard/DashboardEditGroupsTab',
			'../js/components/admin/dashboard/DashboardEditPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-group-dashboard-management-widget',

		files: [
			'data/models/Dashboard',
			'data/stores/AdminDashboardStore',
			'components/admin/grid/DashboardGroupsGrid',
			'components/admin/dashboard/DashboardDetailPanel',
			'components/admin/dashboard/GroupDashboardManagementPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-group-management-widget',

		files: [
			'data/models/Group',
			'data/stores/GroupStore',
			'components/admin/grid/GroupsGrid',
			'components/admin/group/GroupDetailPanel',
			'components/admin/group/GroupManagementPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-group-edit-widget',

		files: [
			'../js/data/models/Dashboard',
			'../js/data/stores/AdminDashboardStore',
			'../js/data/models/Group',
			'../js/data/stores/GroupStore',
			'../js/data/models/User',
			'../js/data/stores/UserStore',
			'../js/data/models/WidgetDefinition',
			'../js/data/stores/AdminWidgetStore',
			'../js/components/admin/grid/DashboardsGrid',
			'../js/components/admin/grid/UsersGrid',
			'../js/components/admin/grid/WidgetsGrid',
			'../js/components/admin/DashboardsTabPanel',
			'../js/components/admin/EditWidgetPanel',
			'../js/components/admin/PropertiesPanel',
			'../js/components/admin/UsersTabPanel',
			'../js/components/admin/WidgetsTabPanel',
			'../js/components/admin/dashboard/DashboardDetailPanel',
			'../js/components/admin/group/GroupEditDashboardsTab',
			'../js/components/admin/group/GroupEditPropertiesTab',
			'../js/components/admin/group/GroupEditUsersTab',
			'../js/components/admin/group/GroupEditWidgetsTab',
			'../js/components/admin/group/GroupEditPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-user-management-widget',

		files: [
			'data/models/User',
			'data/stores/UserStore',
			'components/admin/grid/UsersGrid',
			'components/admin/user/UserDetailPanel',
			'components/admin/user/UserManagementPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-user-edit-widget',

		files: [
			'util/guid',
			'../js/data/models/Dashboard',
			'../js/data/stores/AdminDashboardStore',
			'../js/data/models/Group',
			'../js/data/stores/GroupStore',
			'../js/data/models/Preference',
			'../js/data/stores/PreferenceStore',
			'../js/data/models/User',
			'../js/data/stores/UserStore',
			'../js/data/models/WidgetDefinition',
			'../js/data/stores/AdminWidgetStore',
			'../js/components/admin/EditPreferenceWindow',
			'../js/components/admin/EditDashboardWindow',
			'../js/components/admin/grid/DashboardsGrid',
			'../js/components/admin/grid/GroupsGrid',
			'../js/components/admin/grid/PreferencesGrid',
			'../js/components/admin/grid/WidgetsGrid',
			'../js/components/admin/DashboardsTabPanel',
			'../js/components/admin/EditWidgetPanel',
			'../js/components/admin/GroupsTabPanel',
			'../js/components/admin/PreferencesTabPanel',
			'../js/components/admin/PropertiesPanel',
			'../js/components/admin/WidgetsTabPanel',
			'../js/components/admin/user/UserEditDashboardsTab',
			'../js/components/admin/user/UserEditPreferencesTab',
			'../js/components/admin/user/UserEditPropertiesTab',
			'../js/components/admin/user/UserEditGroupsTab',
			'../js/components/admin/user/UserEditWidgetsTab',
			'../js/components/admin/user/UserEditPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-widget-management-widget',

		files: [
			'../js/data/models/WidgetDefinition',
			'../js/data/stores/AdminWidgetStore',
			'../js/components/admin/ExportWindow',
			'../js/components/admin/grid/WidgetsGrid',
			'../js/components/admin/widget/DeleteWidgetsPanel',
			'../js/components/admin/widget/WidgetDetailPanel',
			'../js/components/admin/widget/WidgetManagementPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-widget-edit-widget',

		files: [
			'util/guid',
			'../js/data/ModelIdGenerator',
			'../js/components/admin/UrlField',
			'../js/data/models/Group',
			'../js/data/stores/GroupStore',
			'../js/data/models/Intent',
			'../js/data/stores/IntentStore',
			'../js/data/models/User',
			'../js/data/stores/UserStore',
			'../js/data/models/WidgetDefinition',
			'../js/data/stores/AdminWidgetStore',
			'../js/data/models/WidgetType',
			'../js/data/stores/WidgetTypeStore',
			'../js/components/admin/EditIntentWindow',
			'../js/components/admin/grid/IntentsGrid',
			'../js/components/admin/grid/GroupsGrid',
			'../js/components/admin/grid/UsersGrid',
			'../js/components/admin/EditWidgetPanel',
			'../js/components/admin/GroupsTabPanel',
			'../js/components/admin/IntentsTabPanel',
			'../js/components/admin/PropertiesPanel',
			'../js/components/admin/UsersTabPanel',
			'../js/components/admin/widget/WidgetEditGroupsTab',
			'../js/components/admin/widget/WidgetEditUsersTab',
			'../js/components/admin/widget/WidgetEditPropertiesTab',
			'../js/components/admin/widget/WidgetEditPanel'
		]
	],
	[
		type: 'js',
		name: 'owf-server',
		//custom fields for createBundles grails script
		minifiedName: 'owf-server-min',
		debugName: 'owf-server-debug',
		//custom fields for createBundles grails script
		files: owfServerBaseJavascript
	],
	[
		type: 'js',
		name: 'owf-server-all-en',
		//custom fields for createBundles grails script
		minifiedName: 'owf-server-all-en-min',
		debugName: 'owf-server-all-en-debug',
		//custom fields for createBundles grails script
		files: owfServerBaseJavascript + [
			"lang/ozone-lang-en_US",
			"../js-lib/ext-4.0.7/locale/ext-lang-en",
			"marketplace/marketplaceAPI"
		]
	],
	[
		type: 'js',
		name: 'owf-lite-client',
		minifiedName: 'owf-lite-client-min',
		debugName: 'owf-lite-client-debug',
		alternateDestDir: 'js-min',
		excludeFromWar: true,
		files: [
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'eventing/WidgetProxy',
			'kernel/kernel-client'
		]
	],
	[
		type: 'js',
		name: 'owf-lite-client-with-dojo',
		minifiedName: 'owf-lite-client-with-dojo-min',
		debugName: 'owf-lite-client-with-dojo-debug',
		alternateDestDir: 'js-min',
		excludeFromWar: true,
		files: [
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'eventing/WidgetProxy',
			'kernel/kernel-client',
			'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed'
		]
	],
	[
		type: 'js',
		name: 'owf-lite-container',
		minifiedName: 'owf-lite-container-min',
		debugName: 'owf-lite-container-debug',
		alternateDestDir: 'js-min',
		excludeFromWar: true,
		files: [
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'kernel/kernel-rpc-base',
			'kernel/kernel-container'
		]
	],
	[
		type: 'js',
		name: 'owf-lite-container-with-dojo',
		minifiedName: 'owf-lite-container-with-dojo-min',
		debugName: 'owf-lite-container-with-dojo-debug',
		alternateDestDir: 'js-min',
		excludeFromWar: true,
		files: [
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub',
			'kernel/kernel-rpc-base',
			'kernel/kernel-container',
			'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed'
		]
	],
	[
		type: 'js',
		name: 'owf-lite-container-compat',
		minifiedName: 'owf-lite-container-compat-min',
		debugName: 'owf-lite-container-compat-debug',
		alternateDestDir: 'js-min',
		excludeFromWar: true,
		files: [
			'../js-lib/shindig/util',
			'../js-lib/shindig/json',
			'../js-lib/shindig/rpc',
			'../js-lib/shindig/pubsub_router',
			'kernel/kernel-rpc-base',
			'kernel/kernel-container',
			'../js-lib/dojo-1.5.0-windowname-only/dojo/owfdojo.js.uncompressed',
			'util/version',
			//                'util/util',
			//                'util/guid',
			'dd/WidgetDragAndDropContainer'
		]
	]

]

owf.url = System.getProperty('ozone.port') ? "${System.getProperty('ozone.host')}:${System.getProperty('ozone.port')}": "${System.getProperty('ozone.host')}"
omp.url = System.getProperty('marketplace.port')? "${System.getProperty('marketplace.host')}:${System.getProperty('marketplace.port')}" : "${System.getProperty('marketplace.host')}"
met.url = System.getProperty('metric.port') ? "${System.getProperty('metric.host')}:${System.getProperty('metric.port')}" : "${System.getProperty('metric.host')}"
jmxSslPort = Integer.parseInt(System.getProperty('ozone.jmxSslPort') ?: "8012")

mpSyncTrustAll = false
mpSyncTrustProvidedUrl = false
mpSyncHostTimeout = Integer.parseInt(System.getProperty('mp.sync.timeout') ?: "10000")

// User update interval, in milliseconds. Default to 24 hours.
userUpdateMillis = Integer.parseInt(System.getProperty('userUpdateMillis') ?: "43200000")

bannerText = System.getProperty('ozone.banner.text')

//main owf config object
owf {
	version = appVersion // From application.properties
	serverVersion = appVersion

	// log4j file watch interval in milliseconds
	log4jWatchTime = 180000 // 3 minutes

	marketplaceLocation = "https://" + omp.url + "/marketplace"
	mpInitialPollingInterval = 5000
	mpPollingInterval = 300000
	mpVersion = "5.0"

	enablePendingApprovalWidgetTagGroup = false
	pendingApprovalTagGroupName = 'pending approval'
	approvedTagGroupName = 'approved'

	adminBannerIcon = "images/adminLogo52.png"
	adminBannerIconHeight = 52
	adminBannerIconWidth = 346
	adminBannerPageTitle = "Admininistration"

	sendWidgetLoadTimesToServer = false
	publishWidgetLoadTimes = false

	showLastLogin = false
	lastLoginDateFormat = 'n/j/Y G:i'

	defaultTheme = "a_default"

	showAccessAlert = false
	accessAlertMsg = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla interdum eleifend sapien dignissim malesuada. Sed imperdiet augue vitae justo feugiat eget porta est blandit. Proin ipsum ipsum, rutrum ac gravida in, ullamcorper a augue. Sed at scelerisque augue. Morbi scelerisque gravida sapien ut feugiat. Donec dictum, nisl commodo dapibus pellentesque, enim quam consectetur quam, at dictum dui augue at risus. Ut id nunc in justo molestie semper. Curabitur magna velit, varius eu porttitor et, tempor pulvinar nulla. Nam at tellus nec felis tincidunt fringilla. Nunc nisi sem, egestas ut consequat eget, luctus et nisi. Nulla et lorem odio, vitae pretium ipsum. Integer tellus libero, molestie a feugiat a, imperdiet sit amet metus. Aenean auctor fringilla eros, sit amet suscipit felis eleifend a."

	// Specifies a freeTextEntryMessage to appear on all dialogs which allow text entry
	// To turn off the warning message, use the following:
	//     freeTextEntryWarningMessage=''
	freeTextEntryWarningMessage = ''

	//use to specify a logout url
	logoutURL = "/logout"

	//sets the autoSave interval for saving dashboards in milliseconds 900000 is 15 minutes
	autoSaveInterval = 900000

	helpFileRegex = '^.*\\.(htm|html|gsp|jsp|pdf|doc|docx|mov|wmv|swf|ppt|pptx|xls)$'

	//this value controls whether the OWF UI uses shims on floating elements, setting this to true will make
	//Applet/Flex have less zindex issues, but browser performance may suffer due to the additional shim frames being created
	useShims = false

	//Locations for the optional external themes and help directories.
	//Default: 'themes', 'help', and 'js-plugins' directories on the classpath.
	//Can be configured to an arbitrary file path.  The following
	//path styles are supported:
	//  'file:/some/absolute/path' ('file:C:/some/absolute/path' on Windows)
	//  'classpath:location/under/classpath'
	//  'location/within/OWF/war/file'
	external {
		themePath = 'classpath:themes'
		helpPath = 'classpath:help'
		jsPluginPath = 'classpath:js-plugins'
	}

	// Optional Configuration elements for custom headers/footers.
	// Example values are shown.  File locations are relative or absolute paths to
	// resources hosted on the owf web server.  Heights are in pixel amounts.
	customHeaderFooter {
		header = '/context/header.gsp'
		headerHeight = 20
		footer = ''
		footerHeight = 0
		jsImports = []
		cssImports = []
	}

	metric {
		enabled = false
		url = "https://" + met.url + "/metric/metric" // The two metric is intentional
		keystorePath = System.properties['javax.net.ssl.keyStore']
		keystorePass = System.properties['javax.net.ssl.keyStorePassword']
		truststorePath = System.properties['javax.net.ssl.trustStore']
	}
}

environments {
	development {
		mpSyncTrustAll = true

		dataSource {
			jndiName = 'java:comp/env/jdbc/raptor'
			dbCreate = false
		}

		grails.plugin.databasemigration.updateOnStart = Boolean.valueOf(System.getProperty('owf.dbmigration.active') ?: true)
		grails.plugin.databasemigration.updateOnStartContexts = "create"
		grails.plugin.databasemigration.updateOnStartFileNames = [
			"changelog_webtops.groovy"
		]
		uiperformance.enabled = false
	}

	test {
		grails.plugin.databasemigration.updateOnStart = false
		grails.plugin.databasemigration.updateOnStartContexts = "upgrade"
		grails.plugin.databasemigration.updateOnStartFileNames  = ["changelog_webtops.groovy"]
	}

	production {
		dataSource {
			jndiName = 'java:comp/env/jdbc/raptor'
			dbCreate = false
		}

		grails.plugin.databasemigration.updateOnStart = Boolean.valueOf(System.getProperty('owf.dbmigration.active') ?: true)
		grails.plugin.databasemigration.updateOnStartContexts = "upgrade"
		grails.plugin.databasemigration.updateOnStartFileNames  = ["changelog_webtops.groovy"]

		uiperformance.enabled = true
	}
}
