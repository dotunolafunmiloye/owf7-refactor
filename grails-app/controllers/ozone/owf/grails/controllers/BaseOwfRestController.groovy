package ozone.owf.grails.controllers

import grails.converters.JSON
import grails.validation.ValidationException

import java.lang.reflect.UndeclaredThrowableException

import ozone.owf.grails.OwfException

class BaseOwfRestController {

	def serviceModelService

	protected getJsonResult(result, targetProperty, params) {
		def gotFromTargetProperty = result.get(targetProperty)
		if (params.isExtAjaxFormat != null && params.isExtAjaxFormat == 'true') {
			return [success:result.success, data: ( (gotFromTargetProperty) ? serviceModelService.createServiceModel(gotFromTargetProperty) : result)] as JSON
		}
		else {
			return (gotFromTargetProperty) ? serviceModelService.createServiceModel(gotFromTargetProperty) as JSON : result as JSON
		}
	}

	protected def handleError(grails.validation.ValidationException ve) {
		return [msg: getFieldErrorsAsJSON(ve.errors), status: 500]
	}

	protected def handleError(UndeclaredThrowableException e) {
		return handleError(e.cause)
	}

	protected def handleError(Exception e) {
		log.error(e,e)
		def mpJson = [success: false, errorMsg: e.message] as JSON
		return [msg: mpJson.toString()?.encodeAsHTML(), status: 500]
	}

	protected def handleError(OwfException owe) {
		if ('INFO' == owe.logLevel) {
			log.info(owe)
		}
		else if ('DEBUG' == owe.logLevel) {
			log.debug(owe)
		}
		else {
			log.error(owe, owe)
		}

		owe.setMessage(owe.message?.encodeAsHTML())
		def mpJson = [success: false, errorMsg: "${owe.exceptionType.generalMessage} ${owe.message}"] as JSON
		return [msg: mpJson.toString(), status: owe.exceptionType.normalReturnCode]
	}

	protected renderResult(Map res) {
		response.status = res.status
		if (isWindowname()) {
			render(view: '/show-windowname', model: [value: res.msg, status: res.status])
		}
		else {
			render res.msg
		}
	}

	protected renderResult(Object result, int statusCode) {
		response.status = statusCode

		if (result instanceof GString || result instanceof String) {
			result = '"' + result.replaceAll(/"/, /\\"/) + '"'
		}

		if (isWindowname()) {
			render(view: '/show-windowname', model: [value: result, status: statusCode])
		}
		else {
			render result
		}
	}

	// test if the window name transport is being used
	protected isWindowname() {
		return (params['windowname'] == 'true')
	}

	protected def getFieldErrorsAsJSON(errs) {
		if (!errs) return ''

		def arrErrors = []
		errs.each { error ->
			def fe = error.getFieldError()
			def arguments = Arrays.asList(fe.getArguments())

			def mpMsg = [code: fe.getCode(), args: arguments, 'default': fe.getDefaultMessage()]
			def msg = [id: fe.getField(), msg: mpMsg]

			arrErrors.add(msg)
		}

		def fJson = [success: false, errorMsg: 'Field  Validation error!', errors: arrErrors] as JSON
		fJson.toString()
	}
}
