package ozone.owf.grails.services

import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.OzonePrincipal
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.model.PersonWidgetDefinitionServiceModel
import ozone.owf.grails.services.model.PreferenceServiceModel
import ozone.owf.grails.services.model.ServiceModel
import ozone.owf.grails.services.model.WidgetDefinitionServiceModel
import ozone.owf.grails.services.model.WidgetTypeServiceModel

/**
 *
 * @author ntabernero
 * @version $Id: $
 * @since May 27, 2010 3:55:50 PM
 */
class ServiceModelService {

	def grailsApplication
	def widgetDefinitionServiceBean

	public ServiceModel createServiceModel(obj, params = [:]) {
		ServiceModel model
		Class clazz = obj?.class

		switch (clazz) {

			case Dashboard:
				Dashboard domain = (Dashboard) obj
				model = domain.toServiceModel(params)
				break

			case OzonePrincipal:
				OzonePrincipal domain = (OzonePrincipal) obj
				if (domain.type.toLowerCase() == 'user') {
					model = new Person(id: domain.id, username: domain.canonicalName,
							userRealName: domain.friendlyName, email: '',
							lastLogin: domain.lastLogin ? domain.lastLogin.getTime() : null).toServiceModel(params)
				}
				else {
					model = new Group(id: domain.id, name: domain.canonicalName,
							displayName: domain.friendlyName, description: domain.description, email: '', automatic: true,
							status: 'active').toServiceModel(params)
				}
				break

			case Person:
				Person domain = (Person) obj
				model = domain.toServiceModel(params)
				break

			case PersonWidgetDefinition:
				PersonWidgetDefinition domain = (PersonWidgetDefinition) obj
				WidgetDefinition parentDomain = domain.widgetDefinition
				model = new PersonWidgetDefinitionServiceModel(
						id: domain.id,
						person: createServiceModel(domain.person),
						widgetDefinition: createServiceModel(domain.widgetDefinition,[localImages:true]),
						pwdPosition: domain.pwdPosition,
						groupWidget: domain.groupWidget,
						favorite: domain.favorite,
						visible: domain.visible,
						displayName: domain.displayName,
						disabled: domain.disabled,
						groups: params.groups != null ? params.groups.collect{ createServiceModel(it) } : [],
						editable: params.editable != null ? params.editable : true,
						tagLinks: []
						)
				break

			case Group:
				Group domain = (Group) obj
				model = domain.toServiceModel(params)
				break

			case Preference:
				Preference domain = (Preference) obj
				model = new PreferenceServiceModel(
						id: domain.id,
						namespace: domain.namespace,
						path: domain.path,
						value: domain.value,
						user: createServiceModel(domain.user)
						)
				break

			case WidgetDefinition:
				WidgetDefinition domain = (WidgetDefinition) obj

				if (widgetDefinitionServiceBean == null) {
					widgetDefinitionServiceBean = grailsApplication.mainContext.getBean('widgetDefinitionService')
				}

				def isMarketplace = (domain.widgetType?.name)?.equals(WidgetType.MARKETPLACE)
				def guids = []
				guids.add(domain.widgetGuid)

				model = new WidgetDefinitionServiceModel(
						id: domain.widgetGuid,
						widgetGuid: domain.widgetGuid,
						universalName: domain.universalName,
						displayName: domain.displayName,
						description: domain.description,
						widgetUrl: domain.widgetUrl,
						imageUrlSmall: (!isMarketplace && params.localImages ? "widget/${domain.widgetGuid}/image/imageUrlSmall" : domain.imageUrlSmall),
						imageUrlLarge: (!isMarketplace && params.localImages ? "widget/${domain.widgetGuid}/image/imageUrlLarge" : domain.imageUrlLarge),
						width: domain.width,
						height: domain.height,
						totalUsers: params.totalUsers ?: 0,
						totalGroups: params.totalGroups ?: 0,
						widgetVersion: domain.widgetVersion,
						singleton: domain.singleton ? true : false,
						visible: domain.visible,
						background: domain.background,
						descriptorUrl: domain.descriptorUrl,
						directRequired: params.directRequired ?: widgetDefinitionServiceBean.getRequirements(guids),
						allRequired:  params.allRequired ?: widgetDefinitionServiceBean.getRequirements(guids, true),
						tagLinks: [],
						widgetTypes: [createServiceModel(domain.widgetType)]
						)
				break

			case WidgetType:
				WidgetType widgetType = (WidgetType) obj
				model = new WidgetTypeServiceModel(
						id: widgetType.id,
						name: widgetType.name
						)
				break

			default:
				model = null
				break
		}


		return model
	}
}
