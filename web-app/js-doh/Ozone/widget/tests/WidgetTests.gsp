<html>
<head>
    <title>Widget Tests</title>
    <script type="text/javascript">
        djConfig = {
                isDebug: true,
                parseOnLoad: false,
                baseUrl: "../../../../js-lib/dojo-release-1.5.0/dojo/",
                scopeMap: [
                        ["dojo", "owfdojo"],
                        ["dijit", "owfdijit"],
                        ["dojox", "owfdojox"]
                ]
        }
    </script>
    <script type="text/javascript" src="../../../../js-lib/dojo-release-1.5.0/dojo/dojo.js"></script>
    <script type='text/javascript' src='../../../../config'></script>
    <!-- include our server bundle, in dev mode a full list of js includes will appear -->
    <p:javascript src='owf-server'/>
    <!-- include our server bundle, in dev mode a full list of js includes will appear -->
    <script type="text/javascript">
        dojo = owfdojo;
        dojox = owfdojox;

        dojo.registerModulePath("Ozone", "../../../js-doh/Ozone");
        dojo.provide("Ozone.eventing.tests.WidgetChromeRemoteTests");
        dojo.require("dojo.parser");
        dojo.require("doh.runner");
        dojo.require("dojox.secure.capability");

        dojo.addOnLoad(function() {

            // eventing configuration
            var serviceName = '_getCurrentTheme';
            Ozone.eventing.Container.init();
            var container = Ozone.eventing.Container;

            var altHostName = Ozone.config.alternateHostName ? Ozone.config.alternateHostName : '127.0.0.1';
            var port = window.location.port != '' ? ':'+window.location.port : '';
            var widget = {
                id: guid.util.guid(),
                widgetGuid: guid.util.guid(),
                url: window.location.protocol + "//" + (altHostName + port +"/"
                        + Ozone.util.contextPath()
                        +'/js-doh/Ozone/widget/tests/getCurrentTheme_Widget.gsp').replace(/\/{2,}/,'/')
            };
          var widgetFrameIdObj = container.generateIframeId(widget.id, widget, 'desktop', 1);
          var widgetFrameId = Ozone.util.formatWindowNameData(widgetFrameIdObj);
          var iframeTag = "<iframe " + container.getIframeProperties(widget.url, widget.id, widget) + " width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"auto\"></iframe>";

            doh.register("Ozone.widget.WidgetTests", [
                {
                    name: "testGetCurrentTheme",
                    setUp: function() {
                        //Setup to do before runTest.
                    },
                    runTest: function() {
                        //Our test function to run.
                        var deferred = new doh.Deferred();

                        //register event handler
                        container.registerHandler(serviceName, function(sender, msg) {
                            var widgetId = Ozone.util.parseJson(sender);
                            doh.assertEqual(widgetId.id,widget.id);
                            doh.assertEqual(msg,Ozone.util.toString(Ozone.config.currentTheme));

                            dojo.byId('message').innerHTML+='current theme data: <b>'+Ozone.util.toString(Ozone.config.currentTheme)+'</b><p>';
                            dojo.byId('message').innerHTML+='received theme data from widget: <b>'+msg+'</b>';

                            deferred.callback(true);
                        });
                        
                        return deferred;
                    },
                    tearDown: function() {
                        //cleanup to do after runTest.
                    },
                    timeout: 10000 //10 second timeout.
                }
            ]);

            //Execute D.O.H. in this remote file.
            doh.run();


            //write out widget iframe <!-- this iframe is loaded via 127.0.0.1 which means it will be xdomain for browsers pointed at localhost-->
            dojo.byId('widget').innerHTML = iframeTag;
        });
    </script>
</head>
<body>
<h>Widget Tests</h>
<div style="border: 2px solid black;">
    <h>Container</h>
    <div id="message"></div>
    <div id="widget_container" style="border: 2px solid black;">
        Widget
        <div id="widget">
        </div>
    </div>
</div>
</body>
</html>
