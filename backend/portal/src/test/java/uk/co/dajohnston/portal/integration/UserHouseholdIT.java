package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticatedAsUser2;

import java.util.Map;
import org.junit.jupiter.api.Test;

class UserHouseholdIT extends AbstractIntegrationTest {

  @Test
  void updateCurrentUser_updatesNickname() {
    String newNickname = "New Nickname-%s".formatted(System.currentTimeMillis());
    authenticated()
        .contentType(JSON)
        .body(Map.of("nickname", newNickname))
        .when()
        .patch("/api/users/me")
        .then()
        .statusCode(200)
        .body("nickname", equalTo(newNickname));
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
        .body("id", equalTo(householdId))
        .body("name", equalTo("Joinable Household"))
        .body("role", equalTo("MEMBER"));
  }

  @Test
  void deleteAndRestoreHousehold_success() {
    String householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", "Deletable Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    authenticated().when().delete("/api/households/{id}", householdId).then().statusCode(204);

    authenticated()
        .when()
        .get("/api/households")
        .then()
        .statusCode(200)
        .body("id", notNullValue())
        .body("name", org.hamcrest.Matchers.not(hasItem("Deletable Household")));

    authenticated()
        .when()
        .post("/api/households/{id}/restore", householdId)
        .then()
        .statusCode(200)
        .body("id", equalTo(householdId))
        .body("name", equalTo("Deletable Household"));

    authenticated()
        .when()
        .get("/api/households")
        .then()
        .statusCode(200)
        .body("name", hasItem("Deletable Household"));
  }

  @Test
  void deleteHousehold_fails_whenNotOwner() {
    String householdId =
        authenticated()
            .contentType(JSON)
            .body(Map.of("name", "Non-Deletable Household"))
            .when()
            .post("/api/households")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    authenticatedAsUser2()
        .when()
        .delete("/api/households/{id}", householdId)
        .then()
        .statusCode(403);
  }
}
