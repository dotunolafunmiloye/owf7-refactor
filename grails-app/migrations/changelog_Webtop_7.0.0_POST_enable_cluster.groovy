databaseChangeLog = {
	changeSet(author: "wallami", id: "7.0.0-webtop-enable_cluster", context: "create, upgrade, 7.0.0", dbms: "mysql, postgresql, oracle, hsqldb") {
		comment(text="Added primary keys to tables.  This enables Galera clustering.")
		sql ( text = """
				alter table widget_def_intent_data_types add id bigint(20) primary key auto_increment;
				alter table intent_data_types            add id bigint(20) primary key auto_increment;
		""")
	}
}

