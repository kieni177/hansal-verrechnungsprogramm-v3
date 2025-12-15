package com.hansal.verrechnungsprogramm.integration;

import com.hansal.verrechnungsprogramm.repository.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Full Workflow Integration Test.
 * Tests the complete business workflow at the API level:
 * 1. Create Products
 * 2. Create Order with Order Items
 * 3. Create Invoice from Order
 * 4. Update Order Status
 * 5. Download Invoice PDF
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullWorkflowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    // Shared IDs across tests
    private static Long productId1;
    private static Long productId2;
    private static Long orderId;
    private static Long invoiceId;

    @AfterAll
    static void cleanup(@Autowired InvoiceRepository invoiceRepo,
                        @Autowired OrderRepository orderRepo,
                        @Autowired ProductRepository productRepo) {
        invoiceRepo.deleteAll();
        orderRepo.deleteAll();
        productRepo.deleteAll();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Step 1: Create first product - Rindfleisch")
    void step1_CreateFirstProduct() {
        String productJson = """
            {
                "name": "Workflow Test Rindfleisch",
                "description": "Premium Rindfleisch für Workflow Test",
                "price": 28.50,
                "meatCutType": "beef",
                "stockQuantity": 100.00
            }
            """;

        Response response = given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(productJson)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Workflow Test Rindfleisch"))
            .body("price", equalTo(28.50f))
            .extract().response();

        productId1 = response.jsonPath().getLong("id");
        assertNotNull(productId1, "Product 1 ID should be assigned");
        System.out.println("Created Product 1 with ID: " + productId1);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Step 2: Create second product - Schweinefleisch")
    void step2_CreateSecondProduct() {
        String productJson = """
            {
                "name": "Workflow Test Schweinefleisch",
                "description": "Premium Schweinefleisch für Workflow Test",
                "price": 18.00,
                "meatCutType": "pork",
                "stockQuantity": 150.00
            }
            """;

        Response response = given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(productJson)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("Workflow Test Schweinefleisch"))
            .extract().response();

        productId2 = response.jsonPath().getLong("id");
        assertNotNull(productId2, "Product 2 ID should be assigned");
        System.out.println("Created Product 2 with ID: " + productId2);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Step 3: Verify products are listed")
    void step3_VerifyProductsExist() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(2)))
            .body("find { it.id == " + productId1 + " }.name", equalTo("Workflow Test Rindfleisch"))
            .body("find { it.id == " + productId2 + " }.name", equalTo("Workflow Test Schweinefleisch"));

        System.out.println("Both products verified in list");
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Step 4: Create order with customer details")
    void step4_CreateOrder() {
        String orderJson = """
            {
                "customerName": "Workflow Test Kunde",
                "customerPhone": "+43 660 9876543",
                "customerAddress": "Workflow Teststraße 42, 1010 Wien",
                "items": []
            }
            """;

        Response response = given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(orderJson)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("customerName", equalTo("Workflow Test Kunde"))
            .body("customerPhone", equalTo("+43 660 9876543"))
            .body("status", equalTo("PENDING"))
            .extract().response();

        orderId = response.jsonPath().getLong("id");
        assertNotNull(orderId, "Order ID should be assigned");
        System.out.println("Created Order with ID: " + orderId);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("Step 5: Verify order exists and has correct status")
    void step5_VerifyOrder() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/" + orderId)
        .then()
            .statusCode(200)
            .body("id", equalTo(orderId.intValue()))
            .body("customerName", equalTo("Workflow Test Kunde"))
            .body("status", equalTo("PENDING"));

        System.out.println("Order verified with PENDING status");
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("Step 6: Update order status to PROCESSING")
    void step6_UpdateOrderStatusToProcessing() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("status", "PROCESSING")
        .when()
            .patch("/api/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("PROCESSING"));

        System.out.println("Order status updated to PROCESSING");
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("Step 7: Update order status to COMPLETED")
    void step7_UpdateOrderStatusToCompleted() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("status", "COMPLETED")
        .when()
            .patch("/api/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));

        System.out.println("Order status updated to COMPLETED");
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("Step 8: Create invoice from order")
    void step8_CreateInvoiceFromOrder() {
        Response response = given()
            .auth().basic("testuser", "testpass")
        .when()
            .post("/api/invoices/from-order/" + orderId)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("invoiceNumber", notNullValue())
            .body("order.id", equalTo(orderId.intValue()))
            .body("status", equalTo("UNPAID"))
            .extract().response();

        invoiceId = response.jsonPath().getLong("id");
        String invoiceNumber = response.jsonPath().getString("invoiceNumber");

        assertNotNull(invoiceId, "Invoice ID should be assigned");
        assertNotNull(invoiceNumber, "Invoice number should be generated");

        System.out.println("Created Invoice with ID: " + invoiceId + ", Number: " + invoiceNumber);
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("Step 9: Verify invoice exists")
    void step9_VerifyInvoice() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/invoices/" + invoiceId)
        .then()
            .statusCode(200)
            .body("id", equalTo(invoiceId.intValue()))
            .body("order.customerName", equalTo("Workflow Test Kunde"))
            .body("status", equalTo("UNPAID"));

        System.out.println("Invoice verified successfully");
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("Step 10: Get invoice by order ID")
    void step10_GetInvoiceByOrderId() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/invoices/by-order/" + orderId)
        .then()
            .statusCode(200)
            .body("id", equalTo(invoiceId.intValue()))
            .body("order.id", equalTo(orderId.intValue()));

        System.out.println("Invoice retrieved by order ID");
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("Step 11: Download invoice PDF")
    void step11_DownloadInvoicePdf() {
        Response response = given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/invoices/" + invoiceId + "/pdf")
        .then()
            .statusCode(200)
            .contentType("application/pdf")
            .header("Content-Disposition", containsString("attachment"))
            .extract().response();

        byte[] pdfBytes = response.asByteArray();
        assertTrue(pdfBytes.length > 0, "PDF should have content");

        System.out.println("Invoice PDF downloaded, size: " + pdfBytes.length + " bytes");
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("Step 12: Update invoice status to PAID")
    void step12_UpdateInvoiceStatus() {
        // First, get the current invoice to have all required fields
        Response getResponse = given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/invoices/" + invoiceId)
        .then()
            .statusCode(200)
            .extract().response();

        // Extract current values
        String invoiceNumber = getResponse.jsonPath().getString("invoiceNumber");
        String issueDate = getResponse.jsonPath().getString("issueDate");
        String dueDate = getResponse.jsonPath().getString("dueDate");
        Float taxRate = getResponse.jsonPath().getFloat("taxRate");
        Integer orderIdFromInvoice = getResponse.jsonPath().getInt("order.id");

        // Build the full update JSON with required order reference
        String updateJson = String.format("""
            {
                "invoiceNumber": "%s",
                "issueDate": "%s",
                "dueDate": %s,
                "taxRate": %s,
                "status": "PAID",
                "notes": "Payment received via bank transfer",
                "order": { "id": %d }
            }
            """,
            invoiceNumber,
            issueDate,
            dueDate != null ? "\"" + dueDate + "\"" : "null",
            taxRate != null ? taxRate : "0",
            orderIdFromInvoice);

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(updateJson)
        .when()
            .put("/api/invoices/" + invoiceId)
        .then()
            .statusCode(200)
            .body("status", equalTo("PAID"))
            .body("notes", equalTo("Payment received via bank transfer"));

        System.out.println("Invoice status updated to PAID");
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("Step 13: Search for customer in orders")
    void step13_SearchOrders() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("customerName", "Workflow Test")
        .when()
            .get("/api/orders/search")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].customerName", containsString("Workflow Test"));

        System.out.println("Order search successful");
    }

    @Test
    @org.junit.jupiter.api.Order(14)
    @DisplayName("Step 14: Get unique customers list")
    void step14_GetUniqueCustomers() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/customers")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("find { it.name == 'Workflow Test Kunde' }", notNullValue());

        System.out.println("Unique customers list verified");
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    @DisplayName("Step 15: Get orders by status")
    void step15_GetOrdersByStatus() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/status/COMPLETED")
        .then()
            .statusCode(200)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].status", equalTo("COMPLETED"));

        System.out.println("Orders by status query successful");
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    @DisplayName("Step 16: Final verification - Complete workflow summary")
    void step16_WorkflowSummary() {
        // Get products count
        int productsCount = given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$").size();

        // Get orders count
        int ordersCount = given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$").size();

        // Get invoices count
        int invoicesCount = given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/invoices")
        .then()
            .statusCode(200)
            .extract().jsonPath().getList("$").size();

        System.out.println("\n========== WORKFLOW SUMMARY ==========");
        System.out.println("Products created: " + productsCount);
        System.out.println("Orders created: " + ordersCount);
        System.out.println("Invoices created: " + invoicesCount);
        System.out.println("Product 1 ID: " + productId1);
        System.out.println("Product 2 ID: " + productId2);
        System.out.println("Order ID: " + orderId);
        System.out.println("Invoice ID: " + invoiceId);
        System.out.println("=======================================\n");

        assertTrue(productsCount >= 2, "Should have at least 2 products");
        assertTrue(ordersCount >= 1, "Should have at least 1 order");
        assertTrue(invoicesCount >= 1, "Should have at least 1 invoice");
    }
}
