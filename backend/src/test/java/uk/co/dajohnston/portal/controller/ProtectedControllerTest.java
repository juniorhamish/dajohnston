package uk.co.dajohnston.portal.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.config.SecurityConfig;

@WebMvcTest(ProtectedController.class)
@Import(SecurityConfig.class)
class ProtectedControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void protectedEndpoint_returnsMessageWhenAuthenticated() throws Exception {
    mockMvc
        .perform(get("/api/protected").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("This is a protected endpoint"));
  }
}
