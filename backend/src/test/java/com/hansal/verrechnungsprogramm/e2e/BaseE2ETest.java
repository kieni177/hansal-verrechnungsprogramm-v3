package com.hansal.verrechnungsprogramm.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class for Selenium E2E tests.
 * Provides WebDriver setup and teardown functionality.
 */
public abstract class BaseE2ETest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    // Configure these based on your environment
    protected static final String FRONTEND_URL = "http://localhost:4200";
    protected static final String BACKEND_URL = "http://localhost:8080";
    protected static final int WAIT_TIMEOUT_SECONDS = 10;

    @BeforeAll
    static void setupWebDriverManager() {
        // Try Chrome first, fallback to Firefox
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception e) {
            WebDriverManager.firefoxdriver().setup();
        }
    }

    @BeforeEach
    void setUp() {
        driver = createWebDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Creates a WebDriver instance.
     * Tries Chrome first (headless), falls back to Firefox.
     */
    private WebDriver createWebDriver() {
        String browserType = System.getProperty("browser", "chrome");

        try {
            if ("firefox".equalsIgnoreCase(browserType)) {
                return createFirefoxDriver();
            } else {
                return createChromeDriver();
            }
        } catch (Exception e) {
            // Fallback to Firefox if Chrome fails
            return createFirefoxDriver();
        }
    }

    private WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();

        // Run headless by default for CI/CD
        if (!"false".equalsIgnoreCase(System.getProperty("headless"))) {
            options.addArguments("--headless=new");
        }

        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        return new ChromeDriver(options);
    }

    private WebDriver createFirefoxDriver() {
        FirefoxOptions options = new FirefoxOptions();

        // Run headless by default for CI/CD
        if (!"false".equalsIgnoreCase(System.getProperty("headless"))) {
            options.addArguments("--headless");
        }

        options.addArguments("--width=1920");
        options.addArguments("--height=1080");

        return new FirefoxDriver(options);
    }

    /**
     * Navigate to a page relative to the frontend URL.
     */
    protected void navigateTo(String path) {
        driver.get(FRONTEND_URL + path);
    }

    /**
     * Wait for Angular to finish loading.
     */
    protected void waitForAngular() {
        try {
            Thread.sleep(500); // Brief pause for Angular
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
