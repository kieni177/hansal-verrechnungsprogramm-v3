package com.hansal.verrechnungsprogramm.integration;

import com.hansal.verrechnungsprogramm.model.Slaughter;
import com.hansal.verrechnungsprogramm.repository.SlaughterRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Slaughter API endpoints.
 * Tests the full stack from HTTP request to database.
 */
class SlaughterApiIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private SlaughterRepository slaughterRepository;

    private Slaughter testSlaughter;

    @BeforeEach
    void setUp() {
        super.setUpRestAssured();

        // Create a test slaughter
        testSlaughter = new Slaughter();
        testSlaughter.setCowTag("AT-TEST-123456");
        testSlaughter.setCowId("COW-TEST-001");
        testSlaughter.setSlaughterDate(LocalDate.of(2024, 1, 15));
        testSlaughter.setTotalWeight(new BigDecimal("350.00"));
        testSlaughter.setNotes("Test slaughter notes");
        testSlaughter = slaughterRepository.save(testSlaughter);
    }

    @AfterEach
    void tearDown() {
        slaughterRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/slaughters - Should return all slaughters")
    void getAllSlaughters_ShouldReturnSlaughters() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/slaughters")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].cowTag", notNullValue());
    }

    @Test
    @DisplayName("GET /api/slaughters/{id} - Should return slaughter by ID")
    void getSlaughterById_ShouldReturnSlaughter() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .get("/api/slaughters/" + testSlaughter.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("id", equalTo(testSlaughter.getId().intValue()))
            .body("cowTag", equalTo("AT-TEST-123456"))
            .body("cowId", equalTo("COW-TEST-001"));
    }

    @Test
    @DisplayName("GET /api/slaughters/search - Should search slaughters by cow tag")
    void searchSlaughters_ShouldReturnMatchingSlaughters() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("cowTag", "AT-TEST")
        .when()
            .get("/api/slaughters/search")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].cowTag", containsString("AT-TEST"));
    }

    @Test
    @DisplayName("GET /api/slaughters/date-range - Should return slaughters in date range")
    void getSlaughtersByDateRange_ShouldReturnSlaughtersInRange() {
        given()
            .auth().basic("testuser", "testpass")
            .queryParam("startDate", "2024-01-01")
            .queryParam("endDate", "2024-12-31")
        .when()
            .get("/api/slaughters/date-range")
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("POST /api/slaughters - Should create new slaughter")
    void createSlaughter_ShouldReturnCreatedSlaughter() {
        String newSlaughterJson = """
            {
                "cowTag": "AT-NEW-789012",
                "cowId": "COW-NEW-002",
                "slaughterDate": "2024-02-20",
                "totalWeight": 400.00,
                "notes": "New slaughter"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(newSlaughterJson)
        .when()
            .post("/api/slaughters")
        .then()
            .statusCode(201)
            .contentType(ContentType.JSON)
            .body("id", notNullValue())
            .body("cowTag", equalTo("AT-NEW-789012"));
    }

    @Test
    @DisplayName("PUT /api/slaughters/{id} - Should update slaughter")
    void updateSlaughter_ShouldReturnUpdatedSlaughter() {
        String updatedSlaughterJson = """
            {
                "cowTag": "AT-TEST-123456-UPDATED",
                "cowId": "COW-TEST-001-UPDATED",
                "slaughterDate": "2024-01-15",
                "totalWeight": 360.00,
                "notes": "Updated notes"
            }
            """;

        given()
            .auth().basic("testuser", "testpass")
            .contentType(ContentType.JSON)
            .body(updatedSlaughterJson)
        .when()
            .put("/api/slaughters/" + testSlaughter.getId())
        .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("cowTag", equalTo("AT-TEST-123456-UPDATED"))
            .body("notes", equalTo("Updated notes"));
    }

    @Test
    @DisplayName("DELETE /api/slaughters/{id} - Should delete slaughter")
    void deleteSlaughter_ShouldReturnNoContent() {
        given()
            .auth().basic("testuser", "testpass")
        .when()
            .delete("/api/slaughters/" + testSlaughter.getId())
        .then()
            .statusCode(204);
    }
}
