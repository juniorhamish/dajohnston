package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticatedAsUser2;

import java.util.Map;
import org.junit.jupiter.api.Test;

class RlsIT extends AbstractIntegrationTest {

  @Test
  void user1Household_notVisibleToUser2() {
    String householdName = "User1-Household-%s".formatted(System.currentTimeMillis());

    authenticated()
        .contentType(JSON)
        .body(Map.of("name", householdName))
        .when()
        .post("/api/households")
        .then()
        .statusCode(201)
        .body("name", equalTo(householdName));

    authenticated()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", hasItem(householdName));

    authenticatedAsUser2()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", not(hasItem(householdName)));
  }

  @Test
  void user2Household_notVisibleToUser1() {
    String householdName = "User2-Household-%s".formatted(System.currentTimeMillis());

    authenticatedAsUser2()
        .contentType(JSON)
        .body(Map.of("name", householdName))
        .when()
        .post("/api/households")
        .then()
        .statusCode(201)
        .body("name", equalTo(householdName));

    authenticatedAsUser2()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", hasItem(householdName));

    authenticated()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", not(hasItem(householdName)));
  }

  @Test
  void invitedUser_canSeeHouseholdAfterJoining() {
    String householdName = "Shared-Household-%s".formatted(System.currentTimeMillis());

    String householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", householdName))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    authenticatedAsUser2()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", not(hasItem(householdName)));

    authenticated()
        .contentType(JSON)
        .body(Map.of("email", System.getenv("TEST_AUTH0_USERNAME_2"), "role", "MEMBER"))
        .when()
        .post("/api/households/{id}/invitations", householdId)
        .then()
        .statusCode(201);

    authenticatedAsUser2()
        .when()
        .post("/api/households/{id}/join", householdId)
        .then()
        .statusCode(200)
        .body("name", equalTo(householdName))
        .body("role", equalTo("MEMBER"));

    authenticatedAsUser2()
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(200)
        .body("households.name", hasItem(householdName));
  }
}
