package ozone.owf.grails.domain

class WidgetType implements Serializable {

	static String MARKETPLACE = 'marketplace'
	static String STANDARD = 'standard'
	static String ADMIN = "administration"

	static String TYPE = 'widget_type'
	static final long serialVersionUID = 700L

	String name

	static constraints = {
		name(nullable: false, blank: false)
	}
}
