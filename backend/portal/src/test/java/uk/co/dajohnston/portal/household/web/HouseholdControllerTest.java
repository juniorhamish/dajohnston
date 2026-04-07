package uk.co.dajohnston.portal.household.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.co.dajohnston.portal.household.HouseholdRole.OWNER;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.co.dajohnston.portal.household.HouseholdMapperImpl;
import uk.co.dajohnston.portal.household.HouseholdRole;
import uk.co.dajohnston.portal.household.HouseholdService;
import uk.co.dajohnston.portal.household.entity.HouseholdEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberId;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.household.entity.HouseholdRepository;
import uk.co.dajohnston.portal.household.entity.InvitationEntity;
import uk.co.dajohnston.portal.household.entity.InvitationRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;
import uk.co.dajohnston.portal.user.entity.UserRepository;
import uk.co.dajohnston.security.config.SecurityConfig;

@WebMvcTest(HouseholdController.class)
@Import({
  SecurityConfig.class,
  HouseholdService.class,
  UserService.class,
  HouseholdMapperImpl.class
})
class HouseholdControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private HouseholdRepository householdRepository;
  @MockitoBean private HouseholdMemberRepository householdMemberRepository;
  @MockitoBean private UserRepository userRepository;
  @MockitoBean private InvitationRepository invitationRepository;
  @MockitoBean private JwtDecoder jwtDecoder;

  @Test
  void createHousehold_returnsCreatedHousehold() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("New Household").build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdRepository.save(any(HouseholdEntity.class))).thenReturn(household);

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
  void joinHousehold_noInvitation_returnsForbidden() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Existing Household").build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.findByHouseholdIdAndEmail(householdId, "test@example.com"))
        .thenReturn(Optional.empty());

    mockMvc
        .perform(
            post("/api/households/{id}/join", householdId)
                .with(
                    jwt().jwt(jwt -> jwt.subject("auth0|123").claim("email", "test@example.com"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void joinHousehold_withInvitation_returnsJoinedHousehold() throws Exception {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Existing Household").build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .household(household)
            .email("test@example.com")
            .role(HouseholdRole.MEMBER)
            .status("PENDING")
            .build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.findByHouseholdIdAndEmail(householdId, "test@example.com"))
        .thenReturn(Optional.of(invitation));

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
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Existing Household").build();
    HouseholdMemberEntity member = HouseholdMemberEntity.builder().role(OWNER).build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(Optional.of(member));
    when(invitationRepository.save(any(InvitationEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

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
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Existing Household").build();

    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(Optional.empty());

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
}
