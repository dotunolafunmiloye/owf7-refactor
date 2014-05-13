package ozone.owf.grails.services.model

import ozone.owf.grails.domain.AbstractServiceModel

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
				tags: [],
				singleton: singleton,
				visible: visible,
				background: background,
				descriptorUrl: descriptorUrl,
				definitionVisible: visible,
				directRequired: directRequired,
				allRequired: allRequired,
				widgetTypes: widgetTypes*.toDataMap()
			],
			path: widgetGuid
		]
	}
}
