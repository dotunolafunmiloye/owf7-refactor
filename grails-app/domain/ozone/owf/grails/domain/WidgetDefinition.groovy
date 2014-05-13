package ozone.owf.grails.domain

class WidgetDefinition implements Serializable {

	static String TYPE = 'widget_definition'
	static final long serialVersionUID = 700L

	String universalName
	String widgetGuid
	String displayName
	String description = ''
	String widgetUrl
	String imageUrlSmall
	String imageUrlLarge
	Integer width
	Integer height
	String widgetVersion
	Boolean visible = true
	Boolean singleton = false
	Boolean background = false
	String descriptorUrl
	WidgetType widgetType

	static hasMany = [personWidgetDefinitions: PersonWidgetDefinition]
	static transients = ['allRequired', 'directRequired']

	static constraints = {
		universalName(nullable: true, blank: true, maxSize: 255)
		widgetGuid(nullable: false, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/, unique: true)
		displayName(maxSize: 256)
		description(nullable: true, blank: true, maxSize: 4000)
		widgetVersion(nullable: true, blank: true, maxSize: 2083)
		widgetUrl(maxSize: 2083)
		imageUrlLarge(maxSize: 2083)
		imageUrlSmall(maxSize: 2083)
		height(min: 200)
		width(min: 200)
		descriptorUrl(nullable: true, blank: true, maxSize: 2083)
	}

	static mapping = {
		personWidgetDefinitions(lazy: true, cascade: "all,delete-orphan", cache: true)
		widgetType column: 'widget_type_id'
	}

	String toString() {
		"${displayName}: " + "(${this.id} - " + "${this.widgetGuid})"
	}

	/**
	 * Returns the current widget definition as a map suitable for use by the export function of Ozone.
	 *
	 * @return this object as a Map.
	 */
	public Map asExportableMap() {
		def mpReturn = [:]

		// Get only the values required for a widget descriptor
		mpReturn.put("displayName", this.displayName)
		mpReturn.put("widgetUrl", this.widgetUrl)
		mpReturn.put("imageUrlSmall", this.imageUrlSmall)
		mpReturn.put("imageUrlLarge", this.imageUrlLarge)
		mpReturn.put("width", this.width)
		mpReturn.put("height", this.height)
		mpReturn.put("visible", this.visible)
		mpReturn.put("singleton", this.singleton)
		mpReturn.put("background", this.background)
		mpReturn.put("widgetTypes", [this.widgetType.name])

		// Add non-required fields
		this.descriptorUrl && mpReturn.put("descriptorUrl", this.descriptorUrl)
		this.universalName && mpReturn.put("universalName", this.universalName)
		this.description && mpReturn.put("description", this.description)
		this.widgetVersion && mpReturn.put("widgetVersion", this.widgetVersion)

		// No longer used, but keeping this to preserve compatibility from an API perspective.
		mpReturn.put("defaultTags", [])

		mpReturn
	}
}
