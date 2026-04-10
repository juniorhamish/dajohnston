package uk.co.dajohnston.portal.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.dajohnston.portal.household.HouseholdRole.MEMBER;
import static uk.co.dajohnston.portal.household.HouseholdRole.OWNER;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.household.Household;
import uk.co.dajohnston.portal.user.UserMapperImpl;
import uk.co.dajohnston.portal.user.UserProfile;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, UserMapperImpl.class})
class UserControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private UserService userService;

  @Test
  void getCurrentUser_userExists_returnsUser() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserProfile userProfile =
        UserProfile.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .givenName("Test")
            .familyName("User")
            .nickname("Test User")
            .picture("https://example.com/pic.jpg")
            .build();

    when(userService.getCurrentUser(any())).thenReturn(userProfile);

    mockMvc
        .perform(get("/api/users/me").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.auth0Id").value("auth0|123"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.givenName").value("Test"))
        .andExpect(jsonPath("$.familyName").value("User"))
        .andExpect(jsonPath("$.nickname").value("Test User"))
        .andExpect(jsonPath("$.picture").value("https://example.com/pic.jpg"));
  }

  @Test
  void getCurrentUser_withHouseholds_returnsHouseholds() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    Household household =
        Household.builder().id(householdId).name("Test Household").role(OWNER).build();
    UserProfile userProfile = UserProfile.builder().households(List.of(household)).build();
    when(userService.getCurrentUser(any())).thenReturn(userProfile);

    mockMvc
        .perform(get("/api/users/me").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.households[0].id").value(householdId.toString()))
        .andExpect(jsonPath("$.households[0].name").value("Test Household"))
        .andExpect(jsonPath("$.households[0].role").value("OWNER"));
  }

  @Test
  void getCurrentUser_invokesServiceWithJwt() throws Exception {
    mockMvc.perform(get("/api/users/me").with(jwt().jwt(jwt -> jwt.subject("auth0|123"))));

    verify(userService).getCurrentUser(argThat(arg -> arg.getSubject().equals("auth0|123")));
  }

  @Test
  void updateCurrentUser_returnsUpdatedUser() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    Household household =
        Household.builder().id(householdId).name("Test Household").role(MEMBER).build();
    UserProfile userProfile =
        UserProfile.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .givenName("New")
            .familyName("Users")
            .nickname("newuser")
            .picture("https://examples.com/pic.jpg")
            .households(List.of(household))
            .build();
    when(userService.updateCurrentUser(any(), any())).thenReturn(userProfile);

    mockMvc
        .perform(
            patch("/api/users/me")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                        {
                          "givenName": "New",
                          "familyName": "Users",
                          "nickname": "newuser",
                          "picture": "https://examples.com/pic.jpg",
                          "useGravatar": false
                        }
                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.auth0Id").value("auth0|123"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.givenName").value("New"))
        .andExpect(jsonPath("$.familyName").value("Users"))
        .andExpect(jsonPath("$.nickname").value("newuser"))
        .andExpect(jsonPath("$.picture").value("https://examples.com/pic.jpg"))
        .andExpect(jsonPath("$.households[0].id").value(householdId.toString()))
        .andExpect(jsonPath("$.households[0].name").value("Test Household"))
        .andExpect(jsonPath("$.households[0].role").value("MEMBER"));
  }

  @Test
  void updateCurrentUser_invokesServiceWithJwt() throws Exception {
    mockMvc.perform(
        patch("/api/users/me")
            .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
            .contentType(APPLICATION_JSON)
            .content("{}"));

    verify(userService)
        .updateCurrentUser(argThat(arg -> arg.getSubject().equals("auth0|123")), any());
  }

  @Test
  void updateCurrentUser_invokesServiceWithUserBody() throws Exception {
    mockMvc.perform(
        patch("/api/users/me")
            .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
            .contentType(APPLICATION_JSON)
            .content(
                """
                {
                  "nickname": "DJ"
                }
                """));

    verify(userService).updateCurrentUser(any(), argThat(arg -> arg.nickname().equals("DJ")));
  }
}
