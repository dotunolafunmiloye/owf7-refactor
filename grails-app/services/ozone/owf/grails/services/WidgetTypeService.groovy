package ozone.owf.grails.services

import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.model.WidgetTypeServiceModel

class WidgetTypeService {

	// FIXME: Retire this by adding the needed ".toServiceModel" function to the domain.
	def serviceModelService

	public List<WidgetTypeServiceModel> list(params) {

		def widgetTypes = null
		widgetTypes = WidgetType.createCriteria().list() {
			order('name', params?.order?.toLowerCase() ?: 'asc')
		}

		List<WidgetTypeServiceModel> processedWidgetTypes = widgetTypes.collect { wt ->
			serviceModelService.createServiceModel(wt)
		}
		return processedWidgetTypes
	}

}
