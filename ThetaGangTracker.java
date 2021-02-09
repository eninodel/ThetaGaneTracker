// Edwin Nino Delgado
// 2/6/2021
// 
// This program web scrapes the trades of a specific user
// on thetagang.com every minute and sends me an email if
// the user logs a new trade. Made to practice Selenium.


import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.WebElement;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.mail.*;
import javax.mail.internet.*; 
import javax.mail.Session; 
import javax.mail.Transport;
import javax.activation.*;
import javax.mail.Authenticator;

public class ThetaGangTracker {
    public static void main(String[] args) throws Exception {
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\bluep\\programming\\Java_projects\\Selenium\\lib\\chromedriver.exe");
        WebDriver driver = new ChromeDriver(chromeOptions);
        String lastStoredTrade = "initialize";
        while (true){
            ArrayList<WebElement> trades = getData(driver);
            checkForNewTrades(trades, lastStoredTrade);
            TimeUnit.MINUTES.sleep(1);
        }
    }

    // This method is the main Selenium portion of the script.
    // It gets the website data and returns the last trade logged.
    public static ArrayList<WebElement> getData(WebDriver driver){
        driver.get("https://thetagang.com/DoughGoat");
        List<WebElement> waitTime = new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("td")));            
        List<WebElement> elements = driver.findElements(By.cssSelector("td"));
        ArrayList<WebElement> trades = new ArrayList<WebElement>();
        WebElement individualTicker = elements.get(3);
        WebElement strategy = elements.get(4);
        WebElement strikes = elements.get(5);
        trades.add(individualTicker);
        trades.add(strategy);
        trades.add(strikes);
        return trades;
    }

    // This method sends the email to notify me if a new trade has been logged.
    public static void sendEmail(String ticker, String strategy, String strikes){
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", true);
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        String recipient = "edninodel@gmail.com";
        String sender = "edninodel@gmail.com";
        Session session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(sender, "");
            }
        });
        try{
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject("New Trade");
            message.setText("New trade by DoughGoat. Ticker: " + ticker + " Strategy: " + strategy + " Strike(s): " + strikes);
            Transport.send(message);
            System.out.println("mailsent");
        } catch (MessagingException mex){
            mex.printStackTrace();
        }
    }

    // This method takes in the data returned by the getData method
    // and decides wether or not the data represents a new trade.
    public static void checkForNewTrades(ArrayList<WebElement> trades, String lastStoredTrade){
        String lastTradeStrike = trades.get(2).getText();
        if (!lastTradeStrike.equals(lastStoredTrade)){
            lastStoredTrade = lastTradeStrike;
            String strategy = trades.get(1).getText();
            String ticker = trades.get(0).getText();
            sendEmail(ticker, strategy, lastTradeStrike);
        }
    }
}


