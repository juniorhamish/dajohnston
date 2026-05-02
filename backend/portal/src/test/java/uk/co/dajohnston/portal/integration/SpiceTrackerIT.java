package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticatedAsUser2;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpiceTrackerIT extends AbstractIntegrationTest {

  private String householdId;

  @BeforeEach
  void setUp() {
    householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", "Test Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
  }

  @Test
  void fullSpiceFlow() {
    // 1. Create a spice
    String spiceId =
        authenticated()
            .header("X-Household-Id", householdId)
            .contentType(JSON)
            .body(Map.of("name", "Cinnamon"))
            .when()
            .post("/api/spices")
            .then()
            .statusCode(201)
            .body("name", equalTo("Cinnamon"))
            .extract()
            .path("id");

    // 2. Add a full jar to pantry
    String jarId =
        authenticated()
            .header("X-Household-Id", householdId)
            .contentType(JSON)
            .body(Map.of("spiceId", spiceId))
            .when()
            .post("/api/pantry")
            .then()
            .statusCode(201)
            .body("spiceName", equalTo("Cinnamon"))
            .body("quantity", equalTo(100))
            .extract()
            .path("id");

    // 3. Add a partial jar to pantry
    authenticated()
        .header("X-Household-Id", householdId)
        .contentType(JSON)
        .body(Map.of("spiceId", spiceId, "quantity", 50))
        .when()
        .post("/api/pantry")
        .then()
        .statusCode(201)
        .body("quantity", equalTo(50));

    // 4. List pantry jars
    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .get("/api/pantry")
        .then()
        .statusCode(200)
        .body("jars", hasSize(2));

    // 5. Update jar quantity
    authenticated()
        .header("X-Household-Id", householdId)
        .contentType(JSON)
        .body(Map.of("quantity", 25))
        .when()
        .patch("/api/pantry/{id}", jarId)
        .then()
        .statusCode(200)
        .body("quantity", equalTo(25));

    // 6. Remove jar
    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .delete("/api/pantry/{id}", jarId)
        .then()
        .statusCode(204);

    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .get("/api/pantry")
        .then()
        .statusCode(200)
        .body("jars", hasSize(1));
  }

  @Test
  void householdIsolation() {
    // User 1 creates a spice
    String spiceId =
        authenticated()
            .header("X-Household-Id", householdId)
            .contentType(JSON)
            .body(Map.of("name", "Cumin"))
            .when()
            .post("/api/spices")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    // User 2 (in their own household) should not see User 1's spice
    String user2HouseholdId =
        authenticatedAsUser2()
            .contentType(JSON)
            .body(Map.of("name", "User 2 Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    authenticatedAsUser2()
        .header("X-Household-Id", user2HouseholdId)
        .when()
        .get("/api/spices")
        .then()
        .statusCode(200)
        .body("spices", hasSize(0));

    // User 2 should not be able to add a jar using User 1's spiceId
    authenticatedAsUser2()
        .header("X-Household-Id", user2HouseholdId)
        .contentType(JSON)
        .body(Map.of("spiceId", spiceId))
        .when()
        .post("/api/pantry")
        .then()
        .statusCode(
            404); // Should be 404 because spiceRepository.findById(spiceId) won't find it due to
    // RLS
  }
}
