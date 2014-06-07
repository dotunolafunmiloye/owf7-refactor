package ozone.owf.grails.domain

/**
 * Houses the names of the default OWF groups for consistent reference.
 */
public enum EDefaultGroupNames {
	GROUP_USER("OWF Users"),
	GROUP_ADMIN("OWF Administrators")

	def strVal

	public EDefaultGroupNames(strVal) {
		this.strVal = strVal
	}

	static list() {
		[GROUP_USER,GROUP_ADMIN]
	}

	static listAsStrings() {
		list().collect{it.strVal}
	}

	static getByStringValue(toCheck) {
		list().each {
			if (toCheck == it.strVal)
				return it
		}
		return null
	}
}
