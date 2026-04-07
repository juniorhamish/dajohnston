package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;

import java.util.Map;
import org.junit.jupiter.api.Test;

class UserHouseholdIT extends AbstractIntegrationTest {

  @Test
  void updateCurrentUser_updatesDisplayName() {
    authenticated()
        .contentType(JSON)
        .body(Map.of("displayName", "New Display Name"))
        .when()
        .patch("/api/users/me")
        .then()
        .statusCode(200)
        .body("displayName", equalTo("New Display Name"));
  }

  @Test
  void createHousehold_createsNewHouseholdAndSetsOwner() {
    authenticated()
        .contentType(JSON)
        .body(Map.of("name", "My New Household"))
        .when()
        .post("/api/households")
        .then()
        .statusCode(201)
        .body("id", notNullValue())
        .body("name", equalTo("My New Household"))
        .body("role", equalTo("OWNER"));

    authenticated()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", hasItem("My New Household"))
        .body("households.role", hasItem("OWNER"));
  }

  @Test
  void joinHousehold_addsUserToHouseholdAsMember() {
    String householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", "Joinable Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    authenticated()
        .contentType(JSON)
        .body(Map.of("email", System.getenv("TEST_AUTH0_USERNAME"), "role", "MEMBER"))
        .when()
        .post("/api/households/{id}/invitations", householdId)
        .then()
        .statusCode(201);

    authenticated()
        .when()
        .post("/api/households/{id}/join", householdId)
        .then()
        .statusCode(200)
        .body("id", equalTo(householdId))
        .body("name", equalTo("Joinable Household"))
        .body("role", equalTo("MEMBER"));
  }
}
