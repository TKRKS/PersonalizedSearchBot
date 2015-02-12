package cs7270.personalizedSearch;

import org.openqa.selenium.By;
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
        final String searchTerm = JOptionPane.showInputDialog("Enter search term");
        final String userName = JOptionPane.showInputDialog("Enter username");
        JPanel panel = new JPanel();
        JLabel label = new JLabel("Enter a password:");
        JPasswordField pass = new JPasswordField(20);
        panel.add(label);
        panel.add(pass);
        JOptionPane.showMessageDialog(null, panel);
        final String password = new String(pass.getPassword());

        // Create a new instance of the Firefox driver
        // Notice that the remainder of the code relies on the interface, 
        // not the implementation.
        final WebDriver driver = new FirefoxDriver();

        // And now use this to visit Google
        driver.get("http://www.google.com");
        if ((!userName.isEmpty() && !password.isEmpty())) {
            driver.findElement(By.xpath("//a[text()='Sign in']")).click();
            driver.findElement(By.name("Email")).sendKeys(userName);
            driver.findElement(By.name("Passwd")).sendKeys(password);
            driver.findElement(By.name("signIn")).click();
        }

        // If using Google two factor, this will wait for the search page.
        new WebDriverWait(driver, 300).until(new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(WebDriver webDriver) {
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

        // Google's search is rendered dynamically with JavaScript.
        // Wait for the page to load, timeout after 10 seconds
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith(searchTerm);
            }
        });

        // Should see: "cheese! - Google Search"
        System.out.println("Page title is: " + driver.getTitle());

        //Close the browser
        driver.quit();
    }
}
