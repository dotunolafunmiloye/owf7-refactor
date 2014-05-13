package ozone.owf.grails.domain

import org.hibernate.proxy.HibernateProxy

class OzonePrincipal implements Serializable {

	static final long serialVersionUID = 700L

	static constraints = {
		canonicalName blank: false, unique: true, maxSize: 200
		friendlyName blank: false, maxSize: 200
		description nullable: true, blank: true
		type blank: false, inList: ['user', 'group']
		lastLogin nullable: true
		personId nullable: true, validator: { val, obj ->
			if (val) { obj.groupId == null }
		}
		groupId nullable: true, validator: { val, obj ->
			if (val) { obj.personId == null }
		}
	}

	static mapping = { table 'principal' }

	String canonicalName
	String friendlyName
	String description
	Date lastLogin
	String type

	// These are to support the transition of legacy Person and Group classes.  When the transition is done, remove these.
	Long personId
	Long groupId

	String toString() {
		"$friendlyName:  ($canonicalName)"
	}

	// These statics support the transition of legacy Person and Group classes.  When the transition is done, remove all of these.
	static OzonePrincipal fromPerson(Person p) {
		return new OzonePrincipal(canonicalName: p.username, friendlyName: p.userRealName, description: p.description, type: 'user', lastLogin: p.lastLogin, personId: p.id)
	}

	static OzonePrincipal fromGroup(Group g) {
		return new OzonePrincipal(canonicalName: g.name, friendlyName: g.displayName, description: g.description, type: 'group', groupId: g.id)
	}

	// These instance methods support the transition of Person and Group to Principal.  Remove when transition is done.
	void updateFromPerson(Person p) {
		this.canonicalName = p.username
		this.friendlyName = p.userRealName
		this.description = p.description
		this.lastLogin = p.lastLogin
	}
}
