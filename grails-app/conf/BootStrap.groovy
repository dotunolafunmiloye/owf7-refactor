import grails.converters.JSON
import grails.util.Environment
import groovy.sql.Sql

import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// import org.ozone.metrics.JmxSslBootStrapper
// import org.ozone.metrics.hibernate.HibernateSQLCounterAppender

import ozone.owf.gorm.AuditStamp
import ozone.owf.grails.domain.Dashboard
import ozone.owf.grails.domain.EDefaultGroupNames
import ozone.owf.grails.domain.Group
import ozone.owf.grails.domain.Person
import ozone.owf.grails.domain.PersonWidgetDefinition
import ozone.owf.grails.domain.Preference
import ozone.owf.grails.domain.RelationshipType
import ozone.owf.grails.domain.WidgetDefinition
import ozone.owf.grails.domain.WidgetType

import com.yammer.metrics.CsvReporter
import com.yammer.metrics.JmxReporter
import com.yammer.metrics.jvm.BufferPoolMetricSet
import com.yammer.metrics.jvm.FileDescriptorRatioGauge
import com.yammer.metrics.jvm.GarbageCollectorMetricSet
import com.yammer.metrics.jvm.MemoryUsageGaugeSet

class BootStrap {

	def grailsApplication
	def sessionFactory
	def domainMappingService
	def metricRegistry

	def init = { servletContext ->
		println 'BootStrap running'

		if (isProductionLikeEnvironment()) {
			setupMetrics()
		}

		//configure custom marshallers
		grails.converters.JSON.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.json.ServiceModelObjectMarshaller())
		grails.converters.XML.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.xml.ServiceModelObjectMarshaller())

		// FIXME: Move this to the integration tests!
		if (Environment.current == Environment.TEST) {
			loadWidgetTypes()
		}
		createNewUser()

		// Create the default all user group if it doesn't already exist.
		def allUsers = Group.findByName(EDefaultGroupNames.GROUP_USER.strVal)
		if (allUsers == null) {
			log.info("Adding new default users group")
			allUsers = new Group(
					name: EDefaultGroupNames.GROUP_USER.strVal,
					description: EDefaultGroupNames.GROUP_USER.strVal,
					displayName: EDefaultGroupNames.GROUP_USER.strVal
					)
		}
		allUsers.status = 'active'
		allUsers.automatic = true
		allUsers.save(flush: true)

		// Same process now for the all administrator group.
		def allAdmins = Group.findByName(EDefaultGroupNames.GROUP_ADMIN.strVal)
		if (allAdmins == null) {
			log.info("Adding new default administration group")
			allAdmins = new Group(
					name: EDefaultGroupNames.GROUP_ADMIN.strVal,
					description: EDefaultGroupNames.GROUP_ADMIN.strVal,
					displayName: EDefaultGroupNames.GROUP_ADMIN.strVal
					)
		}
		allAdmins.status = 'active'
		allAdmins.automatic = true
		allAdmins.save(flush: true)

		if (isProductionLikeEnvironment()) {
			// To avoid contention when new users login, we're doing a blanket update of the
			// groups here.  It seems that adding a user to the group, which doesn't actually
			// change the data on the owf_group table, triggers an update of the group entity
			// in Hibernate, leading to a lot of contention over the (likely) OWF Users group.
			def sql = new Sql(sessionFactory.currentSession.connection())
			def groupSync = sql.executeUpdate("""
				UPDATE principal p, owf_group g SET
					p.canonical_name = g.name,
					p.friendly_name = g.display_name,
					p.description = g.description
				WHERE
					p.group_id = g.id AND
					p.type = 'group'""")

			// Make sure we have the administration widget type created before we go creating those specific widgets.
			def adminWidgetType = WidgetType.findByName(WidgetType.ADMIN)
			if (adminWidgetType == null) {
				adminWidgetType = new WidgetType(name: WidgetType.ADMIN).save(flush: true)
			}

			// Do we already have the minimally required set of administration widgets?
			def minimalAdminWidgets = ['Users', 'User Editor', 'Widgets', 'Widget Editor', 'Groups', 'Group Editor', 'Group Dashboards', 'Dashboard Editor']
			def cAdminWidgets = WidgetDefinition.createCriteria()
			def allAdminWidgets = cAdminWidgets.list {
				widgetType {
					eq 'name', WidgetType.ADMIN
				}
			}
			allAdminWidgets.each { adminWidget ->
				if (minimalAdminWidgets.contains(adminWidget.displayName)) {
					minimalAdminWidgets.remove(adminWidget.displayName)
				}
			}

			// If the list of widgets to create is non-empty, one or more of the minimal administration widgets needs to be created.
			minimalAdminWidgets.each { widgetName ->
				def widget = new WidgetDefinition(
						widgetGuid: java.util.UUID.randomUUID().toString(),
						displayName: widgetName,
						widgetVersion: '1.0',
						height: 440,
						width: 581,
						widgetType: adminWidgetType)

				switch (widgetName) {
					case 'Users':
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Users64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Users24.png'
						widget.widgetUrl = 'admin/UserManagement.gsp'
						widget.width = 818
						break

					case 'User Editor':
						widget.visible = false
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Users64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Users24.png'
						widget.widgetUrl = 'admin/UserEdit.gsp'
						break

					case 'Widgets':
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Widgets64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Widgets24.png'
						widget.widgetUrl = 'admin/WidgetManagement.gsp'
						widget.width = 818
						break

					case 'Widget Editor':
						widget.visible = false
						widget.height = 493
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Widgets64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Widgets24.png'
						widget.widgetUrl = 'admin/WidgetEdit.gsp'
						break

					case 'Groups':
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Groups64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Groups24.png'
						widget.widgetUrl = 'admin/GroupManagement.gsp'
						widget.width = 818
						break

					case 'Group Editor':
						widget.visible = false
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Groups64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Groups24.png'
						widget.widgetUrl = 'admin/GroupEdit.gsp'
						break

					case 'Group Dashboards':
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Dashboards64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Dashboards24.png'
						widget.widgetUrl = 'admin/GroupDashboardManagement.gsp'
						widget.width = 818
						break

					case 'Dashboard Editor':
						widget.visible = false
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Dashboards64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Dashboards24.png'
						widget.widgetUrl = 'admin/DashboardEdit.gsp'
						break
				}
				widget.save(flush: true)

				// Now make sure that the admin group is the owner of the widget, which ensures that admin users get the
				// widget when they log in.
				domainMappingService.createMapping(allAdmins, RelationshipType.owns, widget)
			}

			if (grailsApplication.config?.scanUsersForDuplicates?.enabled) {
				def setSeenAlready = new HashSet()
				def mpDuplicateIds = [:]
				def lstDuplicateIds = []

				// First, scan the profiles for duplicates and build a mapping of duplicate to original.
				Person.findAll().each { person ->
					def ucUsername = person.username.toUpperCase()
					if (!setSeenAlready.contains(ucUsername)) {
						setSeenAlready.add(ucUsername)
					}
					else {
						def pInteresting = Person.findAllByUsernameIlike(ucUsername, [sort: 'id', order: 'asc'])
						def pLowest
						pInteresting.eachWithIndex { dup, i ->
							if (i == 0) {
								pLowest = dup
							}
							else {
								log.warn "moving duplicate person id ${dup.id} to person id ${pLowest.id}"
								mpDuplicateIds.put(dup, pLowest)
								lstDuplicateIds.add(dup)
							}
						}
					}
				}

				// Next, walk every domain class and handle the metadata and any other foreign-keyed columns.
				if (!lstDuplicateIds.isEmpty()) {
					repointDuplicateProfiles(lstDuplicateIds, mpDuplicateIds)
				}

				Person.executeUpdate("update Person p set p.username = UCASE(p.username)")
			}

			if (servletContext.contextPath == '/owf') {
				int jmxPort = grailsApplication.config.jmxSslPort ?: 8011
				println "Starting SSL JMX on port ${jmxPort}"
				def jmxServer = new JmxSslBootStrapper()
				jmxServer.setPort(jmxPort)
				jmxServer.init()

				// Set up metric reporters
				if (grailsApplication.config.metrics?.csv?.enable ?: true) {
					def csvDir = new File("${grailsApplication.config.logDirectory}/metrics")
					if (csvDir.exists()) {
						rollCSVMetrics(csvDir)
					}
					csvDir.mkdirs()

					int csvReportingRate = grailsApplication.config.metrics?.csv?.reportingRate ?: 60
					CsvReporter.forRegistry(metricRegistry).formatFor(Locale.US).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build(csvDir).start(csvReportingRate,TimeUnit.SECONDS)
				}

				if (grailsApplication.config.metrics?.jmx?.enable ?: true) {
					JmxReporter.forRegistry(metricRegistry).registerWith(jmxServer.getMBeanServer()).build().start()
				}
			}
		}

		println 'BootStrap finished!'
	}

	private setupMetrics() {
		// Hook up some metrics
//		org.ozone.metrics.HibernateMetrics.measureAllStatistics(sessionFactory, metricRegistry)
//		org.ozone.metrics.EhcacheMetrics.measureAll(metricRegistry)
//
//		metricRegistry.register("jvm.bufferPools", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()))
//		metricRegistry.register("jvm.garbageCollection", new GarbageCollectorMetricSet())
//		metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet())
//		metricRegistry.register("jvm.fileDescriptorUsage", new FileDescriptorRatioGauge())

//		HibernateSQLCounterAppender.inject()
	}

	private void rollCSVMetrics(File f) {
		def buff = new byte[1024]
		if (f.listFiles().size() > 0) {
			def fParent = f.getParentFile()
			def rightNow = new Date().toString().replace(' ', '_').replace(':', '_')
			def psChar = File.separatorChar
			def zipFile = new File(fParent.getPath() + psChar + "Metrics${rightNow}.zip")
			def zipFileStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))

			f.listFiles().each { csvFile ->
				def iStream = new BufferedInputStream(new FileInputStream(csvFile))
				def zipEntry = new ZipEntry(csvFile.name)
				zipFileStream.putNextEntry(zipEntry)

				int len
				while ((len = iStream.read(buff)) > 0) {
					zipFileStream.write(buff, 0, len)
				}
				zipFileStream.closeEntry()
				iStream.close()
			}
			zipFileStream.close()

			f.listFiles().each {
				it.delete()
			}
		}

		f.delete()
	}

	private repointDuplicateProfiles(List lstDuplicateIds, Map mpDuplicateIds) {
		def domainClasses = grailsApplication.getArtefacts("Domain")*.clazz
		domainClasses.each { clz ->
			if (clz in [Dashboard.class, PersonWidgetDefinition.class, Preference.class]) {
				if (clz == PersonWidgetDefinition.class) {
					def results = clz.findAll("from ${clz.name} cl where cl.person in (:keys)", [keys: lstDuplicateIds])
					results.each {
						it.person = mpDuplicateIds.get(it.person)
						it.save(flush: true)
					}
				}
				else {
					def results = clz.findAll("from ${clz.name} cl where cl.user in (:keys)", [keys: lstDuplicateIds])
					results.each {
						it.user = mpDuplicateIds.get(it.user)
						it.save(flush: true)
					}
				}
			}

			def ann = clz.annotations*.annotationType()
			if (ann.contains(AuditStamp.class)) {
				def auditCreate = clz.findAll(
						"from ${clz.name} as si where si.createdBy in (:keys)", [keys: lstDuplicateIds])
				auditCreate.each {
					it.createdBy = mpDuplicateIds.get(it?.createdBy)
					it.save(flush: true)
				}
				def auditEdit = clz.findAll(
						"from ${clz.name} as si where si.editedBy in (:keys)", [keys: lstDuplicateIds])
				auditEdit.each {
					it.editedBy = mpDuplicateIds.get(it.editedBy)
					it.save(flush: true)
				}
			}
		}

		// Next, remove all the duplicate profiles
		lstDuplicateIds.each {
			it.delete(flush: true)
		}
	}

	private createNewUser() {
		def user = Person.findByUsername(Person.NEW_USER)
		if (user == null) {
			user = new Person(username: Person.NEW_USER, userRealName: Person.NEW_USER, enabled: true).save(flush: true)
		}
	}

	private loadWidgetTypes() {
		new WidgetType(name: WidgetType.STANDARD).save()
	}

	private boolean isProductionLikeEnvironment() {
		def retVal = Environment.current.name in [Environment.TEST.name,'with_beta','with_prod','with_empty','with_mine','with_mine2']
		return !retVal
	}

	def destroy = {
	}
}
