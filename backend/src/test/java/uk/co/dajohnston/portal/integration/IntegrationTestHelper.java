package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.given;

import io.restassured.specification.RequestSpecification;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IntegrationTestHelper {

  private static String getAccessToken() {
    return requestToken(
        System.getenv("TEST_AUTH0_CLIENT_ID"),
        System.getenv("TEST_AUTH0_CLIENT_SECRET"),
        System.getenv("TEST_AUTH0_AUDIENCE"));
  }

  public static String requestToken(String clientId, String clientSecret, String audience) {
    Map<String, String> body =
        Map.of(
            "client_id",
            clientId,
            "client_secret",
            clientSecret,
            "audience",
            audience,
            "grant_type",
            "client_credentials");
    return given()
        .port(443)
        .contentType("application/json")
        .body(body)
        .post(System.getenv("TEST_AUTH0_TOKEN_URL"))
        .path("access_token");
  }

  public static RequestSpecification authenticated() {
    return given().header("Authorization", "Bearer %s".formatted(getAccessToken()));
  }
}
