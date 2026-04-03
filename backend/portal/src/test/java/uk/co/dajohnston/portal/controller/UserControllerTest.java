package uk.co.dajohnston.portal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.model.Household;
import uk.co.dajohnston.portal.model.HouseholdMember;
import uk.co.dajohnston.portal.model.User;
import uk.co.dajohnston.portal.repository.HouseholdMemberRepository;
import uk.co.dajohnston.portal.repository.UserRepository;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserRepository userRepository;
  @MockitoBean private HouseholdMemberRepository householdMemberRepository;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void getCurrentUser_userExists_returnsUser() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    User user =
        User.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .displayName("Test User")
            .build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(List.of());

    mockMvc
        .perform(get("/api/users/me").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.auth0Id").value("auth0|123"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.displayName").value("Test User"));
  }

  @Test
  void getCurrentUser_userDoesNotExist_createsAndReturnsUser() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    User user =
        User.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .displayName("Test User")
            .build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(householdMemberRepository.findByUserId(userId)).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/users/me")
                .with(
                    jwt()
                        .jwt(
                            jwt ->
                                jwt.subject("auth0|123")
                                    .claim("email", "test@example.com")
                                    .claim("name", "Test User"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.displayName").value("Test User"));

    verify(userRepository).save(any(User.class));
  }

  @Test
  void getCurrentUser_withHouseholds_returnsHouseholds() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    User user = User.builder().id(userId).auth0Id("auth0|123").build();
    Household household = Household.builder().id(householdId).name("Test Household").build();
    HouseholdMember membership =
        HouseholdMember.builder().household(household).user(user).role("OWNER").build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(List.of(membership));

    mockMvc
        .perform(get("/api/users/me").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.households[0].id").value(householdId.toString()))
        .andExpect(jsonPath("$.households[0].name").value("Test Household"))
        .andExpect(jsonPath("$.households[0].role").value("OWNER"));
  }
}
