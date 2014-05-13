package ozone.owf.grails.domain

import ozone.owf.grails.utils.OWFDate

import com.ocpsoft.pretty.time.PrettyTime

@ozone.owf.gorm.AuditStamp
class Dashboard implements Serializable, Comparable, OzoneServiceModel {

	static String TYPE = 'dashboard'

	//derived from version number without dots,
	//so that all domain classes with a given
	//version are considered compatible and
	//classes between versions are incompatible.
	static final long serialVersionUID = 700L

	String name					//Added for default JSON
	String guid
	boolean isdefault
	Integer dashboardPosition
	boolean alteredByAdmin
	String description = ''
	String layoutConfig = ''
	boolean locked = false
	static belongsTo = [user: Person]

	static mapping = {
		cache true
		layoutConfig sqlType: 'text' // By default, H2 (integration tests) generates VARCHAR(255); this hints H2 for something larger.
	}

	static constraints = {
		guid(nullable: false, blank: false, unique: true, matches: /^[A-Fa-f\d]{8}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{4}-[A-Fa-f\d]{12}$/)
		//This regex says does not match any of the characters \"/#={}:;,[] because they make the grails JSON parser die.
		name(nullable: false, blank: false, maxSize: 200)
		dashboardPosition(nullable: false, display: false)
		description(nullable: true, blank: true)
		user(nullable: true)
		layoutConfig(nullable: true, blank: true)
	}

	String toString() {
		this.guid
	}

	int compareTo(that) {
		this.dashboardPosition <=> that.dashboardPosition
	}

	public DashboardServiceModel toServiceModel(Map params) {
		return new DashboardServiceModel(this, params)
	}
}

class DashboardServiceModel extends AbstractServiceModel {
	static prettytime = new PrettyTime()

	String name
	String guid
	Boolean isdefault
	Integer dashboardPosition
	PersonServiceModel user
	String alteredByAdmin
	String EDashboardLayoutList = EDashboardLayout.listAsStrings()
	Boolean isGroupDashboard
	List groups = []
	String description = ''
	String createdDate
	String prettyCreatedDate
	String editedDate
	String prettyEditedDate
	PersonServiceModel createdBy
	String layoutConfig
	Boolean locked = false

	Map toDataMap() {
		Map dataMap = [:]

		this.properties.each {
			//ignore lame default class properties don't need these polluting our model
			if (it.key != 'class' && it.key != 'metaClass') {

				//todo some day fix this inconsistency
				if (it.key.equals('user')) {
					dataMap[it.key] = [userId: it.value?.username]
				}
				else if (it.key.equals('createdBy')) {
					dataMap[it.key] = [userId: it.value?.username,
								userRealName: it.value?.userRealName]
				}
				else {
					dataMap[it.key] = it.value
				}
			}
		}

		return dataMap
	}

	public DashboardServiceModel(Dashboard d) {
		this(d, null)
	}

	public DashboardServiceModel(Dashboard d, Map params) {
		def groups = []
		// Total hack here.  The front end doesn't actually need "real" groups since all it's doing is
		// counting them and reporting that to the end-user.  So, rather than actually fetch data which
		// won't be used, generate some dummy data on the fly.  Note that we *never* save these groups
		// so we don't pollute the database (or create all manner of exceptions trying).
		if (params?.groupCount && params?.groupCount >= 1) {
			(1..params.groupCount).each { it
				groups << new Group(name: "Group${it}", displayName: "Group ${it}").toServiceModel()
			}
		}

		this.name = d.name
		this.guid = d.guid
		this.isdefault = d.isdefault
		this.dashboardPosition = d.dashboardPosition
		this.locked = d.locked
		this.user = d.user?.toServiceModel()
		this.alteredByAdmin = d.alteredByAdmin ?: false
		this.isGroupDashboard = params?.isGroupDashboard ?: d.user == null
		this.groups = groups
		this.description = d.description
		this.createdDate = OWFDate.standardShortDateDisplay(d.createdDate)
		this.prettyCreatedDate = d.createdDate != null ? prettytime.format(d.createdDate) : ''
		this.editedDate = OWFDate.standardShortDateDisplay(d.editedDate)
		this.prettyEditedDate = d.editedDate != null ? prettytime.format(d.editedDate) : ''
		this.createdBy = d.createdBy?.toServiceModel() ?: d.user?.toServiceModel()
		this.layoutConfig = d.layoutConfig
	}

}
