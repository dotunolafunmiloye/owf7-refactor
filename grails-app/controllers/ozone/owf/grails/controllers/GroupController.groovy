package ozone.owf.grails.controllers

import ozone.owf.grails.OwfException
import grails.converters.JSON

class GroupController extends BaseOwfRestController {

  def groupService

  def show = {
	def statusCode
	def jsonResult

	  try {
		def result = groupService.show(params)
		statusCode = 200
		jsonResult = result as JSON
	}
	catch (OwfException owe) {
		handleError(owe)
		statusCode = owe.exceptionType.normalReturnCode
		jsonResult = "Error during show: " + owe.exceptionType.generalMessage + " " + owe.message
	}

	renderResult(jsonResult, statusCode)


  }

  def list = {
	  def statusCode
	  def jsonResult


	  try {
		  def result = groupService.list(params)
		  statusCode = 200
		  jsonResult = result as JSON
	  }
	  catch (OwfException owe) {
		  handleError(owe)
		  statusCode = owe.exceptionType.normalReturnCode
		  jsonResult = "Error during list: " + owe.exceptionType.generalMessage + " " + owe.message
	  }

	  renderResult(jsonResult, statusCode)


  }

  def createOrUpdate = {
	def jsonResult

	try {
		def result = groupService.createOrUpdate(params)
		jsonResult = [msg: result as JSON, status: 200 ]
	}
	catch (Exception e) {
		jsonResult = handleError(e)
	}

	renderResult(jsonResult)


  }

  def delete = {
	def jsonResult

	try {
		def result = groupService.delete(params)
		jsonResult = [msg: result as JSON, status: 200]
	}
	catch (Exception e) {
		jsonResult = handleError(e)
	}

	renderResult(jsonResult)

  }

  def copyDashboard = {
	def jsonResult

   
	try {
		def result = groupService.copyDashboard(params)
		jsonResult = [msg: result as JSON, status: 200]
	}
	catch (Exception e) {
		jsonResult = handleError(e)
	}

	renderResult(jsonResult)

  }
}
