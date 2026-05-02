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
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

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

    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.findByUserId(userId)).thenReturn(List.of(member1, member2));

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
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.findByUserId(userId)).thenReturn(List.of());

    List<Household> result = householdService.listHouseholds(jwt);

    assertThat(result).isEmpty();
  }

  @Test
  void listHouseholds_filtersOutMembershipsWithNullHousehold() {
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);

    HouseholdMemberEntity memberWithHousehold =
        HouseholdMemberEntity.builder()
            .household(
                HouseholdEntity.builder()
                    .id(UUID.fromString("12345678-1234-1234-1234-123456789012"))
                    .name("House")
                    .build())
            .role(MEMBER)
            .build();
    HouseholdMemberEntity memberWithoutHousehold =
        HouseholdMemberEntity.builder().household(null).role(MEMBER).build();

    when(householdMemberRepository.findByUserId(userId))
        .thenReturn(List.of(memberWithHousehold, memberWithoutHousehold));

    List<Household> result = householdService.listHouseholds(jwt);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().name()).isEqualTo("House");
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

  @Test
  void listPendingInvitations_returnsInvitationsForEmail() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000010");
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000011");
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Test House").build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("user@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findByEmailAndStatus("user@example.com", "PENDING"))
        .thenReturn(List.of(invitation));

    List<Invitation> result = householdService.listPendingInvitations(jwt);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().id()).isEqualTo(invitationId);
    assertThat(result.getFirst().householdId()).isEqualTo(householdId);
    assertThat(result.getFirst().householdName()).isEqualTo("Test House");
    assertThat(result.getFirst().email()).isEqualTo("user@example.com");
    assertThat(result.getFirst().role()).isEqualTo(MEMBER);
    assertThat(result.getFirst().status()).isEqualTo("PENDING");
  }

  @Test
  void listPendingInvitations_returnsEmptyList_whenNoPending() {
    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findByEmailAndStatus("user@example.com", "PENDING"))
        .thenReturn(List.of());

    List<Invitation> result = householdService.listPendingInvitations(jwt);

    assertThat(result).isEmpty();
  }

  @Test
  void acceptInvitation_success() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000020");
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000021");
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000022");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Accept House").build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("user@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

    Invitation result = householdService.acceptInvitation(invitationId, jwt);

    assertThat(result.id()).isEqualTo(invitationId);
    assertThat(result.householdId()).isEqualTo(householdId);
    assertThat(result.householdName()).isEqualTo("Accept House");
    assertThat(result.status()).isEqualTo("ACCEPTED");
    verify(householdMemberRepository).save(any(HouseholdMemberEntity.class));
    verify(invitationRepository).save(invitation);
  }

  @Test
  void acceptInvitation_notFound_throwsException() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000030");

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> householdService.acceptInvitation(invitationId, jwt))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Invitation not found");
  }

  @Test
  void acceptInvitation_wrongEmail_throwsException() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000031");
    HouseholdEntity household = HouseholdEntity.builder().build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("other@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

    assertThatThrownBy(() -> householdService.acceptInvitation(invitationId, jwt))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Invitation not found");
  }

  @Test
  void acceptInvitation_alreadyAccepted_throwsException() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000032");
    HouseholdEntity household = HouseholdEntity.builder().build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("user@example.com")
            .role(MEMBER)
            .status("ACCEPTED")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

    assertThatThrownBy(() -> householdService.acceptInvitation(invitationId, jwt))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Invitation not found");
  }

  @Test
  void declineInvitation_success() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000040");
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000041");
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Decline House").build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("user@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

    Invitation result = householdService.declineInvitation(invitationId, jwt);

    assertThat(result.id()).isEqualTo(invitationId);
    assertThat(result.householdId()).isEqualTo(householdId);
    assertThat(result.householdName()).isEqualTo("Decline House");
    assertThat(result.status()).isEqualTo("DECLINED");
    verify(invitationRepository).save(invitation);
  }

  @Test
  void declineInvitation_notFound_throwsException() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000050");

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> householdService.declineInvitation(invitationId, jwt))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Invitation not found");
  }

  @Test
  void declineInvitation_wrongEmail_throwsException() {
    UUID invitationId = UUID.fromString("00000000-0000-0000-0000-000000000051");
    HouseholdEntity household = HouseholdEntity.builder().build();
    InvitationEntity invitation =
        InvitationEntity.builder()
            .id(invitationId)
            .household(household)
            .email("other@example.com")
            .role(MEMBER)
            .status("PENDING")
            .build();

    when(userService.findOrCreateUser(jwt)).thenReturn(UserEntity.builder().build());
    when(jwt.getClaimAsString("email")).thenReturn("user@example.com");
    when(invitationRepository.findById(invitationId)).thenReturn(Optional.of(invitation));

    assertThatThrownBy(() -> householdService.declineInvitation(invitationId, jwt))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Invitation not found");
  }

  @Test
  void deleteHousehold_success_asOwner() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000060");
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000061");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Delete House").build();
    HouseholdMemberEntity member =
        HouseholdMemberEntity.builder().household(household).user(user).role(OWNER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(Optional.of(member));
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));

    householdService.deleteHousehold(householdId, jwt);

    household.preRemove();
    verify(householdRepository).delete(household);
    assertThat(household.getDeletedAt()).isNotNull();
  }

  @Test
  void deleteHousehold_fails_asMember() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000070");
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000071");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Member House").build();
    HouseholdMemberEntity member =
        HouseholdMemberEntity.builder().household(household).user(user).role(MEMBER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(Optional.of(member));

    assertThatThrownBy(() -> householdService.deleteHousehold(householdId, jwt))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("Only owners can delete households");
  }

  @Test
  void restoreHousehold_success() {
    UUID householdId = UUID.fromString("00000000-0000-0000-0000-000000000080");
    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000081");
    UserEntity user = UserEntity.builder().id(userId).build();
    HouseholdEntity household =
        HouseholdEntity.builder().id(householdId).name("Restored House").build();
    HouseholdMemberEntity member =
        HouseholdMemberEntity.builder().household(household).user(user).role(OWNER).build();

    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdRepository.findById(householdId)).thenReturn(Optional.of(household));
    when(householdMemberRepository.findById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(Optional.of(member));

    Household result = householdService.restoreHousehold(householdId, jwt);

    verify(householdRepository).restoreById(householdId);
    assertThat(result.id()).isEqualTo(householdId);
    assertThat(result.name()).isEqualTo("Restored House");
    assertThat(result.role()).isEqualTo(OWNER);
  }
}
