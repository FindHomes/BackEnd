package com.findhomes.findhomesbe.crawling;

import java.time.Duration;

import lombok.Getter;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

@Getter
public class Crawling {
    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private Actions actions = null;
    private WebElement preWaitingElement;
    private List<WebElement> preElements;
    private WebElement preElement;
    private final BrowserMobProxy proxy = new BrowserMobProxyServer();

    // driver 설정
    public Crawling setDriverAtServer() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
//        options.addArguments("--no-sandbox");
//        options.addArguments("--disable-dev-shm-usage");
//        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
//        options.addArguments("--disable-software-rasterizer");
//        options.addArguments("--remote-debugging-port=9222");

        this.driver = new ChromeDriver(options);
        return this;
    }
    public Crawling setDriverWithShowing() {
        this.driver = new ChromeDriver();

        return this;
    }

    public void openUrl(String url) {
        this.driver.get(url);
    }

    public void closeDriver() {
        this.driver.close();
    }

    public void quitDriver() {
        this.driver.quit();
    }

    // action 설정
    public Crawling setAction() {
        if (driver == null) {
            System.out.println("error: driver 설정 안됐음.");
            throw new RuntimeException();
        }
        this.actions = new Actions(this.driver);
        return this;
    }

    // wait 시간(초) 설정
    public Crawling setWaitTime(int sec) {
        try {
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(sec));
        } catch (TimeoutException e) {
            System.out.println("ERROR: 시간 초과 - " + e.getLocalizedMessage());
        }
        return this;
    }

    // 요소 기다리기 설정
    public WebElement getWaitingElementByCssSelector(String selector) {
        try {
            return this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: No Element(Wait) Css: " + selector + " - " + e.getLocalizedMessage());
        }
        return null;
    }

    // Find Elements
    public List<WebElement> getElementListByCssSelector(String selector) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            return driver.findElements(By.cssSelector(selector));
        } catch (TimeoutException e) {
            System.out.println("ERROR: 시간 초과(List) - " + e.getLocalizedMessage());
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: No Element(List) Css: " + selector + " - " + e.getLocalizedMessage());
        }
        return null;
    }

    // Find Element
    public WebElement getElementByCssSelector(String selector) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            return driver.findElement(By.cssSelector(selector));
        } catch (TimeoutException e) {
            System.out.println("ERROR: 시간 초과(Element) - " + e.getLocalizedMessage());
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: No Element Css: " + selector + " - " + e.getLocalizedMessage());
        }
        return null;
    }

    public WebElement getElementByTag(By tag) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(tag));
            return driver.findElement(tag);
        } catch (TimeoutException e) {
            System.out.println("ERROR: 시간 초과(Element) - " + e.getLocalizedMessage());
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: No Element Css: " + tag + " - " + e.getLocalizedMessage());
        }
        return null;
    }

    // 요소 text 가져오기
    public String getTextByCssSelector(String selector) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            return driver.findElement(By.cssSelector(selector)).getText();
        } catch (TimeoutException e) {
            System.out.println("ERROR: 시간 초과(Text) - " + e.getLocalizedMessage());
        } catch (NoSuchElementException e) {
            System.out.println("ERROR: No Element(Text) Css: " + selector + " - " + e.getLocalizedMessage());
        }
        return null;
    }
}
