package uk.co.dajohnston.portal.integration;

import static io.restassured.http.ContentType.JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.co.dajohnston.portal.integration.IntegrationTestHelper.authenticated;

import org.junit.jupiter.api.Test;

class NotificationsIT extends AbstractIntegrationTest {

  @Test
  void sendNotification_toRegisteredUser_sendsPush() throws Exception {
    String email = System.getenv("TEST_AUTH0_USERNAME");
    // 1. Create user by calling /api/users/me
    authenticated().get("/api/users/me").then().statusCode(200);

    // 2. Register subscription
    authenticated()
        .contentType(JSON)
        .body(
            """
            {
              "endpoint": "https://endpoint",
              "keys": {
                "p256dh": "BD0vShkR0Je-Kbp5JhGpzGGLsrgaGWTtjgJ16XCE-n4dXzouHG7yPB8FRqhOkYTNGwTvb8rzfY4oYHo3qTch3vo",
                "auth": "in-CG3hCHupt8AWGSe0-yQ"
              }
            }
            """)
        .post("/api/notifications/subscriptions")
        .then()
        .statusCode(201);

    // 3. Send notification
    authenticated()
        .contentType(JSON)
        .body(
            """
            {
              "username": "%s",
              "title": "Hello",
              "body": "World"
            }
            """
                .formatted(email))
        .post("/api/notifications/send")
        .then()
        .statusCode(204);

    // 4. Verify
    verify(pushService).send(any(), any());
  }
}
