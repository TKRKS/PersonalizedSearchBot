package cs7270.personalizedSearch;

import org.apache.xpath.SourceTree;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;

/* Right now this is just the demo from selenium's website.  We will alter it as we work on this project.  Its set to test using firefox becuase chrome involves additional weird setup stuff.*/

public class PersonalizedSearchBot {

    public static void main(String[] args) {
        //Get the search, username and password
        final String searchTerm = JOptionPane.showInputDialog(null, "Enter search term");
        final boolean usePersonalizedSearch =
                JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                        null, "Use personalized search?", "Use personalized search?",
                        JOptionPane.YES_NO_OPTION
                );

        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
        final WebDriver driver = new FirefoxDriver();

        // And now use this to visit Google
        driver.get("http://www.google.com");
        if (usePersonalizedSearch) {
            driver.findElement(By.xpath("//a[text()='Sign in']")).click();
        }

        // If using Google two factor, this will wait for the search page.
        new WebDriverWait(driver, 300).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return driver.getTitle().toLowerCase().equals("google");
            }
        });

        // Find the text input element by its name
        WebElement element = driver.findElement(By.name("q"));

        // Enter something to search for
        element.sendKeys(searchTerm);

        // Now submit the form. WebDriver will find the form for us from the element
        element.submit();

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());

        //Read the first 5 pages
        for (int i = 0; i < 5; i++) {
            (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
                public Boolean apply(WebDriver d) {
                    return d.getTitle().startsWith(searchTerm);
                }
            });

            //Go to the next page
            WebElement nextLink = driver.findElement(By.id("pnnext"));
            nextLink.click();

            //Hack to get the bot to wait for the next page to load before continuing.
            while(true) {
                try {
                    nextLink.findElement(By.id("doesnt-exist"));
                } catch (StaleElementReferenceException e) {
                    break;
                } catch (Exception e) {}
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {}
            }
        }

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());

        //Close the browser
        //driver.quit();
    }
}
