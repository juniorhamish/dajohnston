package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.requestToken;

import org.junit.jupiter.api.Test;

class ApiIntegrationIT extends AbstractIntegrationTest {

  @Test
  void healthCheck_returnsUp() {
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/health")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Test
  void healthLiveness_returnsUp() {
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/health/liveness")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Test
  void healthReadiness_returnsUp() {
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/health/readiness")
        .then()
        .statusCode(200)
        .body("status", equalTo("UP"));
  }

  @Test
  void infoEndpoint_returnsBuildInfo() {
    given()
        .contentType(JSON)
        .when()
        .get("/actuator/info")
        .then()
        .statusCode(200)
        .body("build.artifact", equalTo("portal"));
  }

  @Test
  void protectedEndpoint_withoutAuth_returnsUnauthorized() {
    given().contentType(JSON).when().get("/api/users/me").then().statusCode(401);
  }

  @Test
  void protectedEndpoint_withAuth_returnsSuccess() {
    authenticated().when().get("/api/users/me").then().statusCode(200);
  }

  @Test
  void wrongAudience_returnsUnauthorized() {
    given()
        .header(
            "Authorization",
            "Bearer %s"
                .formatted(
                    requestToken(
                        System.getenv("TEST_AUTH0_CLIENT_ID"),
                        System.getenv("TEST_AUTH0_CLIENT_SECRET"),
                        "https://spice-tracker-service.dajohnston.co.uk")))
        .when()
        .get("/api/users/me")
        .then()
        .statusCode(401);
  }
}
