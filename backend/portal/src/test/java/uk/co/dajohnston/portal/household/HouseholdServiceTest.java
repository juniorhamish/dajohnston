package uk.co.dajohnston.portal.household;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.dajohnston.portal.household.HouseholdRole.MEMBER;
import static uk.co.dajohnston.portal.household.HouseholdRole.OWNER;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import uk.co.dajohnston.portal.household.entity.HouseholdEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberId;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.household.entity.HouseholdRepository;
import uk.co.dajohnston.portal.household.entity.InvitationEntity;
import uk.co.dajohnston.portal.household.entity.InvitationRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

  @Mock private HouseholdRepository householdRepository;
  @Mock private HouseholdMemberRepository householdMemberRepository;
  @Mock private InvitationRepository invitationRepository;
  @Mock private UserService userService;
  @Mock private JwtClaimAccessor jwt;

  @InjectMocks private HouseholdService householdService;

  @Test
  void listHouseholds_returnsHouseholdsFromRepository() {
    UUID householdId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
    UUID householdId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    HouseholdEntity household1 = HouseholdEntity.builder().id(householdId1).name("House 1").build();
    HouseholdEntity household2 = HouseholdEntity.builder().id(householdId2).name("House 2").build();
    HouseholdMemberEntity member1 =
        HouseholdMemberEntity.builder().household(household1).role(OWNER).build();
    HouseholdMemberEntity member2 =
        HouseholdMemberEntity.builder().household(household2).role(MEMBER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(householdMemberRepository.findAll()).thenReturn(List.of(member1, member2));

    List<Household> result = householdService.listHouseholds(jwt);

    assertThat(result).hasSize(2);
    assertThat(result.getFirst().id()).isEqualTo(householdId1);
    assertThat(result.getFirst().name()).isEqualTo("House 1");
    assertThat(result.getFirst().role()).isEqualTo(OWNER);
    assertThat(result.get(1).id()).isEqualTo(householdId2);
    assertThat(result.get(1).name()).isEqualTo("House 2");
    assertThat(result.get(1).role()).isEqualTo(MEMBER);
  }

  @Test
  void listHouseholds_returnsEmptyList_whenNoMemberships() {
    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(householdMemberRepository.findAll()).thenReturn(List.of());

    List<Household> result = householdService.listHouseholds(jwt);

    assertThat(result).isEmpty();
  }

  @Test
  void createHousehold_returnsCreatedHousehold() {
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("New Household").build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdRepository.save(any(HouseholdEntity.class))).thenReturn(household);

    Household result = householdService.createHousehold(jwt, "New Household");

    assertThat(result.id()).isEqualTo(householdId);
    assertThat(result.name()).isEqualTo("New Household");
    assertThat(result.role()).isEqualTo(OWNER);
    verify(householdMemberRepository).save(any(HouseholdMemberEntity.class));
  }

  @Test
  void joinHousehold_success() {
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household = HouseholdEntity.builder().id(householdId).name("Household").build();
    InvitationEntity invitation = InvitationEntity.builder().role(MEMBER).status("PENDING").build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.findByHouseholdIdAndEmail(householdId, "test@example.com"))
        .thenReturn(Optional.of(invitation));

    Household result = householdService.joinHousehold(householdId, jwt);

    assertThat(result.id()).isEqualTo(householdId);
    assertThat(result.name()).isEqualTo("Household");
    assertThat(result.role()).isEqualTo(MEMBER);
    assertThat(invitation.getStatus()).isEqualTo("ACCEPTED");
    verify(householdMemberRepository).save(any(HouseholdMemberEntity.class));
    verify(invitationRepository).save(invitation);
  }

  @Test
  void joinHousehold_householdNotFound_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000000");

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> householdService.joinHousehold(householdId, jwt))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Household not found");
  }

  @Test
  void joinHousehold_noInvitationFound_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000009");
    HouseholdEntity household = HouseholdEntity.builder().id(householdId).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.findByHouseholdIdAndEmail(householdId, "test@example.com"))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> householdService.joinHousehold(householdId, jwt))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("No pending invitation found for this household");
  }

  @Test
  void joinHousehold_invitationNotPending_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000008");
    HouseholdEntity household = HouseholdEntity.builder().id(householdId).build();
    InvitationEntity invitation = InvitationEntity.builder().status("ACCEPTED").build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("test@example.com");
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.findByHouseholdIdAndEmail(householdId, "test@example.com"))
        .thenReturn(Optional.of(invitation));

    assertThatThrownBy(() -> householdService.joinHousehold(householdId, jwt))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("No pending invitation found for this household");
  }

  @Test
  void inviteUser_success() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    UserEntity requester = UserEntity.builder().id(requesterId).build();
    HouseholdMemberEntity member = HouseholdMemberEntity.builder().role(OWNER).build();
    HouseholdEntity household = HouseholdEntity.builder().id(householdId).name("Household").build();
    InvitationEntity savedInvitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("friend@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(requester);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, requesterId)))
        .thenReturn(Optional.of(member));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(invitationRepository.save(any(InvitationEntity.class))).thenReturn(savedInvitation);

    Invitation result = householdService.inviteUser(householdId, jwt, "friend@example.com", MEMBER);

    assertThat(result.id()).isEqualTo(invitationId);
    assertThat(result.householdId()).isEqualTo(householdId);
    assertThat(result.email()).isEqualTo("friend@example.com");
    assertThat(result.role()).isEqualTo(MEMBER);
    assertThat(result.status()).isEqualTo("PENDING");
  }

  @Test
  void inviteUser_notOwner_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserEntity requester = UserEntity.builder().id(requesterId).build();
    HouseholdMemberEntity member = HouseholdMemberEntity.builder().role(MEMBER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(requester);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, requesterId)))
        .thenReturn(Optional.of(member));

    assertThatThrownBy(
            () -> householdService.inviteUser(householdId, jwt, "email@example.com", MEMBER))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("Only owners can invite users");
  }

  @Test
  void inviteUser_notMember_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserEntity requester = UserEntity.builder().id(requesterId).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(requester);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, requesterId)))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> householdService.inviteUser(householdId, jwt, "email@example.com", MEMBER))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("Only owners can invite users");
  }

  @Test
  void inviteUser_householdNotFound_throwsException() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UUID requesterId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    UserEntity requester = UserEntity.builder().id(requesterId).build();
    HouseholdMemberEntity member = HouseholdMemberEntity.builder().role(OWNER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(requester);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, requesterId)))
        .thenReturn(Optional.of(member));
    when(householdRepository.findById(householdId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> householdService.inviteUser(householdId, jwt, "email@example.com", MEMBER))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Household not found");
  }
}
