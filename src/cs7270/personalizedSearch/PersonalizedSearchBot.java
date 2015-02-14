package cs7270.personalizedSearch;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class PersonalizedSearchBot {

	private static class Result {
		private String url;
		private String title;
		private String result;

		public String getUrl() {
			return url;
		}
	
		public void setUrl(final String url) {
			this.url = url;
		}

		public String getTitle() {
			return title;
		}
	
		public void setTitle(final String title) {
			this.title = title;
		}

		public String getResult() {
			return result;
		}
	
		public void setResult(final String result) {
			this.result = result;
		}
	}

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {

	final int pages = 5;
	List<Result> results = new ArrayList<Result>();

        //Get the search, username and password
        final String searchTerm = JOptionPane.showInputDialog("Enter search term");
	String userName = "";
	String password = "";
        final boolean usePersonalizedSearch =
                JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                        null, "Use personalized search?", "Use personalized search?",
                        JOptionPane.YES_NO_OPTION
                );
	if (usePersonalizedSearch) {
		userName = JOptionPane.showInputDialog("Enter username");
		JPanel panel = new JPanel();
		JLabel label = new JLabel("Enter a password:");
		JPasswordField pass = new JPasswordField(20);
		panel.add(label);
		panel.add(pass);
		JOptionPane.showMessageDialog(null, panel);
		password = new String(pass.getPassword());
	}


        // Firefox driver since Chrome driver requires extra work
        final WebDriver driver = new FirefoxDriver();

	//Go to google
        driver.get("http://www.google.com");

	//Login if necessary
        if (usePersonalizedSearch && !userName.isEmpty() && !password.isEmpty()) {
            driver.findElement(By.xpath("//a[text()='Sign in']")).click();
            driver.findElement(By.name("Email")).sendKeys(userName);
            driver.findElement(By.name("Passwd")).sendKeys(password);
            driver.findElement(By.name("signIn")).click();
        } else {
		userName = "Default";
	}

        new WebDriverWait(driver, 300).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver webDriver) {
                return driver.getTitle().toLowerCase().equals("google");
            }
        });

        //Enter the search
        WebElement element = driver.findElement(By.name("q"));
        element.sendKeys(searchTerm);
        element.submit();

        // Wait for Google to load
        (new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {
            @Override public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().startsWith(searchTerm);
            }
        });

	//Read the first 5 pages
	int rank = 1;
	for (int i = 0; i < pages; i++) {
		if (i != 0) {
			//Wait for new page to laod
			final int nextPageNum = i+ 1;
			(new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
		        		return d.findElement(By.id("resultStats")).getText().contains("Page " + (nextPageNum));
				}
			});
		}
		//Find all result host elements 
		List<WebElement> resultElements = driver.findElements(By.xpath("//div[@class = 'srg']/li[@class = 'g']/div[@class = 'rc']/h3/a"));
		for (WebElement resultElement : resultElements) {
			Result result = new Result();			
			result.setUrl(resultElement.getAttribute("href"));
			result.setTitle(resultElement.getText());
			result.setResult(Integer.toString(rank));
			results.add(result);
			rank++;
		}
		//Go to the next page
		driver.findElement(By.id("pnnext")).click();
	}

	//Write elements to CSV
	String fileName = searchTerm + userName + ".csv";
	PrintWriter output = new PrintWriter(searchTerm + userName + ".csv", "UTF-8");
	output.println("Url;Title;Rank");
	for (Result result : results) {
		output.println(result.getUrl() + ";" + result.getTitle() + ";" + result.getResult());
	}
	output.close();
        driver.quit();
    }
}
