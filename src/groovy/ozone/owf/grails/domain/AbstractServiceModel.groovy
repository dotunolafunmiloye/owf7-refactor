package ozone.owf.grails.domain

import grails.converters.JSON
import grails.converters.XML
import ozone.owf.grails.services.model.ServiceModel

abstract class AbstractServiceModel implements ServiceModel {

	Map toDataMap() {
		Map dataMap = [:]

		//ignore lame default class properties don't need these polluting our model
		this.properties.each {
			if (it.key != 'class' && it.key != 'metaClass') {
				dataMap[it.key] = it.value
			}
		}

		return dataMap
	}

	/**
	 * Override asType behavior to add as specialized JSONification or XMLification!
	 * @param clazz
	 * @return
	 */
	Object asType(Class clazz) {

		if (clazz.equals(Map.class)) {
			return toDataMap()
		}
		else if (clazz.equals(JSON.class)) {
			return new JSON(this as Map)
		}
		else if (clazz.equals(XML.class)) {
			return new XML(this as Map)
		}
		//else just do what groovy would normally do
		else {
			return super.asType(clazz)
		}
	}

	/**
	 * defaults to json string
	 * @return
	 */
	String toString() {
		JSON json = this as JSON
		return json.toString(true)
	}
}
