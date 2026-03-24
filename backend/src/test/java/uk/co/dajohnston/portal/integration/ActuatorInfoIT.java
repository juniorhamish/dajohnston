package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class ActuatorInfoIT {

  @LocalServerPort private int port;

  @BeforeEach
  void setup() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void actuatorInfo_returnsBuildInfo() {
    when()
        .get("/actuator/info")
        .then()
        .statusCode(200)
        .body("build.version", equalTo("0.0.1-SNAPSHOT"))
        .body("build.artifact", equalTo("portal-backend"));
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
