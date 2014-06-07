package ozone.owf.grails.test.functional.tests

import static org.junit.Assert.*

import org.junit.Test
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

import ozone.owf.grails.test.functional.pages.OwfMainPage

class OwfMainPageTests extends TestBase {

	@Test
	public void testOwfMainPage() {
		OwfMainPage owfMainPage = webdriver.create(OwfMainPage.class)
		assertTrue(owfMainPage.getBannerText() == "TOP SECRET//HCS/SI-G/TK//NOFORN")
	}

	@Test
	public void testOpenAbout() {
		OwfMainPage owfMainPage = webdriver.create(OwfMainPage.class)
		def aboutPage = owfMainPage.goToAboutDialog()
		// Wait up to ten seconds for the about dialog (about.jsp) to render
		WebDriverWait wait = new WebDriverWait(webdriver.driver, 10)
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("aboutInfo")))
		assertTrue(aboutPage.message.getText().contains("Version"))
	}

	@Test
	public void testOpenProfile() {
		OwfMainPage owfMainPage = webdriver.create(OwfMainPage.class)
		def profilePage = owfMainPage.goToUserProfileDialog()

		WebDriverWait wait = new WebDriverWait(webdriver.driver, 10)
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("userInfoTable")))

		assertTrue(profilePage.header.getText() == "Profile")
		assertTrue(profilePage.bodyHeading.getText() == "User Information")

		def profileLabels = [
			'User Name',
			'Full Name',
			'Email',
			'Member of'
		]
		profilePage.findProfileDataTableLabels().each { WebElement el ->
			assertTrue(profileLabels.contains(el.getText()))
		}
	}
}
