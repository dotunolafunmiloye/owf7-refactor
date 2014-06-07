package ozone.owf.grails.test.functional.pages;

import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait

/**
 * This class must be used with a descendant of BasePage.  This is a mixin to that page which allows for the function
 * of the menu to be established.
 */
public class UserMenu {
	ProfileDialog goToUserProfileDialog() {
		WebElement<ProfileDialog> link = driver.findElement(By.id("profile"))
		clickUserMenuItem(link)
		link.click()
	}


	AboutDialog goToAboutDialog() {
		WebElement<AboutDialog> link = driver.findElement(By.id("about"))
		clickUserMenuItem(link)
		link.click()
	}

	void clickUserMenuItem(link) {
		openUserMenu()

		// Wait up to five seconds for the menu to render
		WebDriverWait wait = new WebDriverWait(driver, 5)
		wait.until(ExpectedConditions.visibilityOf(link))
	}
}
