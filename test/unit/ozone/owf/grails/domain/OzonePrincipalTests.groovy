package ozone.owf.grails.domain

import grails.test.mixin.TestFor

@TestFor(OzonePrincipal)
class OzonePrincipalTests {

	void testOzonePrincipalNotNullConstraints() {
		mockForConstraintsTests(OzonePrincipal)

		def p = new OzonePrincipal()
		assert !p.validate()
		assert 'nullable' == p.errors.getFieldError('canonicalName').code
		assert 'nullable' == p.errors.getFieldError('friendlyName').code
		assert 'nullable' == p.errors.getFieldError('type').code
	}

	void testOzonePrincipalNotBlankConstraints() {
		mockForConstraintsTests(OzonePrincipal)

		def p = new OzonePrincipal(canonicalName: '', friendlyName: '', type: '')
		assert !p.validate()
		assert 'blank' == p.errors.getFieldError('canonicalName').code
		assert 'blank' == p.errors.getFieldError('friendlyName').code
		assert 'blank' == p.errors.getFieldError('type').code
	}

	void testOzonePrincipalInListConstraints() {
		mockForConstraintsTests(OzonePrincipal)

		def p = new OzonePrincipal(type: 'BOGUS')
		assert !p.validate()
		assert 'not.inList' == p.errors.getFieldError('type').code
	}

	void testOzonePrincipalLengthConstraints() {
		mockForConstraintsTests(OzonePrincipal)

		def base = "123456789O"
		def strTest = ''
		(1..20 as List).each {
			strTest += base
		}
		strTest += "1"

		def p = new OzonePrincipal(canonicalName: strTest, friendlyName: strTest)
		assert !p.validate()
		assert 'maxSize.exceeded' == p.errors.getFieldError('canonicalName').code
		assert 'maxSize.exceeded' == p.errors.getFieldError('friendlyName').code
	}

	void testOzonePrincipalUniqueConstraints() {
		def p = new OzonePrincipal(canonicalName: 'TESTUSER1', friendlyName: 'Test User 1', type: 'user')
		mockForConstraintsTests(OzonePrincipal, [p])

		def p2 = new OzonePrincipal(canonicalName: 'TESTUSER1', friendlyName: 'Test User 2', type: 'user')
		assert !p2.validate()
		assert 'unique' == p2.errors.getFieldError('canonicalName').code
	}

	void testOzonePrincipalFromPerson() {
		mockForConstraintsTests(Person)
		mockForConstraintsTests(OzonePrincipal)

		def dt = new Date()
		def pers = new Person(username: 'TESTUSER1', userRealName: 'Test User 1', description: 'Test User', lastLogin: dt)
		assert pers.validate()

		def prin = OzonePrincipal.fromPerson(pers)
		assertNotNull prin
		assertTrue prin instanceof OzonePrincipal

		assert prin.canonicalName == pers.username
		assert prin.friendlyName == pers.userRealName
		assert prin.description == pers.description
		assert prin.type == 'user'
		assert prin.lastLogin == pers.lastLogin
	}

	void testOzonePrincipalFromGroup() {
		mockForConstraintsTests(Group)
		mockForConstraintsTests(OzonePrincipal)

		def dt = new Date()
		def grp = new Group(name: 'TESTGROUP1', displayName: 'Test Group 1', description: 'Test Group')
		assert grp.validate()

		def prin = OzonePrincipal.fromGroup(grp)
		assertNotNull prin
		assertTrue prin instanceof OzonePrincipal

		assert prin.canonicalName == grp.name
		assert prin.friendlyName == grp.displayName
		assert prin.description == grp.description
		assert prin.type == 'group'
	}
}
