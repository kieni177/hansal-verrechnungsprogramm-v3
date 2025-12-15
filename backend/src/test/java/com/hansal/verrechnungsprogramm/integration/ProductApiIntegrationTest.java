package com.hansal.verrechnungsprogramm.integration;

import com.hansal.verrechnungsprogramm.model.Product;
import com.hansal.verrechnungsprogramm.repository.ProductRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Product API endpoints.
 * Tests the full stack from HTTP request to database.
 */
class ProductApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        super.setUpRestAssured();

        // Create a test product
        testProduct = new Product();
        testProduct.setName("Test Rindfleisch");
        testProduct.setDescription("Premium Test Rindfleisch");
        testProduct.setPrice(new BigDecimal("25.00"));
        testProduct.setMeatCutType("beef");
        testProduct.setStockQuantity(new BigDecimal("100.00"));
        testProduct = productRepository.save(testProduct);
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/products - Should return all products")
    void getAllProducts_ShouldReturnProducts() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].name", notNullValue());
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product by ID")
    void getProductById_ShouldReturnProduct() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products/" + testProduct.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(testProduct.getId().intValue()))
            .body("name", equalTo("Test Rindfleisch"))
            .body("price", equalTo(25.00f));
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by name")
    void searchProducts_ShouldReturnMatchingProducts() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("name", "Rind")
        .when()
            .get("/api/products/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].name", containsString("Rind"));
    }

    @Test
    @DisplayName("POST /api/products - Should create new product")
    void createProduct_ShouldReturnCreatedProduct() {
        String newProductJson = """
            {
                "name": "Neues Produkt",
                "description": "Beschreibung",
                "price": 30.00,
                "meatCutType": "pork"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(newProductJson)
        .when()
            .post("/api/products")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("name", equalTo("Neues Produkt"))
            .body("price", equalTo(30.00f));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update product")
    void updateProduct_ShouldReturnUpdatedProduct() {
        String updatedProductJson = """
            {
                "name": "Updated Rindfleisch",
                "description": "Updated Description",
                "price": 35.00,
                "meatCutType": "beef"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(updatedProductJson)
        .when()
            .put("/api/products/" + testProduct.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("name", equalTo("Updated Rindfleisch"))
            .body("price", equalTo(35.00f));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete product")
    void deleteProduct_ShouldReturnNoContent() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .delete("/api/products/" + testProduct.getId())
        .then()
            .statusCode(204);

        // Verify product is deleted
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products/" + testProduct.getId())
        .then()
            .statusCode(anyOf(is(404), is(500))); // Not found or error
    }

    @Test
    @DisplayName("GET /api/products/with-stock - Should return products with stock info")
    void getAllProductsWithStock_ShouldReturnProductsWithStock() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/products/with-stock")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].availableStock", notNullValue());
    }

    @Test
    @DisplayName("POST /api/products/bulk - Should create multiple products")
    void createBulkProducts_ShouldReturnCreatedProducts() {
        String bulkProductsJson = """
            [
                {"name": "Bulk Product 1", "price": 20.00},
                {"name": "Bulk Product 2", "price": 25.00}
            ]
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(bulkProductsJson)
        .when()
            .post("/api/products/bulk")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("$", hasSize(2));
    }
}
