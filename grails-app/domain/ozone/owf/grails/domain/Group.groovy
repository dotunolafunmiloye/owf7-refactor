package ozone.owf.grails.domain

import grails.gorm.DetachedCriteria

import org.hibernate.proxy.HibernateProxy

public class Group implements Serializable, OzoneServiceModel {

	static String TYPE = 'group'
	static final long serialVersionUID = 700L

	String name
	String displayName
	String description = ''
	String email
	Boolean automatic = false
	String status = 'active'

	static hasMany = [people: Person]

	static mapping = {
		table 'owf_group'
		cache true
		people(lazy: true, cache: true)
	}

	static constraints = {
		name(blank: false, maxSize: 200)
		displayName(nullable: true, blank: true, maxSize: 200)
		description(nullable: true, blank: true)
		email(nullable: true, blank: true)
		status(nullable: false, blank: false, inList:['active','inactive'])
	}

	boolean equals(other) {
		if (other instanceof Group || (other instanceof HibernateProxy && other.instanceOf(Group))) {
			other?.id == id
		}
		else {
			false
		}
	}

	int hashCode() { id ?: 0 }

	public GroupServiceModel toServiceModel(Map params) {
		return new GroupServiceModel(this, params)
	}

	// Total hack. As soon as the migration of Group to OzonePrincipal is complete, everything below here should be removed.
	def afterInsert() {
		def pr = OzonePrincipal.fromGroup(this)
		pr.save()
	}

	def beforeDelete() {
		deletePrincipalsForDeletedGroup()
	}

	public void deletePrincipalsForDeletedGroup() {
		def criteria = new DetachedCriteria(OzonePrincipal).build {
			eq 'groupId', this.id
		}
		criteria.deleteAll()
	}
}

class GroupServiceModel extends AbstractServiceModel {
	Long id
	String name
	String description = ''
	String displayName
	String email
	Boolean automatic = false
	String status
	Integer totalWidgets = 0
	Integer totalUsers = 0
	List tagLinks = []

	public GroupServiceModel(Group g) {
		this(g, null)
	}

	public GroupServiceModel(Group g, Map params) {
		this.id = g.id
		this.name = g.name
		this.displayName = g.displayName
		this.description = g.description
		this.email = g.email
		this.automatic = g.automatic
		this.totalUsers = params?.totalUsers ?: 0
		this.totalWidgets = params?.totalWidgets ?: 0
		this.status = g.status
	}

	// Required to prevent the loss of the id attribute.
	Map toDataMap() {
		return [
			id: id,
			name: name,
			description: description,
			displayName: displayName,
			email: email,
			automatic: automatic,
			status: status,
			totalWidgets: totalWidgets,
			totalUsers: totalUsers,
			tagLinks: tagLinks
		]
	}
}
