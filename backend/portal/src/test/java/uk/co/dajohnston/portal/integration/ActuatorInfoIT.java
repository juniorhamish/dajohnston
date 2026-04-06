package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.jupiter.api.Test;

class ActuatorInfoIT extends AbstractIntegrationTest {

  @Test
  void actuatorInfo_returnsBuildInfo() {
    when()
        .get("/actuator/info")
        .then()
        .statusCode(200)
        .body("build.version", equalTo("0.0.1-SNAPSHOT"))
        .body("build.artifact", equalTo("portal"));
  }

  @Test
  void livenessProbe_returnsUp() {
    when().get("/actuator/health/liveness").then().statusCode(200).body("status", equalTo("UP"));
  }

  @Test
  void readinessProbe_returnsUp() {
    when().get("/actuator/health/readiness").then().statusCode(200).body("status", equalTo("UP"));
  }
}
