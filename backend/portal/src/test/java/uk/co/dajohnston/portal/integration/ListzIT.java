package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListzIT extends AbstractIntegrationTest {

  private String householdId;

  @BeforeEach
  void setUp() {
    householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", "Listz Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");
  }

  @Test
  void fullTemplateFlow() {
    // 1. Create a template
    String templateId =
        authenticated()
            .header("X-Household-Id", householdId)
            .contentType(JSON)
            .body(Map.of("name", "Holiday Packing"))
            .when()
            .post("/api/v1/listz/templates")
            .then()
            .statusCode(201)
            .body("name", equalTo("Holiday Packing"))
            .extract()
            .path("id");

    // 2. Add an item to the template
    authenticated()
        .header("X-Household-Id", householdId)
        .contentType(JSON)
        .body(
            Map.of(
                "itemName",
                "Socks",
                "quantityRuleType",
                "PER_DAY",
                "quantityRuleValue",
                1.0,
                "categoryOverride",
                "Clothing"))
        .when()
        .post("/api/v1/listz/templates/{id}/items", templateId)
        .then()
        .statusCode(201)
        .body("name", equalTo("Socks"))
        .body("quantityRuleType", equalTo("PER_DAY"))
        .body("quantityRuleValue", equalTo(1.0f));

    // 3. Get template details
    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .get("/api/v1/listz/templates/{id}", templateId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Holiday Packing"))
        .body("items", hasSize(1))
        .body("items[0].name", equalTo("Socks"));

    // 4. Search for items in catalog
    authenticated()
        .header("X-Household-Id", householdId)
        .queryParam("q", "soc")
        .when()
        .get("/api/v1/listz/items/search")
        .then()
        .statusCode(200)
        .body("items", hasSize(1))
        .body("items[0].name", equalTo("Socks"));

    // 5. Update template name
    authenticated()
        .header("X-Household-Id", householdId)
        .contentType(JSON)
        .body(Map.of("name", "Summer Holiday"))
        .when()
        .put("/api/v1/listz/templates/{id}", templateId)
        .then()
        .statusCode(200)
        .body("name", equalTo("Summer Holiday"));

    // 6. Update template item
    String templateItemId =
        authenticated()
            .header("X-Household-Id", householdId)
            .when()
            .get("/api/v1/listz/templates/{id}", templateId)
            .then()
            .extract()
            .path("items[0].id");

    authenticated()
        .header("X-Household-Id", householdId)
        .contentType(JSON)
        .body(Map.of("quantityRuleValue", 5.0, "categoryOverride", "Toiletries"))
        .when()
        .put("/api/v1/listz/template-items/{id}", templateItemId)
        .then()
        .statusCode(200)
        .body("quantityRuleValue", equalTo(5.0f))
        .body("categoryOverride", equalTo("Toiletries"));

    // 7. Remove item from template
    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .delete("/api/v1/listz/template-items/{id}", templateItemId)
        .then()
        .statusCode(204);

    // 8. Delete template
    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .delete("/api/v1/listz/templates/{id}", templateId)
        .then()
        .statusCode(204);

    authenticated()
        .header("X-Household-Id", householdId)
        .when()
        .get("/api/v1/listz/templates")
        .then()
        .statusCode(200)
        .body("templates", hasSize(0));
  }
}
