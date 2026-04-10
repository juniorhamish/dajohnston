package uk.co.dajohnston.portal.household.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.dajohnston.portal.household.HouseholdRole.OWNER;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.household.Household;
import uk.co.dajohnston.portal.household.HouseholdMapperImpl;
import uk.co.dajohnston.portal.household.HouseholdRole;
import uk.co.dajohnston.portal.household.HouseholdService;
import uk.co.dajohnston.portal.household.Invitation;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(HouseholdController.class)
@Import({SecurityConfig.class, HouseholdMapperImpl.class})
class HouseholdControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private HouseholdService householdService;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void createHousehold_returnsCreatedHousehold() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    Household household =
        Household.builder().id(householdId).name("New Household").role(OWNER).build();

    when(householdService.createHousehold(any(), any())).thenReturn(household);

    mockMvc
        .perform(
            post("/api/households")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "name": "New Household"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(householdId.toString()))
        .andExpect(jsonPath("$.name").value("New Household"))
        .andExpect(jsonPath("$.role").value("OWNER"));
  }

  @Test
  void createHousehold_missingName_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/households")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createHousehold_nullBody_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/api/households")
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  void joinHousehold_noInvitation_returnsForbidden() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");

    when(householdService.joinHousehold(any(), any()))
        .thenThrow(new org.springframework.security.access.AccessDeniedException(""));

    mockMvc
        .perform(
            post("/api/households/{id}/join", householdId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "test@example.com"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void joinHousehold_withInvitation_returnsJoinedHousehold() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    Household household =
        Household.builder()
            .id(householdId)
            .name("Existing Household")
            .role(HouseholdRole.MEMBER)
            .build();

    when(householdService.joinHousehold(any(), any())).thenReturn(household);

    mockMvc
        .perform(
            post("/api/households/{id}/join", householdId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "test@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(householdId.toString()))
        .andExpect(jsonPath("$.name").value("Existing Household"))
        .andExpect(jsonPath("$.role").value("MEMBER"));
  }

  @Test
  void inviteUser_ownerInvites_returnsCreated() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    Invitation invitation =
        Invitation.builder()
            .email("friend@example.com")
            .role(HouseholdRole.MEMBER)
            .status("PENDING")
            .build();

    when(householdService.inviteUser(any(), any(), any(), any())).thenReturn(invitation);

    mockMvc
        .perform(
            post("/api/households/{id}/invitations", householdId)
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "friend@example.com",
                      "role": "MEMBER"
                    }
                    """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("friend@example.com"))
        .andExpect(jsonPath("$.role").value("MEMBER"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  @Test
  void inviteUser_nonOwnerInvites_returnsForbidden() throws Exception {
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");

    when(householdService.inviteUser(any(), any(), any(), any()))
        .thenThrow(new org.springframework.security.access.AccessDeniedException(""));

    mockMvc
        .perform(
            post("/api/households/{id}/invitations", householdId)
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "friend@example.com",
                      "role": "MEMBER"
                    }
                    """))
        .andExpect(status().isForbidden());
  }

  @Test
  void inviteUser_missingEmail_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post(
                    "/api/households/{id}/invitations",
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "role": "MEMBER"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void inviteUser_invalidEmail_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post(
                    "/api/households/{id}/invitations",
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "invalid-email",
                      "role": "MEMBER"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void inviteUser_missingRole_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post(
                    "/api/households/{id}/invitations",
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "friend@example.com"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void inviteUser_invalidRole_returnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post(
                    "/api/households/{id}/invitations",
                    UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .with(jwt().jwt(jwt -> jwt.subject("auth0|123")))
                .contentType(APPLICATION_JSON)
                .content(
                    """
                    {
                      "email": "friend@example.com",
                      "role": "INVALID_ROLE"
                    }
                    """))
        .andExpect(status().isBadRequest());
  }
}
