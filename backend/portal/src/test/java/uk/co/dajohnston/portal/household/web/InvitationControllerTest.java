package uk.co.dajohnston.portal.household.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.dajohnston.portal.household.HouseholdRole.MEMBER;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.config.TenantInterceptor;
import uk.co.dajohnston.portal.household.HouseholdMapperImpl;
import uk.co.dajohnston.portal.household.HouseholdService;
import uk.co.dajohnston.portal.household.Invitation;
import uk.co.dajohnston.security.config.SecurityConfig;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@WebMvcTest(InvitationController.class)
@Import({SecurityConfig.class, HouseholdMapperImpl.class})
class InvitationControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private HouseholdService householdService;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private TenantInterceptor tenantInterceptor;

  @BeforeEach
  void setUp() {
    when(tenantInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Test
  void listPendingInvitations_returnsInvitations() throws Exception {
    UUID invitationId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID invitationId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    UUID householdId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    List<Invitation> invitations =
        List.of(
            Invitation.builder()
                .id(invitationId1)
                .householdId(householdId)
                .householdName("House 1")
                .email("user@example.com")
                .role(MEMBER)
                .status("PENDING")
                .build(),
            Invitation.builder()
                .id(invitationId2)
                .householdId(householdId)
                .householdName("House 2")
                .email("user@example.com")
                .role(MEMBER)
                .status("PENDING")
                .build());

    when(householdService.listPendingInvitations(any())).thenReturn(invitations);

    mockMvc
        .perform(
            get("/api/invitations")
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitations", hasSize(2)))
        .andExpect(jsonPath("$.invitations[0].id").value(invitationId1.toString()))
        .andExpect(jsonPath("$.invitations[0].householdName").value("House 1"))
        .andExpect(jsonPath("$.invitations[0].email").value("user@example.com"))
        .andExpect(jsonPath("$.invitations[0].role").value("MEMBER"))
        .andExpect(jsonPath("$.invitations[0].status").value("PENDING"))
        .andExpect(jsonPath("$.invitations[1].id").value(invitationId2.toString()));
  }

  @Test
  void listPendingInvitations_returnsEmptyList() throws Exception {
    when(householdService.listPendingInvitations(any())).thenReturn(List.of());

    mockMvc
        .perform(
            get("/api/invitations")
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.invitations", hasSize(0)));
  }

  @Test
  void listPendingInvitations_unauthenticated_returnsUnauthorized() throws Exception {
    mockMvc.perform(get("/api/invitations")).andExpect(status().isUnauthorized());
  }

  @Test
  void acceptInvitation_returnsAcceptedInvitation() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID householdId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    Invitation invitation =
        Invitation.builder()
            .id(invitationId)
            .householdId(householdId)
            .householdName("My House")
            .email("user@example.com")
            .role(MEMBER)
            .status("ACCEPTED")
            .build();

    when(householdService.acceptInvitation(any(), any())).thenReturn(invitation);

    mockMvc
        .perform(
            post("/api/invitations/{id}/accept", invitationId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(invitationId.toString()))
        .andExpect(jsonPath("$.householdName").value("My House"))
        .andExpect(jsonPath("$.status").value("ACCEPTED"));
  }

  @Test
  void acceptInvitation_notFound_returnsNotFound() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    when(householdService.acceptInvitation(any(), any()))
        .thenThrow(new ResourceNotFoundException("Invitation not found"));

    mockMvc
        .perform(
            post("/api/invitations/{id}/accept", invitationId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void acceptInvitation_unauthenticated_returnsUnauthorized() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    mockMvc
        .perform(post("/api/invitations/{id}/accept", invitationId).with(csrf()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void declineInvitation_returnsDeclinedInvitation() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID householdId = UUID.fromString("33333333-3333-3333-3333-333333333333");

    Invitation invitation =
        Invitation.builder()
            .id(invitationId)
            .householdId(householdId)
            .householdName("My House")
            .email("user@example.com")
            .role(MEMBER)
            .status("DECLINED")
            .build();

    when(householdService.declineInvitation(any(), any())).thenReturn(invitation);

    mockMvc
        .perform(
            post("/api/invitations/{id}/decline", invitationId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(invitationId.toString()))
        .andExpect(jsonPath("$.householdName").value("My House"))
        .andExpect(jsonPath("$.status").value("DECLINED"));
  }

  @Test
  void declineInvitation_notFound_returnsNotFound() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");

    when(householdService.declineInvitation(any(), any()))
        .thenThrow(new ResourceNotFoundException("Invitation not found"));

    mockMvc
        .perform(
            post("/api/invitations/{id}/decline", invitationId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "user@example.com"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void declineInvitation_unauthenticated_returnsUnauthorized() throws Exception {
    UUID invitationId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    mockMvc
        .perform(post("/api/invitations/{id}/decline", invitationId).with(csrf()))
        .andExpect(status().isUnauthorized());
  }
}
