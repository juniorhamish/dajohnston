package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.requestToken;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration")
class ApiIntegrationIT {

  @LocalServerPort private int port;

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

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
    given().contentType(JSON).when().get("/api/protected").then().statusCode(401);
  }

  @Test
  void protectedEndpoint_withAuth_returnsSuccess() {
    authenticated()
        .when()
        .get("/api/protected")
        .then()
        .statusCode(200)
        .body("message", equalTo("This is a protected endpoint"));
  }

  @Test
  void missingAudience_returnsUnauthorized() {
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
        .get("/api/protected")
        .then()
        .statusCode(401);
  }
}
