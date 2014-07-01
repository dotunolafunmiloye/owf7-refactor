package ozone.owf.grails.services

import org.grails.taggable.TagLink

import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.Stack
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType
import ozone.owf.grails.services.model.DashboardServiceModel
import ozone.owf.grails.services.model.GroupServiceModel
import ozone.owf.grails.services.model.PersonServiceModel
import ozone.owf.grails.services.model.PersonWidgetDefinitionServiceModel
import ozone.owf.grails.services.model.PreferenceServiceModel
import ozone.owf.grails.services.model.ServiceModel
import ozone.owf.grails.services.model.StackServiceModel
import ozone.owf.grails.services.model.TagLinkServiceModel
import ozone.owf.grails.services.model.WidgetDefinitionServiceModel
import ozone.owf.grails.services.model.WidgetTypeServiceModel
import ozone.owf.grails.util.OWFDate

import com.ocpsoft.pretty.time.PrettyTime

/**
 *
 * @author ntabernero
 * @version $Id: $
 * @since May 27, 2010 3:55:50 PM
 */
class ServiceModelService {

    def grailsApplication

    public ServiceModel createServiceModel(obj, params = [:]) {
        ServiceModel model
        Class clazz = obj?.class

        switch (clazz) {

            case Dashboard:
                Dashboard domain = (Dashboard) obj
                def prettytime = new PrettyTime()
                model = new DashboardServiceModel(
                        name: domain.name,
                        guid: domain.guid,
                        isdefault: domain.isdefault,
                        dashboardPosition: domain.dashboardPosition,
                        locked: domain.locked,
                        user: createServiceModel(domain.user),
                        alteredByAdmin: domain.alteredByAdmin ?: false,
                        isGroupDashboard: params.isGroupDashboard ?: false,
                        groups: params.groups != null ? params.groups.collect{ createServiceModel(it) } : [],
                        description: domain.description,
                        createdDate: OWFDate.standardShortDateDisplay(domain.createdDate),
                        prettyCreatedDate: domain.createdDate != null ? prettytime.format(domain.createdDate) : '',
                        editedDate: OWFDate.standardShortDateDisplay(domain.editedDate),
                        prettyEditedDate: domain.editedDate != null ? prettytime.format(domain.editedDate) : '',
                        createdBy: createServiceModel(domain.createdBy),
                        layoutConfig: domain.layoutConfig,
                        stack: createServiceModel(domain.stack)
                        )
                break

            case Person:
                Person domain = (Person) obj
                model = new PersonServiceModel(
                        id: domain.id,
                        username: domain.username,
                        userRealName: domain.userRealName,
                        email: domain.email ?: '',
                        lastLogin: domain.lastLogin ? domain.lastLogin.getTime() : null,
						totalWidgets: params.totalWidgets ? params.totalWidgets : 0,
						totalGroups: params.totalGroups ? params.totalGroups: 0,
						totalDashboards: params.totalDashboards ? params.totalDashboards: 0,
						totalStacks: params.totalStacks ?: 0
						)
                break

            case PersonWidgetDefinition:
                PersonWidgetDefinition domain = (PersonWidgetDefinition) obj
				WidgetDefinition parentDomain = domain.widgetDefinition
				def tags
				if (params.tagLinks) {
					tags = params.tagLinks.collect { createServiceModel(it) }
				} else {
					// We want unique tags, but we want not just the user-defined tags but also whatever
					// has been set for the widget globally.
					tags = domain.getTags().collect(parentDomain.getTags().collect {createServiceModel(it) }) { createServiceModel(it) }
					//def tmpTags = new HashSet()
					//tmpTags.addAll(parentDomain.getTags().collect { createServiceModel(it) })
					//tmpTags.addAll(domain.getTags().collect { createServiceModel(it) })
					//tags = tmpTags
				}
                model = new PersonWidgetDefinitionServiceModel(
                        id: domain.id,
                        person: createServiceModel(domain.person),
                        widgetDefinition: createServiceModel(domain.widgetDefinition),
                        pwdPosition: domain.pwdPosition,
                        groupWidget: domain.groupWidget,
                        favorite: domain.favorite,
                        visible: domain.visible,
                        displayName: domain.displayName,
                        disabled: domain.disabled,
                        groups: params.groups != null ? params.groups.collect{ createServiceModel(it) } : [],
                        editable: params.editable != null ? params.editable : true,
						tagLinks: tags
                        );
                break

            case Group:
                Group domain = (Group) obj
                model = new GroupServiceModel(
                        id: domain.id,
                        name: domain.name,
                        displayName: domain.displayName,
                        description: domain.description,
                        email: domain.email,
                        automatic: domain.automatic,
                        stackDefault: domain.stackDefault,
                        totalUsers: params.totalUsers ? params.totalUsers : 0,
                        totalWidgets: params.totalWidgets ? params.totalWidgets : 0,
                        totalStacks: params.totalStacks ? params.totalStacks : 0,
                        status: domain.status
                        )
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

                //lazy initializtion of reference to WidgetDefinitionService.
                //We can't use injection because of the circular dependency between
                //the two services
                def widgetDefinitionServiceBean =
                        grailsApplication.mainContext.getBean('widgetDefinitionService')

                model = new WidgetDefinitionServiceModel(
                        id: domain.widgetGuid,
                        widgetGuid: domain.widgetGuid,
                        universalName: domain.universalName,
                        displayName: domain.displayName,
                        description: domain.description,
                        widgetUrl: domain.widgetUrl,
                        imageUrlSmall: domain.imageUrlSmall,
                        imageUrlLarge: domain.imageUrlLarge,
                        width: domain.width,
                        height: domain.height,
                        totalUsers: params.totalUsers ? params.totalUsers : 0,
                        totalGroups: params.totalGroups ? params.totalGroups : 0,
                        widgetVersion: domain.widgetVersion,
                        singleton: domain.singleton?true:false,
                        visible: domain.visible,
                        background: domain.background,
                        descriptorUrl: domain.descriptorUrl,
                        directRequired: params.directRequired ? params.directRequired : widgetDefinitionServiceBean.getDirectRequiredIds(domain),
                        allRequired:  params.allRequired ? params.allRequired : widgetDefinitionServiceBean.getAllRequiredIds(domain),
                        tagLinks: params.tagLinks ? params.tagLinks.collect { createServiceModel(it) } : domain.getTags().collect { createServiceModel(it) },
                        intents: domain.widgetDefinitionIntents ?: [],
                        widgetTypes: domain.widgetTypes.collect{ createServiceModel(it) }
                        );
                break

            case TagLink:
                TagLink tagLink = (TagLink) obj
                model = new TagLinkServiceModel(
                        name: tagLink.tag.name,
                        visible: tagLink.visible,
                        position: tagLink.position,
                        editable: tagLink.editable
                        );
                break
            case WidgetType:
                WidgetType widgetType = (WidgetType) obj
                model = new WidgetTypeServiceModel(
                        id: widgetType.id,
                        name: widgetType.name
                        )
                break
            case Stack:
                ozone.owf.grails.domain.Stack domain = (ozone.owf.grails.domain.Stack) obj

                // add groups to stack so we can decide client side whether to allow
                // users to delete this stack
                def groups = []
                domain.groups?.each {
                    groups << createServiceModel(it)
                }

                model = new StackServiceModel(
                        id: domain.id,
                        name: domain.name,
                        description: domain.description,
                        stackContext: domain.stackContext,
                        imageUrl: domain.imageUrl,
                        descriptorUrl: domain.descriptorUrl,
                        groups: groups,
                        totalDashboards: params.totalDashboards ?: 0,
                        totalUsers: params.totalUsers ?: 0,
                        totalGroups: params.totalGroups ?: 0,
                        totalWidgets: domain.uniqueWidgetCount ?: 0
                        )
                break
            default:
                model = null
                break
        }


        return model
    }
}
