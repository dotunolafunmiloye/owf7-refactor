package integration.ozone.owf.grails.conf

import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration
import org.hibernate.cfg.Configuration
import org.hibernate.tool.hbm2ddl.SchemaExport
import org.junit.Before

class DataClearingTestCase extends GroovyTestCase {

	// Hack based on suggestion from Burt Beckwith regarding forcing Grails to drop a database between test runs.
	// Some of the code that runs in these tests ends up committing a durable transaction, thus negating the
	// typical Grails transaction rollback at test-end.  And polluting subsequent tests with spurious data.
	private static Configuration _configuration
	def transactional = false
	def grailsApplication
	def dataSource
	def sessionFactory

	@Before
	public void setUp() throws Exception {
		super.setUp()

		// Continuation of above-described hack.
		if (!_configuration) {
			// 1-time creation of the configuration
			Properties properties = new Properties()
			properties.setProperty 'hibernate.connection.driver_class', dataSource.targetDataSource.driverClassName
			properties.setProperty 'hibernate.connection.username', dataSource.targetDataSource.username
			properties.setProperty 'hibernate.connection.password', dataSource.targetDataSource.password
			properties.setProperty 'hibernate.connection.url', dataSource.targetDataSource.url
			properties.setProperty 'hibernate.dialect', sessionFactory.sqlFunctionRegistry.dialect.class.name

			_configuration = new DefaultGrailsDomainConfiguration(
					grailsApplication: grailsApplication,
					properties: properties)
		}

		// The "magic" portion of the hack is here.
		new SchemaExport(_configuration).create(false, true)
		sessionFactory.currentSession.clear()
	}

}
