package ozone.owf.grails.domain

import java.util.Map

import grails.gorm.DetachedCriteria

import org.hibernate.proxy.HibernateProxy

/**
 * User domain class.
 */
class Person implements Serializable, OzoneServiceModel {

	static String TYPE = 'person'
	static final long serialVersionUID = 700L

	static final String NEW_USER = "DEFAULT_USER"
	static final String SYSTEM = "SYSTEM"

	static hasMany = [dashboards: Dashboard, personWidgetDefinitions: PersonWidgetDefinition, preferences: Preference, groups: Group]
	static belongsTo = [Group]

	static mappedBy = [dashboards: 'user']

	static mapping = {
		cache true
		groups(lazy: true, cache: true)
		dashboards(lazy: true, cascade: "delete-orphan", cache: true)
		personWidgetDefinitions(lazy: true, cascade: "delete-orphan", cache: true)
		preferences(lazy: true, cascade: "delete-orphan", cache: true)
	}

	static constraints = {
		username(blank: false, unique: true, maxSize: 200)
		userRealName(blank: false, maxSize: 200)
		description(nullable: true, blank: true)
		email(nullable: true, blank: true)
		lastLogin(nullable: true)
		prevLogin(nullable: true)
	}

	String username
	String userRealName
	boolean enabled
	String email
	boolean emailShow
	String description = ''
	Date lastLogin
	Date prevLogin

	String toString() {
		"$userRealName:  ($username)"
	}

	boolean equals(other) {
		if (other instanceof Person || (other instanceof HibernateProxy && other.instanceOf(Person))) {
			other?.username == username
		}
		else {
			false
		}
	}

	int hashCode() {
		username ? username.hashCode() : 0
	}

	boolean isOlderThan(millis) {
		// This should never be negative, but in the off chance someone does
		// something wonky with the database....
		return Math.abs(System.currentTimeMillis() - lastLogin.getTime()) > millis
	}

	public PersonServiceModel toServiceModel(Map params) {
		return new PersonServiceModel(this, params)
	}

	// Total hack. As soon as the migration of Person to OzonePrincipal is complete, everything below here should be removed.
	def afterInsert() {
		updatePrincipalFromPerson()
	}

	def afterUpdate() {
		updatePrincipalFromPerson()
	}

	def beforeDelete() {
		deletePrincipalsForDeletedPerson()
	}

	private void updatePrincipalFromPerson() {
		def pr = OzonePrincipal.findByPersonId(this.id)
		if (!pr) {
			pr = OzonePrincipal.findByCanonicalNameAndType(this.username, 'user')
		}

		if (!pr) {
			pr = OzonePrincipal.fromPerson(this)
		}
		else {
			pr.updateFromPerson(this)
		}
		pr.save()
	}

	private void deletePrincipalsForDeletedPerson() {
		def criteria = new DetachedCriteria(OzonePrincipal).build {
			eq 'personId', this.id
		}
		criteria.deleteAll()
	}
}

class PersonServiceModel extends AbstractServiceModel {
	Long id
	String username
	String userRealName
	String email
	Boolean hasPWD
	List tagLinks = []
	Integer totalGroups = 0
	Integer totalWidgets = 0
	Integer totalDashboards = 0
	Long lastLogin

	public PersonServiceModel(Person p) {
		this(p, null)
	}

	public PersonServiceModel(Person p, Map params) {
		this.id = p.id
		this.username = p.username
		this.userRealName = p.userRealName
		this.email = p.email ?: ''
		this.lastLogin = p.lastLogin ? p.lastLogin.getTime() : null
		this.totalWidgets = params?.totalWidgets ?: 0
		this.totalGroups = params?.totalGroups ?: 0
		this.totalDashboards = params?.totalDashboards ?: 0
	}

	// Required to prevent the loss of the id attribute.
	Map toDataMap() {
		return [
			id: id,
			username: username,
			userRealName: userRealName,
			email: email,
			hasPWD: hasPWD,
			lastLogin: lastLogin,
			totalGroups: totalGroups,
			totalWidgets: totalWidgets,
			totalDashboards: totalDashboards,
			tagLinks: tagLinks
		]
	}
}
