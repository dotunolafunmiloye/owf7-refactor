package ozone.owf.grails.controllers

import grails.converters.JSON
import ozone.owf.grails.OwfException

class MetricController extends BaseOwfRestController {

	def metricService
	def modelName = 'metric'

	def create = {
		def result = metricService.create(params)
		def statusCode = 200
		def jsonResult = result as JSON

		renderResult(jsonResult, statusCode)
	}
}
