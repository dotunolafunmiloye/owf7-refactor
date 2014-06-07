package ozone.owf.grails.test.functional.pages

import static org.junit.Assert.*

import org.codehaus.groovy.grails.plugins.webdriver.ButtonElement
import org.codehaus.groovy.grails.plugins.webdriver.WebDriverPage
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.FindBy

@Mixin(UserMenu)
abstract class BasePage extends WebDriverPage {
	@FindBy(xpath = "//div[@id='userMenuBtn']")
	WebElement userMenuButton

	@FindBy(xpath = "//div[@id='customHeaderComponent']")
	WebElement customHeader

	@FindBy(xpath = "//div[@id='launchMenuBtn']//button")
	ButtonElement launchMenuButton

	@FindBy(xpath = "//div[@id='dashMenuBtn']//button")
	ButtonElement dashMenuBtn

	@FindBy(xpath = "//div[@class='settingsBtn']//button")
	ButtonElement settingsBtn

	@FindBy(xpath = "//div[@class='adminBtn']//button")
	ButtonElement adminBtn

	@FindBy(xpath = "//div[@class='helpBtn']//button")
	ButtonElement helpBtn

	Object clickYesButton(Object type) {
		driver.findElement(By.xpath("//button[text() = 'Yes']")).click()
		delay(3000)
		create(type)
	}

	protected void delay(int time = 1000) {
		try {
			Thread.sleep(time);
		}
		catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	boolean isTextPresent(String text) {
		WebElement bodyTag =
				driver.findElement(By.tagName("body"));
		println "Body text:\n${bodyTag?.getText()}"
		bodyTag?.getText().contains(text)
	}

	protected openUserMenu() {
		Actions a = new Actions(driver)
		Actions todo = a.moveToElement(userMenuButton)
		todo.perform()
	}
}