package ozone.owf.grails.domain

import grails.test.mixin.TestFor

@TestFor(Person)
class PersonTests {

	void testUserNameDataQuality() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def personToTest = new Person(userRealName: 'A Real Name')
		personToTest.username = "\""
		assert personToTest.validate()

		personToTest.username = "\\"
		assert personToTest.validate()

		personToTest.username = """/"""
		assert personToTest.validate()

		personToTest.username = "#"
		assert personToTest.validate()

		personToTest.username = "="
		assert personToTest.validate()

		personToTest.username = """{"""
		assert personToTest.validate()

		personToTest.username = """}"""
		assert personToTest.validate()

		personToTest.username = ":"
		assert personToTest.validate()

		personToTest.username = ";"
		assert personToTest.validate()

		personToTest.username = ""","""
		assert personToTest.validate()

		personToTest.username = """["""
		assert personToTest.validate()

		personToTest.username = """]"""
		assert personToTest.validate()

		personToTest.username = "Hello World 1234567890!@\$%^&*()_+-|?><`~."
		assert personToTest.validate()

		person.username = "\\u5317\\u7F8E\\u4E2D\\u6587\\u5831\\u7D19"
		assert personToTest.validate()
	}

	void testUserRealNameDataQuality() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def personToTest = new Person(username: 'A User Name')

		personToTest.userRealName = "\""
		assert personToTest.validate()

		personToTest.userRealName = "\\"
		assert personToTest.validate()

		personToTest.userRealName = """/"""
		assert personToTest.validate()

		personToTest.userRealName = "#"
		assert personToTest.validate()

		personToTest.userRealName = "="
		assert personToTest.validate()

		personToTest.userRealName = """{"""
		assert personToTest.validate()

		personToTest.userRealName = """}"""
		assert personToTest.validate()

		personToTest.userRealName = ":"
		assert personToTest.validate()

		personToTest.userRealName = ";"
		assert personToTest.validate()

		personToTest.userRealName = ""","""
		assert personToTest.validate()

		personToTest.userRealName = """["""
		assert personToTest.validate()

		personToTest.userRealName = """]"""
		assert personToTest.validate()

		personToTest.userRealName = "Hello World 1234567890!@\$%^&*()_+-|?><`~."
		assert personToTest.validate()

		personToTest.userRealName = "\\u5317\\u7F8E\\u4E2D\\u6587\\u5831\\u7D19"
		assert personToTest.validate()
	}

	void testNullConstraints() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def p1Fail = new Person()
		assert !p1Fail.validate()
		assert 'nullable' == p1Fail.errors['username']
		assert 'nullable' == p1Fail.errors['userRealName']

		def p2Fail = new Person(username: '', userRealName: '')
		assert !p2Fail.validate()
		assert 'blank' == p2Fail.errors['username']
		assert 'blank' == p2Fail.errors['userRealName']
	}

	void testUniqueConstraints() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def p3Fail = new Person(username: 'Bogus', userRealName: 'Bogus')
		assert p3Fail.validate()

		def p4Fail = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		assert !p4Fail.validate()
		assert 'unique' == p4Fail.errors['username']
	}

	void testApostrophes() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def p4Fail = new Person(username: "A'postrophe", userRealName: 'Apostrophe')
		assert p4Fail.validate()

		def p5Fail = new Person(username: 'Apostrophe', userRealName: "A'postrophe")
		assert p5Fail.validate()
	}

	void testLengthConstraints() {
		def person = new Person(username: 'constraintTester', userRealName: 'Constraint Tester')
		mockForConstraintsTests(Person, [person])

		def baseString = "A quick brown fox jumps over the lazy dog.  "
		def description = ''
		(1..5).each { description += baseString }
		def longString1 = description[0..200]
		assert 201 == longString1.length()

		def p6Fail = new Person(userRealName: "A'postrophe", username: longString1)
		assert !p6Fail.validate()
		assert 'maxSize' == p6Fail.errors['username']

		def p7Fail = new Person(userRealName: longString1, username: 'Apostrophe')
		assert !p7Fail.validate()
		assert 'maxSize' == p7Fail.errors['userRealName']

		def longString2 = description[0..199]
		assert 200 == longString2.length()
		def p8Fail = new Person(userRealName: longString2, username: longString2)
		assert p8Fail.validate()
	}

	void testServiceModelNoParams() {
		def person = new Person(id: 1, username: 'serviceModelTester', userRealName: 'Service M. Tester')

		def sm = person.toServiceModel(null)
		assertNotNull sm
		assertEquals sm.id, person.id
		assertEquals sm.username, person.username
		assertEquals sm.userRealName, person.userRealName
		assertEquals '', sm.email
		assertNull sm.hasPWD
		assertEquals sm.lastLogin, person.lastLogin
		assertNull sm.lastLogin
		assertEquals 0, sm.totalGroups
		assertEquals 0, sm.totalWidgets
		assertEquals 0, sm.totalDashboards
		assertTrue sm.tagLinks.isEmpty()
	}

	void testServiceModelEmptyParams() {
		def person = new Person(username: 'serviceModelTester', userRealName: 'Service M. Tester')

		def sm = person.toServiceModel([:])
		assertNotNull sm
		assertEquals sm.id, person.id
		assertEquals sm.username, person.username
		assertEquals sm.userRealName, person.userRealName
		assertEquals '', sm.email
		assertNull sm.hasPWD
		assertEquals sm.lastLogin, person.lastLogin
		assertNull sm.lastLogin
		assertEquals 0, sm.totalGroups
		assertEquals 0, sm.totalWidgets
		assertEquals 0, sm.totalDashboards
		assertTrue sm.tagLinks.isEmpty()
	}

	void testServiceModelWithParams() {
		def params = [:]
		params.totalWidgets = 1
		params.totalGroups = 2
		params.totalDashboards = 3

		def person = new Person(username: 'serviceModelTester', userRealName: 'Service M. Tester')

		def sm = person.toServiceModel(params)
		assertNotNull sm
		assertEquals sm.id, person.id
		assertEquals sm.username, person.username
		assertEquals sm.userRealName, person.userRealName
		assertEquals '', sm.email
		assertNull sm.hasPWD
		assertEquals sm.lastLogin, person.lastLogin
		assertNull sm.lastLogin
		assertEquals params.totalGroups, sm.totalGroups
		assertEquals params.totalWidgets, sm.totalWidgets
		assertEquals params.totalDashboards, sm.totalDashboards
		assertTrue sm.tagLinks.isEmpty()
	}

	void testServiceModelBypassInvalidParams() {
		def params = [:]
		params.totalWidget = 1
		params.totalGroup = 2
		params.totalDashboard = 3
		params.id = 4
		params.username = 'Bogus'
		params.userRealName = 'Very Bogus'
		params.email = 'Bogus@bogus.com'
		params.lastLogin = new Date()

		def person = new Person(username: 'serviceModelTester', userRealName: 'Service M. Tester')

		def sm = person.toServiceModel(params)
		assertNotNull sm
		assertEquals sm.id, person.id
		assertEquals sm.username, person.username
		assertEquals sm.userRealName, person.userRealName
		assertEquals '', sm.email
		assertNull sm.hasPWD
		assertEquals sm.lastLogin, person.lastLogin
		assertNull sm.lastLogin
		assertEquals 0, sm.totalGroups
		assertEquals 0, sm.totalWidgets
		assertEquals 0, sm.totalDashboards
		assertTrue sm.tagLinks.isEmpty()
	}
}
