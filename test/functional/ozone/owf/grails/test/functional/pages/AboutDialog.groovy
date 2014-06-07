package ozone.owf.grails.test.functional.pages;

import org.codehaus.groovy.grails.plugins.webdriver.WebDriverPage
import org.openqa.selenium.By
import org.openqa.selenium.WebElement

public class AboutDialog extends WebDriverPage {

	static elements = {
		message(By.xpath("//p[@id='aboutInfo']"))
	}
}
