package ozone.owf.grails.domain

class DomainMapping {

	static String TYPE = 'domain_mapping'
	static final long serialVersionUID = 700L

	static mapping = { cache true }

	static constraints = {
		srcType(nullable: false, blank: false)
		relationshipType(nullable: true, blank: true, inList: RelationshipType.listAsStrings())
		destType(nullable: false, blank: false)
	}

	//source domain obj
	Long srcId
	String srcType

	String relationshipType = RelationshipType.owns.toString()

	//dest obj
	Long destId
	String destType

}
