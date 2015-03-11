package cs7270.personalizedSearch;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PersonalizedSearchBot {

    private static class Result {
        private String url = "";
        private String title = "";
        private List<String> ranks = new ArrayList<String>();
        private String relevance = "";

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

        public List<String> getRanks() {
            return ranks;
        }

        public void setRanks(final List<String> ranks) {
            this.ranks = ranks;
        }

        public void addRank(final String rank) {
            this.ranks.add(rank);
        }

        public String getRelevance() {
            return relevance;
        }

        public void setRelevance(final String relevance) {
            this.relevance = relevance;
        }
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        final int pages = 5;
        List<Result> results = new ArrayList<Result>();

        //Get the search, username and password
        final String searchTerm = JOptionPane.showInputDialog(null, "Enter search term");
        final boolean usePersonalizedSearch =
                JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(
                        null, "Use personalized search?", "Use personalized search?",
                        JOptionPane.YES_NO_OPTION
                );


        // Firefox driver since Chrome driver requires extra work
        final WebDriver driver = new FirefoxDriver();

        //Go to google
        driver.get("http://www.google.com");

        String suffix = "NonPersonalized";
        if (usePersonalizedSearch) {
            driver.findElement(By.xpath("//a[text()='Sign in']")).click();
            suffix = "Personalized";
        }

        // Wait for sign in and any 2-fac
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
            public Boolean apply(WebDriver d) {
                return d.getTitle().startsWith(searchTerm)
                        && d.findElement(By.id("resultStats")).getText().contains("About");
            }
        });

        //Read the first 5 pages
        int rank = 1;
        for (int i = 0; i < pages; i++) {
            if (i != 0) {
                //Wait for new page to load
                final int nextPageNum = i + 1;
                (new WebDriverWait(driver, 30)).until(new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver d) {
			try {
                        	return d.findElement(By.id("resultStats")).getText().contains("Page " + (nextPageNum));
			} catch (Exception e) {
				return false;			
			}
                    }
                });
            }
            //Find all result host elements
            List<WebElement> resultElements = driver.findElements(By.xpath("//div[@class = 'srg']/li[@class = 'g']/div[@class = 'rc']/h3/a"));
            for (WebElement resultElement : resultElements) {
                Result result = new Result();
                result.setUrl(resultElement.getAttribute("href").trim());
                result.setTitle(resultElement.getText());
                result.addRank(Integer.toString(rank));
                results.add(result);
                rank++;
            }

            //Go to the next page
            WebElement nextLink = driver.findElement(By.id("pnnext"));
            nextLink.click();
        }

        //Read from existing CSV if possible
        String fileName = searchTerm + suffix + ".csv";
        List<Result> existingResults = new ArrayList<Result>();
        int ranks = 0;
        boolean firstResult = true;
        List<String> titles = new ArrayList<String>();
        try {
            for (String line : Files.readAllLines(Paths.get(fileName), Charset.forName("UTF-8"))) {
                if (firstResult) {
                    //Line titles
                    firstResult = false;
                    for (String part : line.split(";")) {
                        titles.add(part);
                    }
                    titles.add(dateFormat.format(new Date()));
                } else {
                    //Read line of existing results
                    Result result = new Result();
                    String[] parts = line.split(";");
                    result.setRelevance(parts[0]);
                    result.setUrl(parts[1]);
                    result.setTitle(parts[2]);
                    for (int i = 3; i < parts.length; i++) {
                        result.addRank(parts[i]);
                    }
                    if (ranks == 0) {
                        ranks = parts.length - 3;
                    }
                    existingResults.add(result);
                }
            }
        } catch (NoSuchFileException e) {
            //Create new titles
            titles.add("Relevance");
            titles.add("Url");
            titles.add("Title");
            titles.add(dateFormat.format(new Date()));
        }
        //Add current results as necessary
        for (Result result : results) {
            boolean resultFound = false;
            for (Result existingResult : existingResults) {
                if (existingResult.getUrl().equals(result.getUrl())) {
                    //Update existing result
                    resultFound = true;
                    existingResult.addRank(Integer.toString(results.size() - (Integer.parseInt(result.getRanks().get(0)) - 1)));
                    break;
                }
            }
            if (!resultFound) {
                //Add new results
                String newRank = Integer.toString(results.size() - (Integer.parseInt(result.getRanks().get(0)) - 1));
                result.setRanks(new ArrayList<String>());
                for (int i = 0; i < ranks; i++) {
                    result.addRank("0");
                }
                result.addRank(newRank);
                existingResults.add(result);
            }
        }

        //Update exsting results that fell off top 50
        for (Result existingResult : existingResults) {
            if (existingResult.getRanks().size() < (ranks + 1)) {
                existingResult.addRank("0");
            }
        }

        //Write elements to CSV
        PrintWriter output = new PrintWriter(fileName, "UTF-8");
        for (String title : titles) {
            output.print(title + ";");
        }
        output.println("");
        for (Result result : existingResults) {
            String line = result.getRelevance() + ";" + result.getUrl() + ";" + result.getTitle() + ";";
            for (String rankString : result.getRanks()) {
                line = line + rankString + ";";
            }
            output.println(line);
        }
        output.close();
        driver.quit();
    }
}
