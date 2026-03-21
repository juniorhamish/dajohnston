package uk.co.dajohnston.portal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.config.SecurityConfig;
import uk.co.dajohnston.portal.controller.ProtectedController;

@WebMvcTest({ProtectedController.class})
@Import(SecurityConfig.class)
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void protected_requiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/protected")).andExpect(status().isUnauthorized());
  }

  @Test
  void protected_withValidToken_isAllowed() throws Exception {
    mockMvc
        .perform(get("/api/protected").with(jwt().jwt(builder -> builder.claim("scope", "read"))))
        .andExpect(status().isOk());
  }
}
