package ozone.owf.grails.test.functional.pages

import org.openqa.selenium.WebElement

class OwfMainPage extends BasePage {

	static expectedTitle = "Untitled"

	String getBannerText() {
		return customHeader.getText()
	}
}