package uk.co.dajohnston.portal.notification.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.config.TenantInterceptor;
import uk.co.dajohnston.portal.notification.NotificationMapperImpl;
import uk.co.dajohnston.portal.notification.NotificationService;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(NotificationController.class)
@Import({SecurityConfig.class, NotificationMapperImpl.class})
class NotificationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private NotificationService notificationService;
  @MockitoBean private TenantInterceptor tenantInterceptor;

  @BeforeEach
  void setUp() {
    when(tenantInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Test
  void getVapidPublicKey_returnsPublicKey() throws Exception {
    when(notificationService.getVapidPublicKey()).thenReturn("public-key");

    mockMvc
        .perform(get("/api/notifications/vapid-public-key").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicKey").value("public-key"));
  }

  @Test
  void getVapidPublicKey_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc
        .perform(get("/api/notifications/vapid-public-key"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void registerSubscription_callsService() throws Exception {
    mockMvc
        .perform(
            post("/api/notifications/subscriptions")
                .with(jwt())
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "endpoint": "https://endpoint",
                      "keys": {
                        "p256dh": "p256dh",
                        "auth": "auth"
                      }
                    }
                    """))
        .andExpect(status().isCreated());

    verify(notificationService)
        .registerSubscription(
            any(),
            argThat(
                x ->
                    x.endpoint().equals("https://endpoint")
                        && x.p256Dh().equals("p256dh")
                        && x.auth().equals("auth")));
  }

  @Test
  void registerSubscription_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc
        .perform(
            post("/api/notifications/subscriptions")
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(""))
        .andExpect(status().isUnauthorized());
  }
}
