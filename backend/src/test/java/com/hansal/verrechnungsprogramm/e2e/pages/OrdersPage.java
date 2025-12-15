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
 * Page Object for the Orders page.
 */
public class OrdersPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(css = "[data-testid='orders-table'], table, mat-table")
    private WebElement ordersTable;

    @FindBy(css = "[data-testid='new-order-btn'], button[mat-fab], .add-button, .new-order")
    private WebElement newOrderButton;

    @FindBy(css = "input[formControlName='customerName'], input[name='customerName'], #customerName")
    private WebElement customerNameInput;

    @FindBy(css = "input[formControlName='customerPhone'], input[name='customerPhone'], #customerPhone")
    private WebElement customerPhoneInput;

    @FindBy(css = "input[formControlName='customerAddress'], textarea[formControlName='customerAddress']")
    private WebElement customerAddressInput;

    @FindBy(css = "button[type='submit'], .save-btn")
    private WebElement saveButton;

    public OrdersPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void navigateTo() {
        driver.get("http://localhost:4200/orders");
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        wait.until(ExpectedConditions.or(
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("table")),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector("mat-table")),
            ExpectedConditions.presenceOfElementLocated(By.cssSelector(".orders-container"))
        ));
    }

    public boolean isOrdersTableVisible() {
        try {
            return ordersTable.isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getOrderCount() {
        try {
            List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr, mat-row"));
            return rows.size();
        } catch (Exception e) {
            return 0;
        }
    }

    public void clickNewOrder() {
        try {
            newOrderButton.click();
        } catch (Exception e) {
            WebElement btn = driver.findElement(By.cssSelector("button[mat-fab], .fab-button"));
            btn.click();
        }
    }

    public void enterCustomerName(String name) {
        wait.until(ExpectedConditions.visibilityOf(customerNameInput));
        customerNameInput.clear();
        customerNameInput.sendKeys(name);
    }

    public void enterCustomerPhone(String phone) {
        try {
            wait.until(ExpectedConditions.visibilityOf(customerPhoneInput));
            customerPhoneInput.clear();
            customerPhoneInput.sendKeys(phone);
        } catch (Exception ignored) {
        }
    }

    public void enterCustomerAddress(String address) {
        try {
            wait.until(ExpectedConditions.visibilityOf(customerAddressInput));
            customerAddressInput.clear();
            customerAddressInput.sendKeys(address);
        } catch (Exception ignored) {
        }
    }

    public void clickSave() {
        saveButton.click();
    }

    public void createOrder(String customerName, String phone, String address) {
        clickNewOrder();
        enterCustomerName(customerName);
        enterCustomerPhone(phone);
        enterCustomerAddress(address);
        clickSave();
    }

    public boolean isOrderInList(String customerName) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//td[contains(text(), '" + customerName + "')] | //mat-cell[contains(text(), '" + customerName + "')]")
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void openOrder(String customerName) {
        WebElement row = driver.findElement(
            By.xpath("//tr[td[contains(text(), '" + customerName + "')]] | //mat-row[mat-cell[contains(text(), '" + customerName + "')]]")
        );
        row.click();
    }

    public String getOrderStatus(String customerName) {
        try {
            WebElement row = driver.findElement(
                By.xpath("//tr[td[contains(text(), '" + customerName + "')]] | //mat-row[mat-cell[contains(text(), '" + customerName + "')]]")
            );
            WebElement statusCell = row.findElement(By.cssSelector("[data-testid='status'], .status, mat-chip"));
            return statusCell.getText();
        } catch (Exception e) {
            return "";
        }
    }
}
