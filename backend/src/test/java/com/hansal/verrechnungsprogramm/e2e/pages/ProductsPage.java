package com.hansal.verrechnungsprogramm.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Page Object for the Products page.
 */
public class ProductsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Page elements
    @FindBy(css = "[data-testid='products-table'], table, mat-table")
    private WebElement productsTable;

    @FindBy(css = "[data-testid='add-product-btn'], button[mat-fab], .add-button")
    private WebElement addProductButton;

    @FindBy(css = "[data-testid='search-input'], input[type='search'], .search-input")
    private WebElement searchInput;

    @FindBy(css = "[data-testid='product-name-input'], input[formControlName='name']")
    private WebElement productNameInput;

    @FindBy(css = "[data-testid='product-price-input'], input[formControlName='price']")
    private WebElement productPriceInput;

    @FindBy(css = "[data-testid='save-product-btn'], button[type='submit']")
    private WebElement saveProductButton;

    public ProductsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:4200/products");
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("mat-table")),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".products-container"))
        ));
    }

    public boolean isProductsTableVisible() {
        try {
            return productsTable.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getProductCount() {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr, mat-row"));
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickAddProduct() {
        try {
            addProductButton.click();
        } catch (Exception e) {
            // Try alternative selector
            WebElement btn = driver.findElement(By.cssSelector("button[mat-fab], .fab-button, button.add"));
            btn.click();
        }
    }

    public void enterProductName(String name) {
        wait.until(ExpectedConditions.visibilityOf(productNameInput));
        productNameInput.clear();
        productNameInput.sendKeys(name);
    }

    public void enterProductPrice(String price) {
        wait.until(ExpectedConditions.visibilityOf(productPriceInput));
        productPriceInput.clear();
        productPriceInput.sendKeys(price);
    }

    public void clickSave() {
        saveProductButton.click();
    }

    public void searchProduct(String query) {
        try {
            searchInput.clear();
            searchInput.sendKeys(query);
        } catch (Exception e) {
            // Search might not be available
        }
    }

    public boolean isProductInList(String productName) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(text(), '" + productName + "')] | //mat-cell[contains(text(), '" + productName + "')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteProduct(String productName) {
        try {
            WebElement row = driver.findElement(
                By.xpath("//tr[td[contains(text(), '" + productName + "')]] | //mat-row[mat-cell[contains(text(), '" + productName + "')]]")
            );
            WebElement deleteBtn = row.findElement(By.cssSelector("[data-testid='delete-btn'], button.delete, button[color='warn']"));
            deleteBtn.click();

            // Confirm deletion if dialog appears
            try {
                WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("[data-testid='confirm-delete'], button.confirm, .mat-dialog-actions button:last-child")
                ));
                confirmBtn.click();
            } catch (Exception ignored) {
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not delete product: " + productName, e);
        }
    }

    public String getPageTitle() {
        try {
            return driver.findElement(By.cssSelector("h1, h2, .page-title, mat-card-title")).getText();
        } catch (Exception e) {
            return driver.getTitle();
        }
    }
}
