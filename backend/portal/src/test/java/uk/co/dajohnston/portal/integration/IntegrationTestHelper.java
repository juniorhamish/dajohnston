package uk.co.dajohnston.portal.integration;

import static io.restassured.RestAssured.given;

import io.restassured.specification.RequestSpecification;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class IntegrationTestHelper {

  private static String getAccessToken() {
    return requestToken(System.getenv("TEST_AUTH0_USERNAME"), System.getenv("TEST_AUTH0_PASSWORD"));
  }

  private static String getAccessTokenUser2() {
    return requestToken(
        System.getenv("TEST_AUTH0_USERNAME_2"), System.getenv("TEST_AUTH0_PASSWORD_2"));
  }

  public static String requestToken(String username, String password) {
    return requestToken(
        System.getenv("TEST_AUTH0_CLIENT_ID"),
        System.getenv("TEST_AUTH0_CLIENT_SECRET"),
        System.getenv("TEST_AUTH0_AUDIENCE"),
        username,
        password);
  }

  public static String requestToken(
      String clientId, String clientSecret, String audience, String username, String password) {
    Map<String, String> body =
        Map.of(
            "client_id",
            clientId,
            "client_secret",
            clientSecret,
            "audience",
            audience,
            "grant_type",
            "password",
            "username",
            username,
            "password",
            password,
            "scope",
            "openid profile email");
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

  public static RequestSpecification authenticatedAsUser2() {
    return given().header("Authorization", "Bearer %s".formatted(getAccessTokenUser2()));
  }
}
