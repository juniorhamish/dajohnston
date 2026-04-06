package uk.co.dajohnston.portal.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration")
abstract class AbstractIntegrationTest {

  @ServiceConnection static PostgreSQLContainer<?> postgres;

  static {
    postgres = new PostgreSQLContainer<>("postgres:17-alpine");
    postgres.start();
  }

  @LocalServerPort private int port;

  @BeforeEach
  void setupRestAssured() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }
}
