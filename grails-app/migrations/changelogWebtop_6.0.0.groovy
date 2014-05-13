databaseChangeLog = {

	// Changes required to make a pristine version from Ozone fit Webtops' needs.
	changeSet(author: "mayojac", id: "6.0.0-webtop-create-1", context: "legacy_ngt, 6.0.0") {
		createTable(tableName: "server_configuration") {
			column(name: "SYSTEM_NAME", type: "VARCHAR(255)") { constraints(nullable: "false") }
			column(name: "CONFIG_KEY", type: "VARCHAR(255)") { constraints(nullable: "false") }
			column(name: "VALUE", type: "VARCHAR(4000)")
		}
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-create-2", context: "legacy_ngt, 6.0.0") {
		createTable(tableName: "server_configuration_keys") {
			column(name: "KEY_NAME", type: "VARCHAR(255)") {
				constraints(nullable: "false", primaryKey: "true")
			}
			column(name: "KEY_DESC", type: "VARCHAR(4000)")
		}
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-create-3", context: "legacy_ngt, 6.0.0") {
		createTable(tableName: "version") {
			column(autoIncrement: "true", name: "id", type: "BIGINT") {
				constraints(nullable: "false", primaryKey: "true")
			}
			column(name: "sprint_level", type: "BIGINT") { constraints(nullable: "false") }
			column(name: "user_story", type: "VARCHAR(16)") { constraints(nullable: "false") }
			column(name: "schema_name", type: "VARCHAR(16)") { constraints(nullable: "false") }
			column(name: "user_name", type: "VARCHAR(16)") { constraints(nullable: "false") }
			column(name: "session_user", type: "VARCHAR(24)") { constraints(nullable: "false") }
			column(defaultValueComputed: "CURRENT_TIMESTAMP", name: "update_dt", type: "TIMESTAMP") { constraints(nullable: "false") }
		}
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-create-4", context: "legacy_ngt, 6.0.0") {
		addPrimaryKey(columnNames: "SYSTEM_NAME, CONFIG_KEY", tableName: "server_configuration")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-create-5", context: "legacy_ngt, 6.0.0") {
		addForeignKeyConstraint(baseColumnNames: "CONFIG_KEY", baseTableName: "server_configuration", constraintName: "server_configuration_fk01", deferrable: "false", initiallyDeferred: "false", onDelete: "NO ACTION", onUpdate: "NO ACTION", referencedColumnNames: "KEY_NAME", referencedTableName: "server_configuration_keys", referencesUniqueColumn: "false")
	}

	// Changes to a 3.7 --> 6.0 upgrade required to make Webtops' database look like Ozone pristine.
	changeSet(author: "mayojac", id: "6.0.0-webtop-1", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "altered_by_admin", newDataType: "BIT", tableName: "dashboard")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-2", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "isdefault", newDataType: "BIT", tableName: "dashboard")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-3", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "active", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-4", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "button_id", newDataType: "VARCHAR(255)", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-5", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "button_opened", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-6", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "collapsed", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-7", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "maximized", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-8", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "minimized", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-9", context: "create, upgrade, 6.0.0") {
		dropNotNullConstraint(columnDataType: "BIGINT", columnName: "person_widget_definition_id", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-10", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "pinned", newDataType: "BIT", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-11", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "email_show", newDataType: "BIT", tableName: "person")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-12", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "enabled", newDataType: "BIT", tableName: "person")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-13", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "visible", newDataType: "BIT", tableName: "person_widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-14", context: "create, upgrade, 6.0.0") {
		addNotNullConstraint(columnDataType: "BIGINT", columnName: "widget_definition_id", tableName: "person_widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-15", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "namespace", newDataType: "VARCHAR(255)", tableName: "preference")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-16", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "visible", newDataType: "BIT", tableName: "tag_links")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-17", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "image_url_large", newDataType: "VARCHAR(2083)", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-18", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "image_url_small", newDataType: "VARCHAR(2083)", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-19", context: "create, upgrade, 6.0.0") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "singleton", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-20", context: "create, upgrade, 6.0.0") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "visible", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-21", context: "create, upgrade, 6.0.0") {
		modifyDataType(columnName: "widget_url", newDataType: "VARCHAR(2083)", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-22", dbms: "mysql", context: "upgrade, 6.0.0") {
		comment("Remove lower-case triggers from DashboardWidgetState")
		sql ( text = """
drop trigger if exists person_bi;
drop trigger if exists person_bu;
 		""" )
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-23", dbms: "mysql", context: "upgrade, 6.0.0") {
		comment("Remove raptor_newuser stored procedure")
		sql ( text = """
drop procedure if exists raptor_newuser;
 		""" )
	}

	changeSet(author: "mayojac", id: "6.0.0-webtop-24", dbms: "mysql", context: "upgrade, 6.0.0") {
		comment("Force user names to be uppercase in person table")
		sql ( text = """
create table temp as (SELECT id, SUBSTRING(username, 4, LOCATE(",",username)-4) AS username from person);
update person inner join temp on person.id = temp.id set person.username = temp.username WHERE temp.username <> "";
update person set username = UCASE(username);
drop table temp;
 		""" )
	}

	// This constraint actually should be removed as it presents problems when adding widgets to OWF
	// from OMP.
	changeSet(author: "mayojac", id: "6.0.0-webtop-25", context: "upgrade, 6.0.0") {
		dropForeignKeyConstraint(baseTableName: "tag_links", constraintName: "tag_links_tr_fk")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-25") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "altered_by_admin", tableName: "dashboard")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-26") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "isdefault", tableName: "dashboard")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-27") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "active", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-28") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "button_opened", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-29") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "collapsed", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-30") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "maximized", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-31") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "minimized", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-32") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "pinned", tableName: "dashboard_widget_state")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-33") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "email_show", tableName: "person")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-34") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "enabled", tableName: "person")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-35") {
		addNotNullConstraint(columnDataType: "BIT", columnName: "visible", tableName: "person_widget_definition")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-36") {
		modifyDataType(columnName: "namespace", newDataType: "VARCHAR(200)", tableName: "preference")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-37") {
		addNotNullConstraint(columnDataType: "VARCHAR(200)", columnName: "namespace", tableName: "preference")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-38") {
		addNotNullConstraint(columnDataType: "VARCHAR(2083)", columnName: "image_url_large", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-39") {
		addNotNullConstraint(columnDataType: "VARCHAR(2083)", columnName: "image_url_small", tableName: "widget_definition")
	}

	changeSet(author: "mayojac", id: "webtop-6.0.0-40") {
		addNotNullConstraint(columnDataType: "VARCHAR(2083)", columnName: "widget_url", tableName: "widget_definition")
	}

}
