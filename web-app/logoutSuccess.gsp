<!DOCTYPE html>
<%@ page contentType="text/html; UTF-8"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title id='title'>Ozone Widget Framework</title>

<link rel="shortcut icon" href="images/favicon.ico" />

<!-- ** CSS ** -->
<!-- base library -->
<p:css id='theme' name='${owfCss.defaultCssPath()}' absolute='true' />

<!-- initialize ozone configuration from server -->
<script type="text/javascript" src="${resource(file: 'config')}"></script>

<!-- include our server bundle, in dev mode a full list of js includes will appear -->
<p:javascript src='owf-server' />
<!-- include our server bundle, in dev mode a full list of js includes will appear -->

<script language="javascript">
	owfdojo.config.dojoBlankHtmlUrl = './js-lib/dojo-1.5.0-windowname-only/dojo/resources/blank.html';
</script>

<!-- bring in custom header/footer resources -->
<g:each
	in="${grailsApplication.config.owf.customHeaderFooter.jsImports}"
	var="jsImport">
	<script type="text/javascript" src="${jsImport}"></script>
</g:each>
<g:each
	in="${grailsApplication.config.owf.customHeaderFooter.cssImports}"
	var="cssImport">
	<link rel="stylesheet" href="${cssImport}" type="text/css" />
</g:each>

<!-- language switching -->
<lang:preference lang="${params.lang}" />

<!-- set Marketplace Version -->
<script type="text/javascript" src="${resource(dir: 'js/marketplace', file: 'marketplaceAPI.js')}"></script>


<script type="text/javascript">
	Ext.define('Ozone.ux.LogoutMask', {
		extend : 'Ext.LoadMask',

		initComponent : function() {
			var me = this;
			me.callParent(arguments);
		},

		show : function() {
			var me = this;
			me.callParent(arguments);
		}
	});
	window.opener = null;

	function initLayoutComponents(customHeaderFooter, floatingWindowManager) {
		var layoutComponents = [];

		// create panel for custom header
		var showHeader = (customHeaderFooter.header != "" && customHeaderFooter.headerHeight > 0);
		var customHeader = {
			id : 'customHeaderComponent',
			xtype : 'component',
			border : false,
			frame : false,
			hidden : !showHeader,
			height : customHeaderFooter.headerHeight
		};

		// calculate height offset for main component
		var heightOffset = 0;

		if (showHeader) {
			heightOffset = heightOffset - customHeaderFooter.headerHeight;
		}

		// Build the layout components array.  Add functional panels as necessary.
		if (showHeader) {
			customHeader.loader = {
				url : customHeaderFooter.header,
				autoLoad : true,
				callback : Ozone.config.customHeaderFooter.onHeaderReady
			}
			layoutComponents.push(customHeader);
		}

		return layoutComponents;
	}

	var handleBodyOnScrollEvent = function() {
		document.body.scrollTop = 0;
		document.body.style.overflow = "hidden";
		document.body.scroll = "no";
		scroll(0, 0);
		return;
	};

	if (Ext.isIE) {
		Ext.BLANK_IMAGE_URL = './themes/common/images/s.gif';
	}

	Ext
			.onReady(function() {
				handleBodyOnScrollEvent();
				var floatingWindowManager = new Ext.ZIndexManager();

				var layoutComponents = initLayoutComponents(
						Ozone.config.customHeaderFooter, floatingWindowManager);
				var continueProcessingPage = function() {
					OWF.Mask = new Ozone.ux.LogoutMask(
							Ext.getBody(),
							{
								msg : "You must close your browser to complete the sign out process.",
								id : 'owf-body-mask'
							});
					OWF.Mask.show();

					Ext.create('Ext.container.Viewport', {
						id : 'viewport',
						cls : 'viewport',
						layout : {
							type : 'fit'
						},
						items : [ {
							xtype : 'container',
							style : 'overflow:hidden',
							layout : 'anchor',
							items : layoutComponents
						} ]
					});

				};
				continueProcessingPage();
			});
</script>
</head>

<body id="owf-body" onscroll="handleBodyOnScrollEvent();">

</body>
</html>
