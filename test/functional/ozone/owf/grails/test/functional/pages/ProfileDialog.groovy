package ozone.owf.grails.test.functional.pages

import org.codehaus.groovy.grails.plugins.webdriver.WebDriverPage
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class ProfileDialog extends WebDriverPage {

	List<WebElement> findProfileDataTableLabels() {
		driver.findElements(By.className("fieldLabel"))
	}

	static elements = {
		header(By.className("x-window-header-text-default"))
		bodyHeading(By.className("x-panel-header-text-default"))
	}
}
