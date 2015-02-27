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

public class FixOldCsvs {

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


        //Read from existing CSV if possible
        String fileName = "dummy.csv";
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

	int[] maxRank = {0,0,0,0,0,0,0};
	System.out.println("Max ranks");
	for (int maxRan : maxRank) {
		System.out.println(maxRan);
	}
        //Update exsting results that fell off top 50
        for (Result existingResult : existingResults) {
            for (int i = 0; i < existingResult.getRanks().size(); i++) {
		String rank = existingResult.getRanks().get(i);
		if (!rank.equals("N/A")) {
			int current = Integer.parseInt(rank);
			if (maxRank[i] < current) {
				maxRank[i] = current;
			}
		}
		}
        }

	System.out.println("Max ranks");
	for (Integer maxRan : maxRank) {
		System.out.println(maxRan);
	}

        for (Result existingResult : existingResults) {
		ArrayList<String> newRanks = new ArrayList<>();
            for (int i = 0; i < existingResult.getRanks().size(); i++) {
		String rank = existingResult.getRanks().get(i);
		if (!rank.equals("N/A")) {
			newRanks.add(Integer.toString(maxRank[i] - (Integer.parseInt(rank) - 1)));
		} else {
			newRanks.add("0");
		}
		}
		existingResult.setRanks(newRanks);
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

    }
}
