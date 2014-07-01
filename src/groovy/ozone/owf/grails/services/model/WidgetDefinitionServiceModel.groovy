package ozone.owf.grails.services.model

class WidgetDefinitionServiceModel extends AbstractServiceModel {

    String id
    String universalName
    String widgetGuid
    String displayName
    String description
    String widgetUrl
    String imageUrlSmall
    String imageUrlLarge
    Integer width
    Integer height
    Integer totalUsers = 0
    Integer totalGroups = 0
    String widgetVersion
    List tagLinks = []
    Boolean singleton
    Boolean visible
    Boolean background
    String descriptorUrl
    List directRequired = []
    List allRequired = []
    Set intents = []
    List widgetTypes = []

    Map toDataMap() {
        return [
            id: id,
            namespace: "widget",
            value: [
                universalName: universalName,
                namespace: displayName,
                description: description,
                url: widgetUrl,
                headerIcon: imageUrlSmall,
                image: imageUrlLarge,
                smallIconUrl: imageUrlSmall,
                largeIconUrl: imageUrlLarge,
                width: width,
                height: height,
                x: 0,
                y: 0,
                minimized: false,
                maximized: false,
                widgetVersion: widgetVersion,
                totalUsers: totalUsers,
                totalGroups: totalGroups,
                tags: tagLinks*.toDataMap(),
                singleton: singleton,
                visible: visible,
                background: background,
                descriptorUrl: descriptorUrl,
                definitionVisible: visible,
                directRequired: directRequired,
                allRequired: allRequired,
                intents: arrangeIntents(),
                widgetTypes: widgetTypes*.toDataMap()
            ],
            path: widgetGuid
        ]
    }

    //Arrange intents into send and receive arrays
    LinkedHashMap arrangeIntents() {
        def sendIntents = [], receiveIntents = []
        intents.collect { widgetDefinitionIntent ->
            if(widgetDefinitionIntent.intent != null) {
                if(widgetDefinitionIntent.send) {
                    sendIntents.push(action: widgetDefinitionIntent.intent.action, dataTypes: widgetDefinitionIntent.dataTypes*.toString())
                }
                if(widgetDefinitionIntent.receive) {
                    receiveIntents.push(action: widgetDefinitionIntent.intent.action, dataTypes: widgetDefinitionIntent.dataTypes*.toString())
                }
            }
        }

        return [send: sendIntents, receive: receiveIntents]
    }
}
