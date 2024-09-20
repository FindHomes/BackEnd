package com.findhomes.findhomesbe.crawling;

import java.time.Duration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.core.har.Har;
import net.lightbody.bmp.core.har.HarEntry;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.http.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
@Slf4j
public class Crawling {
    private WebDriver driver = null;
    private WebDriverWait wait = null;
    private Actions actions = null;
    private WebElement preWaitingElement;
    private List<WebElement> preElements;
    private WebElement preElement;
    private BrowserMobProxy proxy;

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
        // Start the BrowserMob Proxy
        proxy = new BrowserMobProxyServer();
        proxy.setTrustAllServers(true);
        proxy.start(0); // Start on an available port

        // Configure Selenium to use this proxy
        Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        // Setup ChromeOptions
        ChromeOptions options = new ChromeOptions();
        options.setProxy(seleniumProxy);
        options.setAcceptInsecureCerts(true);
        options.addArguments("--ignore-certificate-errors");

        // Create ChromeDriver instance
        this.driver = new ChromeDriver(options);

        return this;
    }

    public void openUrl(String url) {
        this.driver.get(url);
    }

    public void openUrlNewTab(String url) {
        // 현재 창의 핸들 저장
        String originalWindow = driver.getWindowHandle();
        // 새로운 탭 열기
        WebDriver newTab = driver.switchTo().newWindow(WindowType.TAB);
        // 새로운 탭에서 URL 열기
        newTab.get(url);
        // 기존 창 닫기
        driver.switchTo().window(originalWindow).close();
        // 새로운 탭으로 포커스 이동 (탭이 2개 이상 있을 경우 마지막 탭으로 이동)
        for (String windowHandle : driver.getWindowHandles()) {
            driver.switchTo().window(windowHandle);
        }
        driver.get(url);
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
            log.error("[[{} thread - ERROR: TimeoutException(List) - Css: {}]]", Thread.currentThread(), selector);
        } catch (NoSuchElementException e) {
            log.error("[[{} thread - ERROR: No Element(List) - Css: {}]]", Thread.currentThread(), selector);
        }
        return null;
    }

    // Find Element
    public WebElement getElementByCssSelector(String selector) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            return driver.findElement(By.cssSelector(selector));
        } catch (TimeoutException e) {
            log.error("[[{} thread - ERROR: TimeoutException - Css: {}]]", Thread.currentThread(), selector);
        } catch (NoSuchElementException e) {
            log.error("[[{} thread - ERROR: No Element - Css: {}]]", Thread.currentThread(), selector);
        }
        return null;
    }

    // 요소 text 가져오기
    public String getTextByCssSelector(String selector) {
        try {
            this.wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(selector)));
            return driver.findElement(By.cssSelector(selector)).getText();
        } catch (TimeoutException e) {
            log.error("[[{} thread - ERROR: TimeoutException(Text) - Css: {}]]", Thread.currentThread(), selector);
        } catch (NoSuchElementException e) {
            log.error("[[{} thread - ERROR: No Element(Text) - Css: {}]]", Thread.currentThread(), selector);
        }
        return null;
    }

    // iframe 변경하기
    public void changeIframe(String iframeId) {
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(By.id(iframeId)));
    }
}
