package ozone.owf.grails.utils

import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.springframework.web.context.request.RequestContextHolder as RCH

import ozone.owf.grails.domain.Person

class AuditTrailInterceptor extends EmptyInterceptor {

	def createdBy
	def editedBy
	def editedDate
	def createdDate

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,Object[] previousState, String[] propertyNames,Type[] types) {
		def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, editedDate)
		List fieldList = propertyNames.toList()

		if (property) {
			def now = property.getType().newInstance([System.currentTimeMillis()] as Object[])
			setValue(currentState, fieldList, editedDate, now)
		}
		property = metaClass.hasProperty(entity, editedBy)
		if (property) {
			setValue(currentState, fieldList, editedBy, getUserID())
		}

		return true
	}

	boolean onSave(Object entity, Serializable id, Object[] state,String[] propertyNames, Type[] types) {
		def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, createdDate)
		def time = System.currentTimeMillis()
		List fieldList = propertyNames.toList()
		Object userId = getUserID()

		if (property) {
			def now = property.getType().newInstance([time] as Object[])
			setValue(state, fieldList, createdDate, now)
		}
		property = metaClass.hasProperty(entity, editedDate)
		if (property) {
			def now = property.getType().newInstance([time] as Object[])
			setValue(state, fieldList, editedDate, now)
		}
		property = metaClass.hasProperty(entity, editedBy)
		if (property) {
			setValue(state, fieldList, editedBy, userId)
		}
		property = metaClass.hasProperty(entity, createdBy)
		if (property) {
			setValue(state, fieldList, createdBy, userId)
		}
		return true
	}

	def setValue(Object[] currentState, List fieldList, String propertyToSet, Object value) {
		int index = fieldList.indexOf(propertyToSet)
		if (index >= 0) {
			currentState[index] = value
		}
	}

	Object getUserID() {
		Long returnValue
		if (RCH?.getRequestAttributes()?.getSession()?.personID != null) {
			returnValue = RCH?.getRequestAttributes()?.getSession()?.personID
		}
		if (returnValue)
			return Person.get(returnValue)
		else
			return null
	}
}
