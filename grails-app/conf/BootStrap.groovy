import grails.converters.JSON
import grails.util.GrailsUtil

import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.OpenOption
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import org.apache.commons.lang.StringUtils
import org.apache.log4j.helpers.*
import org.apache.log4j.xml.*
import org.ozone.metrics.hibernate.HibernateSQLCounterAppender;

import ozone.owf.gorm.AuditStamp
import ozone.owf.grails.domain.*

import com.yammer.metrics.*
import com.yammer.metrics.core.*
import com.yammer.metrics.jvm.*
import com.yammer.metrics.reporting.*

class BootStrap {

	def grailsApplication
	def sessionFactory
	def domainMappingService
	MetricRegistry metricRegistry

	def init = {servletContext ->

		println 'BootStrap running'
		setupMetrics()
		
		//configure custom marshallers
		grails.converters.JSON.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.json.ServiceModelObjectMarshaller())
		grails.converters.deep.JSON.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.json.ServiceModelObjectMarshaller())

		grails.converters.XML.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.xml.ServiceModelObjectMarshaller())
		grails.converters.deep.XML.registerObjectMarshaller(new ozone.owf.grails.web.converters.marshaller.xml.ServiceModelObjectMarshaller())

		// Logging setup.
		if (!grailsApplication.config.log4j) {
			URL url = Loader.getResource('owf-override-log4j.xml')
			if (url) {
				println "Using log configuration at: ${url.toString()}"
				DOMConfigurator.configure(url)
			}
		}

		switch (GrailsUtil.environment) {
			case 'test':
				loadWidgetTypes()
				break
			case ["development", "testUser1", "testAdmin1"]:
				log.info('Loading development fixture data')
				createNewUser()
				loadDevelopmentData()
				break
			case 'production':
				log.info('Adding default newUser')
				createNewUser()
				break;
			default:
				break
		}

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

		// Make sure we have the administration widget type created before we
		// go creating those specific widgets.
		if (GrailsUtil.environment != 'test') {
			def adminWidgetType = WidgetType.findByName(WidgetType.ADMIN)
			if (adminWidgetType == null) {
				adminWidgetType = new WidgetType(name: WidgetType.ADMIN).save(flush: true)
			}

			// Do we already have the minimally required set of administration widgets?
			def minimalAdminWidgets = ['Users','User Editor','Widgets','Widget Editor','Groups','Group Editor','Group Dashboards','Dashboard Editor','Stacks','Stack Editor']
			def cAdminWidgets = WidgetDefinition.createCriteria()
			def allAdminWidgets = cAdminWidgets.list {
				widgetTypes {
					eq('name', WidgetType.ADMIN)
				}
			}
			allAdminWidgets.each { adminWidget ->
				if (minimalAdminWidgets.contains(adminWidget.displayName)) {
					minimalAdminWidgets.remove(adminWidget.displayName)
				}
			}

			// If the list of widgets to create is non-empty, one or more of the
			// minimal administration widgets needs to be created.
			minimalAdminWidgets.each { widgetName ->
				def widget = new WidgetDefinition(
						widgetGuid: java.util.UUID.randomUUID().toString(),
						displayName: widgetName,
						widgetVersion: '1.0',
						height: 440,
						width: 581)

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

					case 'Stacks':
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Stacks64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Stacks24.png'
						widget.widgetUrl = 'admin/StackManagement.gsp'
						widget.width = 818
						break

					case 'Stack Editor':
						widget.visible = false
						widget.imageUrlLarge = 'themes/common/images/adm-tools/Stacks64.png'
						widget.imageUrlSmall = 'themes/common/images/adm-tools/Stacks24.png'
						widget.widgetUrl = 'admin/StackEdit.gsp'
						break
				}

				widget.addToWidgetTypes(adminWidgetType)
				widget.save(flush: true)
				widget.addTag('admin')

				// Now make sure that the admin group is the owner of the widget, which
				// ensures that admin users get the widget when they log in.
				domainMappingService.createMapping(allAdmins, RelationshipType.owns, widget);
			}
		}


		if (grailsApplication.config?.scanUsersForDuplicates?.enabled) {
			def setSeenAlready = new HashSet()
			def mpDuplicateIds = [:]
			def lstDuplicateIds = []

			// First, scan the profiles for duplicates and build a mapping of
			// duplicate to original.
			Person.findAll().each { person ->
				def ucUsername = person.username.toUpperCase()
				if (!setSeenAlready.contains(ucUsername)) {
					setSeenAlready.add(ucUsername)
				} else {
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

			// Next, walk every domain class and handle the metadata and any
			// other foreign-keyed columns.
			if (!lstDuplicateIds.isEmpty()) {
				repointDuplicateProfiles(lstDuplicateIds, mpDuplicateIds)
			}

			Person.executeUpdate("update Person p set p.username = UCASE(p.username)")
		}

		int jmxPort = grailsApplication.config.jmxSslPort ?: 8011
		println "Starting SSL JMX on port ${jmxPort}"
		def jmxServer=new ozone.owf.JmxSslBootStrapper();
		jmxServer.setPort(jmxPort)
		jmxServer.init()

		// Set up metric reporters
		if(grailsApplication.config.metrics?.csv?.enable?:true) {
			def csvDir = new File("${grailsApplication.config.logDirectory}/metrics")
			if (csvDir.exists()) {
				rollCSVMetrics(csvDir);
			}
			csvDir.mkdirs()
			
			int csvReportingRate=grailsApplication.config.metrics?.csv?.reportingRate?:60;
			CsvReporter.forRegistry(metricRegistry)
					.formatFor(Locale.US)
					.convertRatesTo(TimeUnit.SECONDS)
					.convertDurationsTo(TimeUnit.MILLISECONDS)
					.build(csvDir)
					.start(csvReportingRate,TimeUnit.SECONDS);
		}
		
		if(grailsApplication.config.metrics?.jmx?.enable?:true) {
			JmxReporter.forRegistry(metricRegistry)
					.registerWith(jmxServer.getMBeanServer())
					.build()
					.start();
		}

		println 'BootStrap finished!'
	}

	private setupMetrics() {
		// Hook up some metrics
		org.ozone.metrics.HibernateMetrics.measureAllStatistics(sessionFactory,metricRegistry)
		org.ozone.metrics.EhcacheMetrics.measureAll(metricRegistry)

		metricRegistry.register("jvm.bufferPools",new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()))
		metricRegistry.register("jvm.garbageCollection",new GarbageCollectorMetricSet())
		metricRegistry.register("jvm.memory",new MemoryUsageGaugeSet())
		metricRegistry.register("jvm.fileDescriptorUsage",new FileDescriptorRatioGauge())
		
		HibernateSQLCounterAppender.inject();
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
				} else {
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
					it.createdBy = mpDuplicateIds.get(it.createdBy)
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

	def destroy = {
	}

	private def loadDevelopmentData() {
		if (grailsApplication.config?.perfTest?.enabled) {
			println "	Loading development data..."
			def numWidgets = grailsApplication.config.perfTest?.numWidgets?: 0
			def numWidgetsPerUser = grailsApplication.config.perfTest?.numWidgetsPerUser?: 0
			def numAdmins = grailsApplication.config.perfTest?.numAdmins?: 1
			def numUsers = grailsApplication.config.perfTest?.numUsers?: 2
			def numGroups = grailsApplication.config.perfTest?.numGroups?: 2
			def numGroupsPerUser = grailsApplication.config.perfTest?.numGroupsPerUser?: 2
			def numWidgetsInGroups = grailsApplication.config.perfTest?.numWidgetsInGroups?: 2
			def numDashboards = grailsApplication.config.perfTest?.numDashboards?: 0
			def numGroupDashboards = grailsApplication.config.perfTest?.numGroupDashboards?: 0
			def numDashboardsWidgets = grailsApplication.config.perfTest?.numDashboardsWidgets?: 0
			def numStacks = grailsApplication.config.perfTest?.numStacks?: 0
			def numStacksPerUser = grailsApplication.config.perfTest?.numStacksPerUser?: 0
			def numStackDashboards = grailsApplication.config.perfTest?.numStackDashboards?: 0
			def numPreferences = grailsApplication.config.perfTest?.numPreferences?: 2
			def clearCacheEvery = grailsApplication.config.perfTest?.clearCacheEvery?: 10
			def createSampleWidgets = grailsApplication.config.perfTest?.createSampleWidgets?: false
			def sampleWidgetBaseUrl = grailsApplication.config.perfTest?.sampleWidgetBaseUrl?: 'https://127.0.0.1:8443/'

			def widgetDefinitionIds = [:]
			def personWidgetMapping = [:]
			def stkRecordIds = []
			def grpRecordIds = []
			def adminRecordIds = []
			def userRecordIds = []
			def maxPersonId = 0
			def maxDomainMapping = 0
			def maxDashboard = 0

			def path = Paths.get("C:\\LocalApps\\datadump\\dataLoad_${numAdmins}admins_${numUsers}users.sql")
			def file = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset(), [] as OpenOption[]));

			file.println("-- Basic setup")
			file.println("INSERT INTO `owf_group` VALUES (1,0,'active',NULL,'OWF Users','OWF Users',1,'OWF Users',0);")
			file.println("INSERT INTO `owf_group` VALUES (2,0,'active',NULL,'OWF Administrators','OWF Administrators',1,'OWF Administrators',0);")
			file.println("INSERT INTO `person` VALUES (1,0,1,'DEFAULT_USER','DEFAULT_USER','2013-05-09 14:27:49',0,NULL,NULL,NULL);")
			file.println("INSERT INTO `widget_type` VALUES (1,0,'standard'),(2,0,'administration'),(3,0,'marketplace'),(4,0,'metric');")
			file.println("INSERT INTO `requestmap` VALUES (1,0,'/login/**','IS_AUTHENTICATED_ANONYMOUSLY'),(2,0,'/denied.jsp','IS_AUTHENTICATED_ANONYMOUSLY'),(3,0,'/**','IS_AUTHENTICATED_FULLY');")
			file.println("INSERT INTO `role` VALUES (1,1,'ROLE_USER','User Role'),(2,1,'ROLE_ADMIN','Admin Role');")
			file.println("")
			file.flush()

			grpRecordIds.add(1)
			grpRecordIds.add(2)
			maxPersonId++

			def rand = new Random()

			private def generateId = {
				UUID.randomUUID().toString()
			}

			private def assignGroupDashboards = {
				println "		Creating group/stack dashboards..."
				def users = []
				users.addAll(adminRecordIds)
				users.addAll(userRecordIds)

				def dValues = []
				def dmValues = []
				def widgetIds = widgetDefinitionIds.keySet() as List

				def createTheDashboards = { grps, type ->
					def ownedWidgets = []

					grps.each { group ->
						// For each group, generate the requisite number of group
						// dashboards

						def dbRange = 1..numGroupDashboards
						if (type == 'Stack') {
							dbRange = 1..numStackDashboards
						}
						dbRange.each { i ->
							def dashboardGuid = generateId()
							def paneGuid = generateId()

							def widgets2 = []

							def dbWidgetRange = 1..numDashboardsWidgets
							dbWidgetRange.eachWithIndex { dbWIndex, idx ->
								def xyPos = 300 + (dbWIndex * 5)

								def randomWidgetId = widgetIds[rand.nextInt(widgetIds.size())]
								def randomWidget = widgetDefinitionIds.get(randomWidgetId)
								while (!randomWidget) {
									randomWidgetId = widgetIds[rand.nextInt(widgetIds.size())]
									randomWidget = widgetDefinitionIds.get(randomWidgetId)
								}
								def dbWidgetRandomGuid = generateId()

								def singleWidget = [
											widgetGuid: randomWidget.guid,
											x: xyPos,
											y: xyPos,
											uniqueId: dbWidgetRandomGuid,
											name: randomWidget.name,
											paneGuid: paneGuid,
											height: 250,
											width: 250,
											dashboardGuid: dashboardGuid
										]
								widgets2.add(singleWidget)

								if (!ownedWidgets.contains(randomWidgetId)) {
									maxDomainMapping++
									dmValues.add("(${maxDomainMapping}, 0, ${group}, 'group', 'owns', ${randomWidgetId}, 'widget')")
									ownedWidgets.add(randomWidgetId)
								}
							}

							def layoutConfig = [
										xtype: 'desktoppane',
										flex: 1,
										height: '100%',
										items: [],
										paneType: 'desktoppane',
										defaultSettings: '',
										widgets: widgets2
									]
							def jsLayoutConfig = layoutConfig as JSON

							def stckId = (type == 'Stack' ? group : 'NULL')

							maxDashboard++
							dValues.add("(${maxDashboard}, 0, 1, ${i - 1}, 0, '${dashboardGuid}', '${type} Dashboard ${i} (${group})', NULL, '', NULL, '2013-05-09 14:33:28', NULL, '2013-05-09 14:33:28', '${jsLayoutConfig.toString()}', 0, ${stckId})")
							maxDomainMapping++
							dmValues.add("(${maxDomainMapping}, 0, ${group}, 'group', 'owns', ${maxDashboard}, 'dashboard')")
						}
					}
				}

				file.println("-- Load Group and Stack Dashboards")
				file.println("LOCK TABLES `domain_mapping` WRITE, `dashboard` WRITE;")
				file.println("ALTER TABLE `domain_mapping` DISABLE KEYS;")
				file.println("ALTER TABLE `dashboard` DISABLE KEYS;")

				def realGroups = []
				realGroups.addAll(grpRecordIds)
				realGroups.removeAll(stkRecordIds)
				realGroups.removeAll([1, 2])
				createTheDashboards(realGroups, 'Group')
				createTheDashboards(stkRecordIds, 'Stack')

				if (!dValues.isEmpty()) {
					def jdValues = StringUtils.join(dValues, ',')
					def jdmValues = StringUtils.join(dmValues, ',')
					file.println("INSERT INTO `dashboard` VALUES ${jdValues};")
					file.println("INSERT INTO `domain_mapping` VALUES ${jdmValues};")
				}
				file.println("ALTER TABLE `dashboard` ENABLE KEYS;")
				file.println("ALTER TABLE `domain_mapping` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadWidgetDefinitions = {
				println "		Creating widget definitions..."
				def range = 1..numWidgets
				file.println("-- Load Widgets: ${numWidgets}")
				file.println("LOCK TABLES `widget_definition` WRITE, `widget_definition_widget_types` WRITE;")
				file.println("ALTER TABLE `widget_definition` DISABLE KEYS;")
				file.println("ALTER TABLE `widget_definition_widget_types` DISABLE KEYS;")

				def wdValues = []
				def wdtValues = []
				range.each { num ->
					def widgName = "Test Widget ${num}"
					def widgGuid = generateId()

					wdValues.add("(${num},0,1,'themes/common/images/widget-icons/HTMLViewer.png','themes/common/images/widget-icons/HTMLViewer.png',0,540,'1.0',440,'examples/walkthrough/widgets/HTMLViewer.gsp','${widgGuid}','${widgName}',0,NULL,NULL,'')")
					wdtValues.add("(${num}, 1)")
					if (num % 500 == 0) {
						def jwdValues = StringUtils.join(wdValues, ',')
						def jwdtValues = StringUtils.join(wdtValues, ',')
						file.println("INSERT INTO `widget_definition` VALUES ${jwdValues};")
						file.println("INSERT INTO `widget_definition_widget_types` VALUES ${jwdtValues};")
						wdValues.clear()
						wdtValues.clear()
					}
					widgetDefinitionIds.put(num, [name: widgName, guid: widgGuid])
				}
				if (!wdValues.isEmpty()) {
					def jwdValues = StringUtils.join(wdValues, ',')
					def jwdtValues = StringUtils.join(wdtValues, ',')
					file.println("INSERT INTO `widget_definition` VALUES ${jwdValues};")
					file.println("INSERT INTO `widget_definition_widget_types` VALUES ${jwdtValues};")
				}
				file.println("ALTER TABLE `widget_definition_widget_types` ENABLE KEYS;")
				file.println("ALTER TABLE `widget_definition` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadStacks = {
				println "		Creating stacks..."
				def range = 1..numStacks
				def grpOffset = grpRecordIds[grpRecordIds.size() - 1]
				file.println("-- Load Stacks: ${numStacks}")
				file.println("LOCK TABLES `stack` WRITE, `owf_group` WRITE, `domain_mapping` WRITE;")
				file.println("ALTER TABLE `stack` DISABLE KEYS;")
				file.println("ALTER TABLE `owf_group` DISABLE KEYS;")
				file.println("ALTER TABLE `domain_mapping` DISABLE KEYS;")
				range.each { num ->
					int plOne = num + grpOffset
					stkRecordIds.add(plOne)
					grpRecordIds.add(plOne)
					maxDomainMapping++

					file.println("INSERT INTO `stack` VALUES (${plOne},1,'TestStack${num}','TestStack${num}','TestStack${num}',NULL,NULL,0);")
					file.println("INSERT INTO `owf_group` VALUES (${plOne},1,'active',NULL,'','TestStack${num}-DefaultGroup',1,'TestStack${num}-DefaultGroup',1);")
					file.println("INSERT INTO `domain_mapping` VALUES (${maxDomainMapping},0,${plOne},'stack','owns',${plOne},'group');")
				}
				file.println("ALTER TABLE `domain_mapping` ENABLE KEYS;")
				file.println("ALTER TABLE `owf_group` ENABLE KEYS;")
				file.println("ALTER TABLE `stack` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadGroups = {
				println "		Creating groups..."
				def range = 1..numGroups
				def grpOffset = grpRecordIds[grpRecordIds.size() - 1]
				file.println("-- Load Groups: ${numGroups}")
				file.println("LOCK TABLES `owf_group` WRITE;")
				file.println("ALTER TABLE `owf_group` DISABLE KEYS;")
				range.each { num ->
					int plOne = num + grpOffset
					grpRecordIds.add(plOne)

					file.println("INSERT INTO `owf_group` VALUES (${plOne},0,'active','testgroup${num}@group${num}.com','TestGroup${num}','TestGroup${num}',1,'TestGroup${num}',0);")
				}
				file.println("ALTER TABLE `owf_group` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadGroupsAndStacksForUserId = { userId ->
				def grpsSeen = []

				def realGroupIds = []
				realGroupIds.addAll(grpRecordIds)
				realGroupIds.removeAll(stkRecordIds)
				realGroupIds.removeAll([1, 2])

				def sizeGrps = realGroupIds.size()
				def grpsUser = 1..numGroupsPerUser
				grpsUser.each {
					def grpAdd = realGroupIds[rand.nextInt(sizeGrps)]
					while (grpsSeen.contains(grpAdd)) {
						grpAdd = realGroupIds[rand.nextInt(sizeGrps)]
					}
					grpsSeen.add(grpAdd)
				}

				def sizeStcks = stkRecordIds.size()
				def stksUser = 1..numStacksPerUser
				stksUser.each {
					def grpAdd = stkRecordIds[rand.nextInt(sizeStcks)]
					while (grpsSeen.contains(grpAdd)) {
						grpAdd = stkRecordIds[rand.nextInt(sizeStcks)]
					}
					grpsSeen.add(grpAdd)
				}
				def valuePairings = []
				grpsSeen.each {
					valuePairings.add("(${userId}, ${it})")
				}
				valuePairings
			}

			private def loadAdmins = {
				println "		Creating admins..."
				def range = 1..numAdmins
				def pValues = []
				def gValues = []
				def rValues = []
				range.each { num ->
					maxPersonId++
					adminRecordIds.add(maxPersonId)

					pValues.add("(${maxPersonId},1,1,'Test Admin ${num}','TESTADMIN${num}',NULL,0,'testAdmin${num}@ozone3.test',NULL,'Test Administrator ${num}')")
					gValues.addAll(["(${maxPersonId}, 1)", "(${maxPersonId}, 2)"])
					gValues.addAll(loadGroupsAndStacksForUserId(maxPersonId))
					rValues.addAll(["(${maxPersonId},1)", "(${maxPersonId},2)"])
				}

				def jpValues = StringUtils.join(pValues, ',')
				def jgValues = StringUtils.join(gValues, ',')
				def jrValues = StringUtils.join(rValues, ',')
				file.println("-- Load Admins: ${numAdmins}")
				file.println("LOCK TABLES `person` WRITE, `owf_group_people` WRITE, `role_people` WRITE;")
				file.println("ALTER TABLE `person` DISABLE KEYS;")
				file.println("ALTER TABLE `owf_group_people` DISABLE KEYS;")
				file.println("ALTER TABLE `role_people` DISABLE KEYS;")
				file.println("INSERT INTO `person` VALUES ${jpValues};")
				file.println("INSERT INTO `owf_group_people` VALUES ${jgValues};")
				file.println("INSERT INTO `role_people` VALUES ${jrValues};")
				file.println("ALTER TABLE `role_people` ENABLE KEYS;")
				file.println("ALTER TABLE `owf_group_people` ENABLE KEYS;")
				file.println("ALTER TABLE `person` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadPersons = {
				println "		Creating users..."
				def range = 1..numUsers
				file.println("-- Load Users: ${numUsers}")
				file.println("LOCK TABLES `person` WRITE, `owf_group_people` WRITE, `role_people` WRITE;")
				file.println("ALTER TABLE `person` DISABLE KEYS;")
				file.println("ALTER TABLE `owf_group_people` DISABLE KEYS;")
				file.println("ALTER TABLE `role_people` DISABLE KEYS;")

				def pValues = []
				def gValues = []
				def rValues = []
				range.each { num ->
					maxPersonId++
					userRecordIds.add(maxPersonId)

					pValues.add("(${maxPersonId},1,1,'Test User ${num}','TESTUSER${num}',NULL,0,'testUser${num}@ozone3.test',NULL,'Test User ${num}')")
					gValues.addAll(["(${maxPersonId}, 1)"])
					gValues.addAll(loadGroupsAndStacksForUserId(maxPersonId))
					rValues.addAll(["(${maxPersonId},1)"])

					if (maxPersonId % 3000 == 0) {
						def jpValues = StringUtils.join(pValues, ',')
						def jgValues = StringUtils.join(gValues, ',')
						def jrValues = StringUtils.join(rValues, ',')
						file.println("INSERT INTO `person` VALUES ${jpValues};")
						file.println("INSERT INTO `owf_group_people` VALUES ${jgValues};")
						file.println("INSERT INTO `role_people` VALUES ${jrValues};")

						pValues.clear()
						gValues.clear()
						rValues.clear()
					}
				}

				if (!pValues.isEmpty()) {
					def jpValues = StringUtils.join(pValues, ',')
					def jgValues = StringUtils.join(gValues, ',')
					def jrValues = StringUtils.join(rValues, ',')
					file.println("INSERT INTO `person` VALUES ${jpValues};")
					file.println("INSERT INTO `owf_group_people` VALUES ${jgValues};")
					file.println("INSERT INTO `role_people` VALUES ${jrValues};")
				}
				file.println("ALTER TABLE `role_people` ENABLE KEYS;")
				file.println("ALTER TABLE `owf_group_people` ENABLE KEYS;")
				file.println("ALTER TABLE `person` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def assignWidgetsInGroups = {
				println "		Creating group/widget mappings..."
				def tmpGroupIds = []
				tmpGroupIds.addAll(grpRecordIds)
				tmpGroupIds.removeAll(stkRecordIds)
				tmpGroupIds.removeAll([1,2])
				def widgetIds = widgetDefinitionIds.keySet()
				def widgetIds2 = []
				widgetIds2.addAll(widgetIds)

				// Carve up the widget definitions roughly equally among the groups.
				def wdgPerGroup = 1..(widgetIds.size() / tmpGroupIds.size())
				def gRange = tmpGroupIds[0]..tmpGroupIds[tmpGroupIds.size() - 1]
				def wdRangeIdx = 0

				def dmValues = []
				gRange.each { gId ->
					wdgPerGroup.each {
						maxDomainMapping++
						dmValues.add("(${maxDomainMapping},0,${gId},'group','owns',${widgetIds2[wdRangeIdx]},'widget_definition')")
						wdRangeIdx++
					}
				}

				def jdmValues = StringUtils.join(dmValues, ',')
				file.println("-- Assign widgets to groups: ${tmpGroupIds.size()} groups, ${widgetDefinitionIds.size()} widgets")
				file.println("LOCK TABLES `domain_mapping` WRITE;")
				file.println("ALTER TABLE `domain_mapping` DISABLE KEYS;")
				file.println("INSERT INTO `domain_mapping` VALUES ${jdmValues};")
				file.println("ALTER TABLE `domain_mapping` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadPersonWidgetDefinitions = {
				println "		Creating person widget definitions..."
				file.println("-- Load Person Widget Definitions: ${numUsers} users, ${numWidgetsPerUser} widgets per user")
				file.println("LOCK TABLES `person_widget_definition` WRITE;")
				file.println("ALTER TABLE `person_widget_definition` DISABLE KEYS;")
				def userRange = []
				userRange.addAll([1]) // Map some widgets onto the default user
				userRange.addAll(adminRecordIds)
				userRange.addAll(userRecordIds)
				def range = 1..numWidgetsPerUser
				def widgetIds = widgetDefinitionIds.keySet()
				def widgetIds2 = []
				widgetIds2.addAll(widgetIds)
				def widgSize = widgetIds.size()

				def recordId = 0
				def pwdValues = []
				userRange.each { userNum ->
					def widgetsAdded = []
					range.each {
						recordId++
						def wd = rand.nextInt(widgSize)
						while (widgetsAdded.contains(wd)) {
							wd = rand.nextInt(widgSize)
						}
						widgetsAdded.add(wd)
						pwdValues.add("(${recordId},0,${userNum},1,${it},${widgetIds2[wd]},0,0,NULL,0,0)")

						// Write 25K at a time.
						if (recordId % 25000 == 0) {
							def jpwdValues = StringUtils.join(pwdValues, ',')
							file.println("INSERT INTO `person_widget_definition` VALUES ${jpwdValues};")
							pwdValues.clear()
						}
					}
					personWidgetMapping.put(userNum, widgetsAdded)
				}
				if (!pwdValues.isEmpty()) {
					def jpwdValues = StringUtils.join(pwdValues, ',')
					file.println("INSERT INTO `person_widget_definition` VALUES ${jpwdValues};")
				}
				file.println("ALTER TABLE `person_widget_definition` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadDashboards = {
				println "		Creating dashboards..."
				file.println("-- Load Dashboards: ${numUsers} users, ${numDashboards} dashboards per user, 1 clone")
				file.println("LOCK TABLES `domain_mapping` WRITE, `dashboard` WRITE;")
				file.println("ALTER TABLE `domain_mapping` DISABLE KEYS;")
				file.println("ALTER TABLE `dashboard` DISABLE KEYS;")
				maxDomainMapping++
				file.println("INSERT INTO `domain_mapping` VALUES (${maxDomainMapping}, 0, 1, 'group', 'owns', 1, 'dashboard');")

				def dValues = []
				def dmValues = []

				def createTheDashboards = { userId ->
					def userWidgets = personWidgetMapping.get(userId)

					def rangeOfDashboards = 1..numDashboards
					rangeOfDashboards.each { i ->
						def dashboardGuid = generateId()
						def paneGuid = generateId()

						def widgets2 = []
						def range = 1..numDashboardsWidgets
						range.eachWithIndex { dbWIndex, idx ->
							def xyPos = 300 + (dbWIndex * 5)

							def randomWidgetId = userWidgets[rand.nextInt(userWidgets.size())]
							def randomWidget = widgetDefinitionIds.get(randomWidgetId)
							while (!randomWidget) {
								randomWidgetId = userWidgets[rand.nextInt(userWidgets.size())]
								randomWidget = widgetDefinitionIds.get(randomWidgetId)
							}
							def dbWidgetRandomGuid = generateId()

							def singleWidget = [
										widgetGuid: randomWidget.guid,
										x: xyPos,
										y: xyPos,
										uniqueId: dbWidgetRandomGuid,
										name: randomWidget.name,
										paneGuid: paneGuid,
										height: 250,
										width: 250,
										dashboardGuid: dashboardGuid
									]
							widgets2.add(singleWidget)
						}

						def layoutConfig = [
									xtype: 'desktoppane',
									flex: 1,
									height: '100%',
									items: [],
									paneType: 'desktoppane',
									defaultSettings: '',
									widgets: widgets2
								]
						def jsLayoutConfig = layoutConfig as JSON

						maxDashboard++
						dValues.add("(${maxDashboard}, 0, 0, ${i}, 0, '${dashboardGuid}', '${i}-Dashboard (${userId})', ${userId}, '', NULL, '2013-05-09 14:33:28', NULL, '2013-05-09 14:33:28', '${jsLayoutConfig.toString()}', 0, NULL)")
						if (maxDashboard != 1 && i == 1) {
							// Create a clone so that we can test out the clone performance fix for logins.
							maxDomainMapping++
							dmValues.add("(${maxDomainMapping}, 0, ${maxDashboard}, 'dashboard', 'cloneOf', 1, 'dashboard')")
						}

						// Write in batches of 500.
						if (maxDashboard % 500 == 0) {
							def jdValues = StringUtils.join(dValues, ',')
							def jdmValues = StringUtils.join(dmValues, ',')
							file.println("INSERT INTO `dashboard` VALUES ${jdValues};")
							file.println("INSERT INTO `domain_mapping` VALUES ${jdmValues};")
							dValues.clear()
							dmValues.clear()
						}
					}
				}

				adminRecordIds.each {
					createTheDashboards(it)
				}
				userRecordIds.each {
					createTheDashboards(it)
				}
				if (!dValues.isEmpty()) {
					def jdValues = StringUtils.join(dValues, ',')
					def jdmValues = StringUtils.join(dmValues, ',')
					file.println("INSERT INTO `dashboard` VALUES ${jdValues};")
					file.println("INSERT INTO `domain_mapping` VALUES ${jdmValues};")
				}
				file.println("ALTER TABLE `dashboard` ENABLE KEYS;")
				file.println("ALTER TABLE `domain_mapping` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			private def loadPreferences = {
				println "		Creating preferences..."
				file.println("-- Load Preferences: ${numUsers} users, ${numPreferences} preferences per user")
				file.println("LOCK TABLES `preference` WRITE;")
				file.println("ALTER TABLE `preference` DISABLE KEYS;")
				// From production data, we know that the average number of prefs/user
				// is ~10 and the average value length is ~200
				def range = 10..30
				def keys = range.collect { "someKey_${it}" } // 10 chars
				def values = range.collect { "someLongerThanTheKeyNameValue_${it}" } // 32 chars
				def recordId = 0

				def prefValues = []
				def createThePrefs = { userNum ->
					def prefPerUserRange = 1..numPreferences
					def pairPerPrefRange = 1..5
					// For each user, create a known number of prefs.
					prefPerUserRange.each { prefNum ->
						// For each pref, select some number of random key/value pairs
						def kvPairings = pairPerPrefRange.collect {
							def idx = rand.nextInt(20)
							["${keys[idx]}":"${values[idx]}"]
						}

						recordId++
						prefValues.add("(${recordId}, 0, '${(kvPairings as JSON).toString()}', 'test path entry ${prefNum}', ${userNum}, 'foo.bar.${prefNum}')")

						// Write 3000 in a batch
						if (recordId % 3000 == 0) {
							def jprefValues = StringUtils.join(prefValues, ',')
							file.println("INSERT INTO `preference` VALUES ${jprefValues};")
							prefValues.clear()
						}
					}
				}

				adminRecordIds.each {
					createThePrefs(it)
				}
				userRecordIds.each {
					createThePrefs(it)
				}
				if (!prefValues.isEmpty()) {
					def jprefValues = StringUtils.join(prefValues, ',')
					file.println("INSERT INTO `preference` VALUES ${jprefValues};")
				}
				file.println("ALTER TABLE `preference` ENABLE KEYS;")
				file.println("UNLOCK TABLES;")
				file.println("")
				file.flush()
			}

			loadWidgetDefinitions()
			loadStacks()
			loadGroups()
			loadAdmins()
			loadPersons()
			assignWidgetsInGroups()
			loadPersonWidgetDefinitions()
			loadDashboards()
			loadPreferences()
			assignGroupDashboards()

			file.close()
		}
	}

	private createNewUser() {
		def user = Person.findByUsername(Person.NEW_USER)
		if (user == null) {
			user = new Person(
					username     : Person.NEW_USER,
					userRealName : Person.NEW_USER,
					lastLogin    : new Date(),
					email        : '',
					emailShow    : false,
					description  : '',
					enabled      : true).save(flush: true)
			def userRole = Role.findByAuthority(ERoleAuthority.ROLE_USER.strVal)
			def users = Person.findAllByUsername(Person.NEW_USER)
			if (userRole) {
				if (!userRole.people)
					userRole.people = users
				else
					userRole.people.add(Person.findByUsername(Person.NEW_USER))
				userRole.save(flush: true)
			}
		}
	}

	private loadWidgetTypes() {
		new WidgetType(name: 'standard').save()
	}
}
