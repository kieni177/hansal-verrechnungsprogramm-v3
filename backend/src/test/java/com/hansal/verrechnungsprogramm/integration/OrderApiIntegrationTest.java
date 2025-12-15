package com.hansal.verrechnungsprogramm.integration;

import com.hansal.verrechnungsprogramm.model.Order;
import com.hansal.verrechnungsprogramm.model.OrderStatus;
import com.hansal.verrechnungsprogramm.repository.OrderRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Order API endpoints.
 * Tests the full stack from HTTP request to database.
 */
class OrderApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        super.setUpRestAssured();

        // Create a test order
        testOrder = new Order();
        testOrder.setCustomerName("Test Kunde");
        testOrder.setCustomerPhone("+43 660 1234567");
        testOrder.setCustomerAddress("Teststraße 1, 1010 Wien");
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(new BigDecimal("150.00"));
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/orders - Should return all orders")
    void getAllOrders_ShouldReturnOrders() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].customerName", notNullValue());
    }

    @Test
    @DisplayName("GET /api/orders/{id} - Should return order by ID")
    void getOrderById_ShouldReturnOrder() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/" + testOrder.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(testOrder.getId().intValue()))
            .body("customerName", equalTo("Test Kunde"))
            .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/customers - Should return unique customers")
    void getUniqueCustomers_ShouldReturnCustomers() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/customers")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/orders/search - Should search orders by customer name")
    void searchOrders_ShouldReturnMatchingOrders() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("customerName", "Test")
        .when()
            .get("/api/orders/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].customerName", containsString("Test"));
    }

    @Test
    @DisplayName("GET /api/orders/status/{status} - Should return orders by status")
    void getOrdersByStatus_ShouldReturnOrdersWithStatus() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/orders/status/PENDING")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("POST /api/orders - Should create new order")
    void createOrder_ShouldReturnCreatedOrder() {
        String newOrderJson = """
            {
                "customerName": "Neuer Kunde",
                "customerPhone": "+43 660 9999999",
                "customerAddress": "Neue Straße 5, 1020 Wien"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(newOrderJson)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("customerName", equalTo("Neuer Kunde"))
            .body("status", equalTo("PENDING"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id} - Should update order")
    void updateOrder_ShouldReturnUpdatedOrder() {
        String updatedOrderJson = """
            {
                "customerName": "Updated Kunde",
                "customerPhone": "+43 660 8888888",
                "customerAddress": "Updated Straße 10, 1030 Wien"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(updatedOrderJson)
        .when()
            .put("/api/orders/" + testOrder.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("customerName", equalTo("Updated Kunde"));
    }

    @Test
    @DisplayName("PATCH /api/orders/{id}/status - Should update order status")
    void updateOrderStatus_ShouldReturnUpdatedOrder() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("status", "COMPLETED")
        .when()
            .patch("/api/orders/" + testOrder.getId() + "/status")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("status", equalTo("COMPLETED"));
    }

    @Test
    @DisplayName("DELETE /api/orders/{id} - Should delete order")
    void deleteOrder_ShouldReturnNoContent() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .delete("/api/orders/" + testOrder.getId())
        .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("Full order workflow - Create, Update Status, Complete")
    void orderWorkflow_ShouldWorkEndToEnd() {
        // 1. Create order
        String newOrderJson = """
            {
                "customerName": "Workflow Kunde",
                "customerPhone": "+43 660 1111111"
            }
            """;

        Integer orderId = given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(newOrderJson)
        .when()
            .post("/api/orders")
        .then()
            .statusCode(201)
            .body("status", equalTo("PENDING"))
            .extract()
            .path("id");

        // 2. Update status to PROCESSING
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("status", "PROCESSING")
        .when()
            .patch("/api/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("PROCESSING"));

        // 3. Update status to COMPLETED
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("status", "COMPLETED")
        .when()
            .patch("/api/orders/" + orderId + "/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("COMPLETED"));
    }
}
