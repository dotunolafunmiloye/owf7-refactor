package ozone.owf.grails.test.functional.tests

import static org.junit.Assert.*

import org.codehaus.groovy.grails.plugins.webdriver.WebDriverHelper
import org.junit.Before
import org.junit.Rule
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

abstract class TestBase {

	@Rule
	public WebDriverHelper webdriver = new WebDriverHelper()

	@Before void setUp() {
		// If you want to use log4j to actually log the progress of your
		// tests, unremark the following line and then simply add the 
		// logging statements to tests, pages.
		// DOMConfigurator.configure("src/resources/owf-testing-log4j.xml")
		webdriver.open('/')

		// Give time for the Ext mask to clear.
		WebDriverWait wait = new WebDriverWait(webdriver.driver, 10)
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("owf-body-mask")))
	}

	protected void waitABit(int interval = 1000) {
		try {
			Thread.sleep(interval);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
