package ozone.owf.grails.services.model

import ozone.owf.grails.domain.AbstractServiceModel

class WidgetTypeServiceModel extends AbstractServiceModel {

	Long id
	String name

	Map toDataMap() {
		return [
			id: id,
			name: name
		]
	}
}