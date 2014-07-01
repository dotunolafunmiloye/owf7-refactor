package ozone.owf.grails.services.model

class StackServiceModel extends AbstractServiceModel {
    Long id
    String name
    String description
    String stackContext
    String imageUrl
    String descriptorUrl
    List groups
    Integer totalDashboards = 0
    Integer totalUsers = 0
    Integer totalGroups = 0
    Integer totalWidgets = 0
}

