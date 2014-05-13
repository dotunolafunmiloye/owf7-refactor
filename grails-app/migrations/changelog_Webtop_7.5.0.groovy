databaseChangeLog = {

	changeSet(author: "owf", id: "7.5.0-1", context: "create, upgrade, 7.5.0") {
		comment("Remove stack mappings from domain_mapping table.")
		delete(tableName: "domain_mapping") {
			where(text="src_type = 'stack'")

		}
		delete(tableName: "domain_mapping") {
			where(text="dest_type = 'stack'")
		}
	}

	changeSet(author: "owf", id: "7.5.0-2", context: "create, upgrade, 7.5.0") {
		comment("Remove stack_group table.")
		dropTable(tableName: "stack_groups")
	}

	changeSet(author: "owf", id: "7.5.0-3", context: "create, upgrade, 7.5.0") {
		comment("Remove foreign key constraint to stack table from dashboard table.")
		dropForeignKeyConstraint(constraintName: "FKC18AEA946B3A1281", baseTableName: "dashboard")
	}

	changeSet(author: "owf", id: "7.5.0-4", context: "create, upgrade, 7.5.0") {
		comment("Remove stack_id column from dashboard table.")
		dropColumn(tableName: "dashboard", columnName: "stack_id")
	}

	changeSet(author: "owf", id: "7.5.0-5", context: "create, upgrade, 7.5.0") {
		comment("Remove stack table.")
		dropTable(tableName: "stack")
	}

	changeSet(author: "owf", id: "7.5.0-6", context: "create, upgrade, 7.5.0") {
		comment("Remove stack_default column from group table.")
		dropColumn(tableName: "owf_group", columnName: "stack_default")
	}

	changeSet(author: "owf", id: "7.5.0-7", context: "create, upgrade, 7.5.0", dbms: 'mysql') {
		comment("Remove the stack widgets (2) from being considered administrative widgets")
		sql (text = """
DELETE wdwt
	FROM widget_definition_widget_types wdwt INNER JOIN widget_definition wd
		ON (wdwt.widget_definition_id = wd.id AND wd.display_name LIKE 'Stack%');
		""")
	}

	changeSet(author: "owf", id: "7.5.0-8", context: "create, upgrade, 7.5.0", dbms: 'mysql') {
		comment("Remove the stack widgets from person widget definitions")
		sql (text = """
DELETE pwd
	FROM person_widget_definition pwd INNER JOIN widget_definition wd
		ON (pwd.widget_definition_id = wd.id AND wd.display_name LIKE 'Stack%');
		""")
	}

	changeSet(author: "owf", id: "7.5.0-9", context: "create, upgrade, 7.5.0", dbms: 'mysql') {
		comment("Remove the stack widgets from widget definitions")
		sql (text = """
DELETE FROM widget_definition
WHERE
	widget_definition.display_name LIKE 'Stack%';
		""")
	}

	changeSet(author: 'owf', id: '7.5.0-10', context: 'create, upgrade, 7.5.0') {
		comment('Remove the role_people and role tables.')
		dropTable(tableName: 'role_people')
		dropTable(tableName: 'role')
	}

	changeSet(author: 'owf', id: '7.5.0-11', context: 'create, upgrade, 7.5.0') {
		comment('Remove the widget_def_intent and widget_def_intent_data_types tables.')
		dropTable(tableName: 'widget_def_intent_data_types')
		dropTable(tableName: 'widget_def_intent')
	}

	changeSet(author: 'owf', id: '7.5.0-12', context: 'create, upgrade, 7.5.0') {
		comment('Remove the intent_data_types and intent_data_type tables.')
		dropTable(tableName: 'intent_data_types')
		dropTable(tableName: 'intent_data_type')
	}

	changeSet(author: 'owf', id: '7.5.0-13', context: 'create, upgrade, 7.5.0') {
		comment('Remove the intent table.')
		dropTable(tableName: 'intent')
	}

	changeSet(author: 'owf', id: '7.5.0-14', context: 'create, upgrade, 7.5.0') {
		comment('Remove the requestmap table.')
		dropTable(tableName: 'requestmap')
	}

	changeSet(author: 'owf', id: '7.5.0-15', context: 'create, upgrade, 7.5.0') {
		comment('Create the principal table.')
		createTable(tableName: 'principal') {
			column(autoIncrement: "true", name: "id", type: "bigint") {
				constraints(nullable: "false", primaryKey: "true", primaryKeyName: "principalPK")
			}

			column(name: "canonical_name", type: "varchar(200)") {
				constraints(nullable: "false", unique: "true")
			}
			column(name: "friendly_name", type: "varchar(200)") {
				constraints(nullable: "false")
			}
			column(name: "description", type: "varchar(255)")
			column(name: "last_login", type: "datetime")
			column(name: "date_created", type: "datetime") {
				constraints(nullable: "false")
			}
			column(name: "type", type: "varchar(5)") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: 'owf', id: '7.5.0-16', context: 'create, upgrade, 7.5.0') {
		comment('Drop the create date from the principal table.')
		dropColumn(tableName: "principal", columnName: "date_created")
	}

	changeSet(author: 'owf', id: '7.5.0-17', context: 'create, upgrade, 7.5.0') {
		comment('Add legacy columns for group and person IDs to principal, plus a constraint check.')
		addColumn(tableName: "principal") {
			column(name: "person_id", type: "bigint")
		}
		addColumn(tableName: "principal") {
			column(name: "group_id", type: "bigint")
		}

		sql (text = """
			alter table principal add constraint legacy_id_exists check (group_id != null OR person_id != null);
		""")

		addForeignKeyConstraint(constraintName: "FK_Legacy_Person_ID",
				baseTableName: "principal", baseColumnNames: "person_id",
				referencedTableName: "person", referencedColumnNames: "id")

		addForeignKeyConstraint(constraintName: "FK_Legacy_Group_ID",
				baseTableName: "principal", baseColumnNames: "group_id",
				referencedTableName: "owf_group", referencedColumnNames: "id")
	}

	// This change set was added after the ones below, but needs to execute before -18.  Since Liquibase
	// executes these in the order they appear in the file, we can get away with not renumbering.
	changeSet(author: 'owf', id: '7.5.0-21', context: 'create, upgrade, 7.5.0') {
		comment('Make sure all groups have a display name.')
		sql (text = "UPDATE owf_group SET display_name = name WHERE display_name IS NULL")
	}

	changeSet(author: 'owf', id: '7.5.0-18', context: 'create, upgrade, 7.5.0') {
		comment('Populate principal table from person and owf_group data.')
		sql (text = """
			insert into principal (person_id, canonical_name, friendly_name, description, last_login, type)
				select id, username, user_real_name, description, last_login, "user" from person;

			insert into principal (group_id, canonical_name, friendly_name, description, type)
				select id, name, display_name, description, "group" from owf_group;
		""")
	}

	changeSet(author: 'owf', id: '7.5.0-19', context: 'create, upgrade, 7.5.0') {
		comment('Add the Hibernate-required version column to principal')
		addColumn(tableName: 'principal') {
			column(name: 'version', type: 'bigint', value: 0) {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: 'owf', id: '7.5.0-20', context: 'create, upgrade, 7.5.0') {
		comment('Index the src columns of domain mapping to improve dashboard performance.')
		createIndex(tableName: 'domain_mapping', indexName: 'idx_dm_sources') {
			column(name: 'src_id')
			column(name: 'src_type')
		}
	}

	// Whatever comes next needs to be 7.5.0-22; changeset 7.5.0-21 is above and reflects a missed statement.
	changeSet(author: 'owf', id: '7.5.0-22', context: 'create, upgrade, 7.5.0') {
		comment('Remove domain mappings for non-existent widgets')
		sql (text = """
			DELETE FROM domain_mapping WHERE
				dest_type = 'widget_definition' AND
				dest_id NOT IN (
					SELECT id FROM widget_definition
				)""")
	}

	changeSet(author: 'owf', id: '7.5.0-23', context: 'create, upgrade, 7.5.0') {
		comment('Drop taggable plugin related tables.')
		dropTable(tableName: 'tag_links')
		dropTable(tableName: 'tags')
	}

	changeSet(author: 'owf', id: '7.5.0-24', context: 'create, upgrade, 7.5.0') {
		// Ultimately, we'll make this column not-null, but for now we're allowing it
		// so that we don't have to plug in a default value.
		comment('Add widget_type_id column to widget_definition table.')
		addColumn(tableName: 'widget_definition') {
			column(name: 'widget_type_id', type: 'bigint') {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: 'owf', id: '7.5.0-25', context: 'create, upgrade, 7.5.0') {
		comment('Migrate data to widget_definition.widget_type_id.')
		sql (text = """
			UPDATE widget_definition wd, widget_definition_widget_types wdwt SET
				wd.widget_type_id = wdwt.widget_type_id
			WHERE
				wd.id = wdwt.widget_definition_id
		""")
		sql (text = """
			UPDATE widget_definition wd SET
				wd.widget_type_id = (SELECT id FROM widget_type WHERE name = 'standard')
			WHERE
				wd.widget_type_id IS NULL
		""")
	}

	changeSet(author: 'owf', id: '7.5.0-26', context: 'create, upgrade, 7.5.0') {
		comment('Add not null constraint to widget_definition.widget_type_id.')
		addNotNullConstraint(columnDataType: "BIGINT", columnName: "widget_type_id", tableName: "widget_definition")
	}

	changeSet(author: 'owf', id: '7.5.0-27', context: 'create, upgrade, 7.5.0') {
		comment('Add foreign key to widget_definition.widget_type pointing to widget_types table.')
		addForeignKeyConstraint(constraintName: "FK_Widget_Type_ID",
				baseTableName: "widget_definition", baseColumnNames: "widget_type_id",
				referencedTableName: "widget_type", referencedColumnNames: "id")
	}

	changeSet(author: 'owf', id: '7.5.0-28', context: 'create, upgrade, 7.5.0') {
		comment('Drop widget_definition_widget_types table.')
		dropTable(tableName: 'widget_definition_widget_types')
	}
}
