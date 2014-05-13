package ozone.owf.grails.services.model

import ozone.owf.grails.domain.AbstractServiceModel

class TagLinkServiceModel extends AbstractServiceModel {

	String name
	Boolean visible
	Integer position
	Boolean editable

	Map toDataMap() {
		return [
			'name': name,
			'visible': visible,
			'position': position,
			'editable': editable
		]
	}
}
