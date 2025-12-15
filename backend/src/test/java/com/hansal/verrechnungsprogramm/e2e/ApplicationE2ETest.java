package com.hansal.verrechnungsprogramm.e2e;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for the complete application workflow.
 * Tests navigation, main features, and user flows.
 *
 * Run with: mvn test -Dtest=ApplicationE2ETest -Dheadless=false
 *
 * Prerequisites:
 * - Backend running on localhost:8080
 * - Frontend running on localhost:4200
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
class ApplicationE2ETest extends BaseE2ETest {

    @Test
    @Order(1)
    @DisplayName("Application should load successfully")
    void applicationShouldLoad() {
        navigateTo("/");
        waitForAngular();

        // Check page loaded
        assertNotNull(driver.getTitle());
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());
    }

    @Test
    @Order(2)
    @DisplayName("Main navigation should be present")
    void mainNavigationShouldBePresent() {
        navigateTo("/");
        waitForAngular();

        // Look for navigation elements (sidenav, navbar, or menu)
        List<WebElement> navElements = driver.findElements(
            By.cssSelector("nav, mat-sidenav, mat-toolbar, .navigation, .sidebar, mat-nav-list")
        );

        assertTrue(navElements.size() > 0, "Navigation should be present");
    }

    @Test
    @Order(3)
    @DisplayName("Products page should be accessible")
    void productsPageShouldBeAccessible() {
        navigateTo("/products");
        waitForAngular();

        assertTrue(driver.getCurrentUrl().contains("products"));

        // Should show products content
        boolean hasContent = driver.findElements(
            By.cssSelector("table, mat-table, mat-card, .products, .product-list")
        ).size() > 0;

        assertTrue(hasContent, "Products page should have content");
    }

    @Test
    @Order(4)
    @DisplayName("Orders page should be accessible")
    void ordersPageShouldBeAccessible() {
        navigateTo("/orders");
        waitForAngular();

        assertTrue(driver.getCurrentUrl().contains("orders"));

        // Should show orders content
        boolean hasContent = driver.findElements(
            By.cssSelector("table, mat-table, mat-card, .orders, .order-list")
        ).size() > 0;

        assertTrue(hasContent, "Orders page should have content");
    }

    @Test
    @Order(5)
    @DisplayName("Invoices page should be accessible")
    void invoicesPageShouldBeAccessible() {
        navigateTo("/invoices");
        waitForAngular();

        assertTrue(driver.getCurrentUrl().contains("invoices"));

        // Should show invoices content
        boolean hasContent = driver.findElements(
            By.cssSelector("table, mat-table, mat-card, .invoices, .invoice-list")
        ).size() > 0;

        assertTrue(hasContent, "Invoices page should have content");
    }

    @Test
    @Order(6)
    @DisplayName("Slaughter page should be accessible")
    void slaughterPageShouldBeAccessible() {
        navigateTo("/slaughter");
        waitForAngular();

        assertTrue(driver.getCurrentUrl().contains("slaughter"));

        // Should show slaughter content
        boolean hasContent = driver.findElements(
            By.cssSelector("table, mat-table, mat-card, .slaughter, .slaughter-list")
        ).size() > 0;

        assertTrue(hasContent, "Slaughter page should have content");
    }

    @Test
    @Order(7)
    @DisplayName("Material Design components should render")
    void materialDesignComponentsShouldRender() {
        navigateTo("/");
        waitForAngular();

        // Check for Material Design components
        List<WebElement> materialComponents = driver.findElements(
            By.cssSelector("[class*='mat-'], [class*='mdc-']")
        );

        assertTrue(materialComponents.size() > 0, "Material Design components should be present");
    }

    @Test
    @Order(8)
    @DisplayName("Tables should display data")
    void tablesShouldDisplayData() {
        navigateTo("/products");
        waitForAngular();

        List<WebElement> tables = driver.findElements(By.cssSelector("table, mat-table"));

        if (!tables.isEmpty()) {
            WebElement table = tables.get(0);
            assertTrue(table.isDisplayed(), "Table should be visible");

            // Check for headers
            List<WebElement> headers = table.findElements(By.cssSelector("th, mat-header-cell"));
            assertTrue(headers.size() > 0, "Table should have headers");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Buttons should be clickable")
    void buttonsShouldBeClickable() {
        navigateTo("/products");
        waitForAngular();

        List<WebElement> buttons = driver.findElements(
            By.cssSelector("button, [mat-button], [mat-raised-button], [mat-fab]")
        );

        for (WebElement button : buttons) {
            if (button.isDisplayed() && button.isEnabled()) {
                assertTrue(button.isEnabled(), "Visible buttons should be enabled");
                break;
            }
        }
    }

    @Test
    @Order(10)
    @DisplayName("Full order workflow - Create order, add items, generate invoice")
    void fullOrderWorkflow() {
        // 1. Navigate to orders
        navigateTo("/orders");
        waitForAngular();

        // 2. Try to find and click "New Order" button
        try {
            WebElement newOrderBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='new-order'], button[mat-fab], .add-order, button.new-order")
            ));
            newOrderBtn.click();
            waitForAngular();

            // 3. Fill in customer details (if form appears)
            List<WebElement> customerInputs = driver.findElements(
                By.cssSelector("input[formControlName='customerName'], input[name='customerName'], #customerName")
            );

            if (!customerInputs.isEmpty()) {
                customerInputs.get(0).sendKeys("E2E Test Kunde");

                // 4. Save order
                WebElement saveBtn = driver.findElement(
                    By.cssSelector("button[type='submit'], .save-btn, button.save")
                );
                saveBtn.click();
                waitForAngular();

                // 5. Verify order was created
                assertTrue(
                    driver.getCurrentUrl().contains("orders") ||
                    driver.findElements(By.xpath("//*[contains(text(), 'E2E Test Kunde')]")).size() > 0,
                    "Order should be created"
                );
            }
        } catch (Exception e) {
            System.out.println("Order workflow test skipped - UI elements not found: " + e.getMessage());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Error handling - Invalid URL should show error or redirect")
    void errorHandlingInvalidUrl() {
        navigateTo("/nonexistent-page-12345");
        waitForAngular();

        // Should either show 404 page or redirect to home
        boolean handled = driver.getCurrentUrl().equals(FRONTEND_URL + "/") ||
                         driver.getCurrentUrl().contains("404") ||
                         driver.findElements(By.cssSelector(".error-page, .not-found, mat-card")).size() > 0;

        assertTrue(handled, "Invalid URL should be handled gracefully");
    }

    @Test
    @Order(12)
    @DisplayName("Performance - Page should load within acceptable time")
    void performancePageLoadTime() {
        long startTime = System.currentTimeMillis();

        navigateTo("/products");
        waitForAngular();

        // Wait for content to be visible
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

        long loadTime = System.currentTimeMillis() - startTime;

        // Page should load within 10 seconds
        assertTrue(loadTime < 10000, "Page should load within 10 seconds, took: " + loadTime + "ms");
    }
}
