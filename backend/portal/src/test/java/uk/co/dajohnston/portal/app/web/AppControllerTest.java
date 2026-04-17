package uk.co.dajohnston.portal.app.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.app.App;
import uk.co.dajohnston.portal.app.AppMapperImpl;
import uk.co.dajohnston.portal.app.AppService;
import uk.co.dajohnston.portal.config.TenantInterceptor;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(AppController.class)
@Import({SecurityConfig.class, AppMapperImpl.class})
class AppControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private AppService appService;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private TenantInterceptor tenantInterceptor;

  @BeforeEach
  void setUp() {
    when(tenantInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Test
  void listApps_returnsAvailableApps() throws Exception {
    List<App> apps =
        List.of(
            App.builder()
                .id("spice-tracker")
                .name("Spice Tracker")
                .description("Track your spice inventory")
                .icon("pepper")
                .url("/apps/spice-tracker")
                .build(),
            App.builder()
                .id("meal-planner")
                .name("Meal Planner")
                .description("Plan your weekly meals")
                .icon("utensils")
                .url("/apps/meal-planner")
                .build());

    when(appService.listActiveApps()).thenReturn(apps);

    mockMvc
        .perform(get("/api/apps").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apps", hasSize(2)))
        .andExpect(jsonPath("$.apps[0].id").value("spice-tracker"))
        .andExpect(jsonPath("$.apps[0].name").value("Spice Tracker"))
        .andExpect(jsonPath("$.apps[0].description").value("Track your spice inventory"))
        .andExpect(jsonPath("$.apps[0].icon").value("pepper"))
        .andExpect(jsonPath("$.apps[0].url").value("/apps/spice-tracker"))
        .andExpect(jsonPath("$.apps[1].id").value("meal-planner"))
        .andExpect(jsonPath("$.apps[1].name").value("Meal Planner"));
  }

  @Test
  void listApps_returnsEmptyList_whenNoApps() throws Exception {
    when(appService.listActiveApps()).thenReturn(List.of());

    mockMvc
        .perform(get("/api/apps").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.apps", hasSize(0)));
  }

  @Test
  void listApps_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/apps")).andExpect(status().isUnauthorized());
  }
}
