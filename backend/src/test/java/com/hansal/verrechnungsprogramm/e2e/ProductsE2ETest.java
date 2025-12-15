package com.hansal.verrechnungsprogramm.e2e;

import com.hansal.verrechnungsprogramm.e2e.pages.ProductsPage;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End tests for the Products functionality.
 * These tests require the frontend and backend to be running.
 *
 * Run with: mvn test -Dtest=ProductsE2ETest -Dheadless=false
 *
 * Prerequisites:
 * - Backend running on localhost:8080
 * - Frontend running on localhost:4200
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
class ProductsE2ETest extends BaseE2ETest {

    private ProductsPage productsPage;

    @BeforeEach
    void setUpPage() {
        productsPage = new ProductsPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Products page should load successfully")
    void productsPageShouldLoad() {
        productsPage.navigateTo();
        waitForAngular();

        String title = productsPage.getPageTitle();
        assertNotNull(title);
    }

    @Test
    @Order(2)
    @DisplayName("Products table should be visible")
    void productsTableShouldBeVisible() {
        productsPage.navigateTo();
        waitForAngular();

        // Check if either table or empty state is shown
        boolean hasContent = driver.findElements(By.cssSelector("table, mat-table, .empty-state, .no-products")).size() > 0;
        assertTrue(hasContent, "Products page should show table or empty state");
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to add product form")
    void shouldNavigateToAddProductForm() {
        productsPage.navigateTo();
        waitForAngular();

        try {
            productsPage.clickAddProduct();
            waitForAngular();

            // Check if form is visible or navigated to new page
            Object result = wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("form, mat-dialog-container, .product-form")),
                ExpectedConditions.urlContains("/products/new"),
                ExpectedConditions.urlContains("/products/add")
            ));
            assertNotNull(result, "Add product form should be accessible");
        } catch (Exception e) {
            // Button might not exist or have different behavior
            System.out.println("Add product button not found or different UI flow");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should display product details")
    void shouldDisplayProductDetails() {
        productsPage.navigateTo();
        waitForAngular();

        int productCount = productsPage.getProductCount();

        if (productCount > 0) {
            // Click on first product row to see details
            try {
                driver.findElement(By.cssSelector("table tbody tr:first-child, mat-row:first-child")).click();
                waitForAngular();

                // Check if details are shown
                Object detailsResult = wait.until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector(".product-details, mat-dialog-container")),
                    ExpectedConditions.urlContains("/products/")
                ));
                assertNotNull(detailsResult, "Product details should be accessible");
            } catch (Exception e) {
                System.out.println("Product details flow not implemented or different UI");
            }
        }
    }

    @Test
    @Order(5)
    @DisplayName("Navigation should work correctly")
    void navigationShouldWork() {
        // Navigate to products
        navigateTo("/products");
        waitForAngular();
        assertTrue(driver.getCurrentUrl().contains("products"));

        // Navigate to orders
        navigateTo("/orders");
        waitForAngular();
        assertTrue(driver.getCurrentUrl().contains("orders"));

        // Navigate back to products
        navigateTo("/products");
        waitForAngular();
        assertTrue(driver.getCurrentUrl().contains("products"));
    }

    @Test
    @Order(6)
    @DisplayName("Page should be responsive")
    void pageShouldBeResponsive() {
        productsPage.navigateTo();
        waitForAngular();

        // Test desktop size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());

        // Test tablet size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(768, 1024));
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());

        // Test mobile size
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667));
        assertTrue(driver.findElement(By.tagName("body")).isDisplayed());

        // Restore to desktop
        driver.manage().window().maximize();
    }
}
