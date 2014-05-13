package ozone.owf.grails.domain

/**
 * Meant to constrain the domain model classes which generate augmented
 * representations of themselves for consumption by the browser.
 *
 * This should go away eventually (when all the service model classes are
 * moved) and be merged into ServiceModel.
 */
interface OzoneServiceModel {
	AbstractServiceModel toServiceModel(Map params)
}
