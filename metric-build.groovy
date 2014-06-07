environments {
	with_mine  {
		dataSource {
			pooled = true
			dbCreate = "none"
			driverClassName = "com.mysql.jdbc.Driver"
			url = "jdbc:mysql://${ozone.dbServerPort}/${ozone.dbUsername}_metric"
			username = ozone.dbUsername
			password = ozone.dbPassword
			dialect = "org.hibernate.dialect.MySQL5InnoDBDialect"
			properties {
				minEvictableIdleTimeMillis = 180000
				timeBetweenEvictionRunsMillis = 180000
				numTestsPerEvictionRun = 3
				testOnBorrow = true
				testWhileIdle = true
				testOnReturn = true
				validationQuery = "SELECT 1"
			}
		}
	}
}
