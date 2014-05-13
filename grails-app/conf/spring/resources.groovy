import grails.util.GrailsUtil

import org.ozone.metrics.MetricsAdvice
import org.ozone.metrics.hibernate.HibernateSQLCountAdvice

import ozone.owf.grails.OwfExceptionResolver
import ozone.owf.grails.domain.ERoleAuthority
import ozone.owf.grails.services.AccountService
import ozone.owf.grails.services.AutoLoginAccountService
import ozone.owf.grails.utils.AuditTrailInterceptor

import com.yammer.metrics.MetricRegistry

beans = {

	xmlns aop:"http://www.springframework.org/schema/aop"

	entityInterceptor(AuditTrailInterceptor) {
		createdBy = 'createdBy'
		editedBy = 'editedBy'
		editedDate = 'editedDate'
		createdDate = 'createdDate'
	}
	metricRegistry(MetricRegistry) { beanDef ->
		beanDef.constructorArgs = ['OWF']
	}

	// This first path is taken during integration testing and when generating SQL files from the db migration plugin.
	if (['test','with_beta','with_prod','with_empty','with_mine','with_mine2'].contains(GrailsUtil.environment)) {
		switch (System.properties.user) {
			case "testUser1":
				println("Using AutoLoginAccountService - you will be logged in as testUser1")
				accountService(AutoLoginAccountService) {
					autoAccountName = "testUser1"
					autoAccountDisplayName = "Test User 1"
					autoRoles = [ERoleAuthority.ROLE_USER.strVal]
					serviceModelService = ref('serviceModelService')
				}
				break
			case "testAdmin1":
				println("Using AutoLoginAccountService - you will be logged in as testAdmin1")
				accountService(AutoLoginAccountService) {
					autoAccountName = "testAdmin1"
					autoAccountDisplayName = "Test Admin 1"
					autoRoles = [ERoleAuthority.ROLE_USER.strVal, ERoleAuthority.ROLE_ADMIN.strVal]
					serviceModelService = ref('serviceModelService')
				}
				break
			case "testAdmin2":
				println("Using AutoLoginAccountService - you will be logged in as testAdmin2")
				accountService(AutoLoginAccountService) {
					autoAccountName = "testAdmin2"
					autoAccountDisplayName = "Test Admin 2"
					autoRoles = [ERoleAuthority.ROLE_USER.strVal, ERoleAuthority.ROLE_ADMIN.strVal]
					serviceModelService = ref('serviceModelService')
				}
				break
			default :
				accountService(AccountService) {
					serviceModelService = ref('serviceModelService')
				}
				break
		}
	} else {
		importBeans "classpath*:resources/ListenerBeans.xml"
		importBeans "classpath*:resources/owf/OWFLogInOutBeans.xml"
		importBeans "classpath*:resources/owf/OWFsecurityContext.xml"

		metricsAdvice(MetricsAdvice)
		hibernateSqlAdvice(HibernateSQLCountAdvice)

		aop {
			config("proxy-target-class": true) {
				aspect(id: 'metricsGatheringAspect', ref: 'metricsAdvice') {
					around method: 'logMetrics',
							pointcut: '(execution(* ozone..*Controller.*(..)) or execution(* ozone..*Service.*(..))) and !execution(* *.getMetaClass(..))'
				}

				aspect(id: 'hibernateSqlAspect', ref: 'hibernateSqlAdvice') {
					around method: 'logMetrics',
							pointcut: '(execution(* ozone..*Controller.*(..)) or execution(* ozone..*Service.*(..))) and !execution(* *.getMetaClass(..))'
				}
			}
		}

		// These two items are absolutely required when loading the XML files
		// via the BeanBuilder.  If you forget the first one, Spring complains
		// about a missing authenticationManager and you don't get to the
		// BootStrap code.  If you forget the second one, you get all the way
		// through the BootStrap and die when the Ozone Deployment plugin tries
		// to inject various filters into the live application context (add to
		// web.xml).
		springConfig.addAlias 'authenticationManager', 'org.springframework.security.authenticationManager'
		springConfig.addAlias 'springSecurityFilterChain', 'org.springframework.security.filterChainProxy'
	}

	exceptionHandler(OwfExceptionResolver) {
		exceptionMappings = [
					'java.lang.Exception': '/error'
				]
	}
}