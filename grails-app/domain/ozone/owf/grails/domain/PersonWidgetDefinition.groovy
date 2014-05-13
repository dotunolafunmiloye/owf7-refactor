package ozone.owf.grails.domain

class PersonWidgetDefinition implements Serializable, Comparable {

	static final long serialVersionUID = 700L

	Person person
	WidgetDefinition widgetDefinition
	Integer pwdPosition
	String displayName
	Boolean groupWidget = false  // True if the PWD was added to a user because of their group membership
	Boolean userWidget = false // True if the PWD was added directly to a widget.
	Boolean favorite = false
	Boolean visible = true
	Boolean disabled = false

	// FIXME: This could be better expressed using hasOne, particularly on the widget definition side,
	// as this would eliminate the contention around the widget definition whenever you save a new
	// person widget definition.  Alas, that could be a big ask, so deferring for now.
	//
	// When that change does get made, there is code which runs in either the IndexController or
	// SecurityFilters classes which actually creates PWD objects for WD objects that are mapped
	// to a user by group membership.  That code drops to raw SQL to avoid the Hibernate contention,
	// but if we fix the contention issue, then we could also avoid the raw SQL.
	static belongsTo = [person: Person, widgetDefinition: WidgetDefinition]

	static constraints = {
		displayName(nullable:true, maxSize: 256)
		visible(nullable:false)
		userWidget(nullable:true)
		groupWidget(nullable:true)
		favorite(nullable:true)
		disabled(nullable:true)
		pwdPosition(nullable:false, display:false)
		widgetDefinition(unique:'person')
	}

	static mapping = {
		cache true
		widgetDefinition lazy:true
		person lazy:true
	}


	String toString() {
		"${this.id}: ${this.widgetDefinition} - ${this.person} - visible=${this.visible}"

	}

	int compareTo(that) {
		this.pwdPosition <=> that.pwdPosition
	}
}
