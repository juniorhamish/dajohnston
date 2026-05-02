package uk.co.dajohnston.portal.household;

import static uk.co.dajohnston.portal.household.HouseholdRole.*;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.config.ResourceNotFoundException;
import uk.co.dajohnston.portal.household.entity.HouseholdEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberId;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.household.entity.HouseholdRepository;
import uk.co.dajohnston.portal.household.entity.InvitationEntity;
import uk.co.dajohnston.portal.household.entity.InvitationRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
@Transactional
public class HouseholdService {

  public static final String EMAIL_CLAIM = "email";
  public static final String PENDING_STATUS = "PENDING";
  public static final String HOUSEHOLD_NOT_FOUND = "Household not found";
  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final InvitationRepository invitationRepository;
  private final UserService userService;

  private static Invitation toInvitation(InvitationEntity entity) {
    return Invitation.builder()
        .id(entity.getId())
        .householdId(entity.getHousehold().getId())
        .householdName(entity.getHousehold().getName())
        .email(entity.getEmail())
        .role(entity.getRole())
        .status(entity.getStatus())
        .build();
  }

  @Transactional(readOnly = true)
  public List<Household> listHouseholds(JwtClaimAccessor jwt) {
    UserEntity user = userService.findOrCreateUser(jwt);
    return householdMemberRepository.findByUserId(user.getId()).stream()
        .filter(member -> member.getHousehold() != null)
        .map(
            member ->
                Household.builder()
                    .id(member.getHousehold().getId())
                    .name(member.getHousehold().getName())
                    .role(member.getRole())
                    .build())
        .toList();
  }

  public Household createHousehold(JwtClaimAccessor jwt, String name) {
    UserEntity user = userService.findOrCreateUser(jwt);
    HouseholdEntity household =
        householdRepository.save(HouseholdEntity.builder().name(name).build());

    householdMemberRepository.save(
        HouseholdMemberEntity.builder()
            .id(new HouseholdMemberId(household.getId(), user.getId()))
            .household(household)
            .user(user)
            .role(OWNER)
            .build());

    return Household.builder().id(household.getId()).name(household.getName()).role(OWNER).build();
  }

  public Household joinHousehold(UUID householdId, JwtClaimAccessor jwt) {
    UserEntity user = userService.findOrCreateUser(jwt);
    var email = jwt.getClaimAsString(EMAIL_CLAIM);
    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new IllegalArgumentException(HOUSEHOLD_NOT_FOUND));

    InvitationEntity invitation =
        invitationRepository
            .findByHouseholdIdAndEmail(householdId, email)
            .filter(i -> PENDING_STATUS.equals(i.getStatus()))
            .orElseThrow(
                () -> new AccessDeniedException("No pending invitation found for this household"));

    householdMemberRepository.save(
        HouseholdMemberEntity.builder()
            .id(new HouseholdMemberId(household.getId(), user.getId()))
            .household(household)
            .user(user)
            .role(invitation.getRole())
            .build());

    invitation.setStatus("ACCEPTED");
    invitationRepository.save(invitation);

    return Household.builder()
        .id(household.getId())
        .name(household.getName())
        .role(invitation.getRole())
        .build();
  }

  public Invitation inviteUser(
      UUID householdId, JwtClaimAccessor jwt, String email, HouseholdRole role) {
    UserEntity requester = userService.findOrCreateUser(jwt);

    boolean isOwner =
        householdMemberRepository
            .findById(new HouseholdMemberId(householdId, requester.getId()))
            .map(member -> OWNER == member.getRole())
            .orElse(false);

    if (!isOwner) {
      throw new AccessDeniedException("Only owners can invite users");
    }

    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new IllegalArgumentException(HOUSEHOLD_NOT_FOUND));
    InvitationEntity invitation =
        invitationRepository.save(
            InvitationEntity.builder()
                .household(household)
                .email(email)
                .role(role)
                .status(PENDING_STATUS)
                .build());

    return Invitation.builder()
        .id(invitation.getId())
        .householdId(household.getId())
        .householdName(household.getName())
        .email(invitation.getEmail())
        .role(invitation.getRole())
        .status(invitation.getStatus())
        .build();
  }

  @Transactional(readOnly = true)
  public boolean isUserMemberOfHousehold(UUID userId, UUID householdId) {
    return householdMemberRepository.existsById(new HouseholdMemberId(householdId, userId));
  }

  @Transactional(readOnly = true)
  public List<Invitation> listPendingInvitations(JwtClaimAccessor jwt) {
    userService.findOrCreateUser(jwt);
    var email = jwt.getClaimAsString(EMAIL_CLAIM);
    return invitationRepository.findByEmailAndStatus(email, PENDING_STATUS).stream()
        .map(HouseholdService::toInvitation)
        .toList();
  }

  public Invitation acceptInvitation(UUID invitationId, JwtClaimAccessor jwt) {
    UserEntity user = userService.findOrCreateUser(jwt);
    var email = jwt.getClaimAsString(EMAIL_CLAIM);

    InvitationEntity invitation =
        invitationRepository
            .findById(invitationId)
            .filter(i -> PENDING_STATUS.equals(i.getStatus()))
            .filter(i -> email.equals(i.getEmail()))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

    HouseholdEntity household = invitation.getHousehold();

    householdMemberRepository.save(
        HouseholdMemberEntity.builder()
            .id(new HouseholdMemberId(household.getId(), user.getId()))
            .household(household)
            .user(user)
            .role(invitation.getRole())
            .build());

    invitation.setStatus("ACCEPTED");
    invitationRepository.save(invitation);

    return toInvitation(invitation);
  }

  public Invitation declineInvitation(UUID invitationId, JwtClaimAccessor jwt) {
    userService.findOrCreateUser(jwt);
    var email = jwt.getClaimAsString(EMAIL_CLAIM);

    InvitationEntity invitation =
        invitationRepository
            .findById(invitationId)
            .filter(i -> PENDING_STATUS.equals(i.getStatus()))
            .filter(i -> email.equals(i.getEmail()))
            .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));

    invitation.setStatus("DECLINED");
    invitationRepository.save(invitation);

    return toInvitation(invitation);
  }

  public void deleteHousehold(UUID householdId, JwtClaimAccessor jwt) {
    UserEntity user = userService.findOrCreateUser(jwt);

    boolean isOwner =
        householdMemberRepository
            .findById(new HouseholdMemberId(householdId, user.getId()))
            .map(member -> OWNER == member.getRole())
            .orElse(false);

    if (!isOwner) {
      throw new AccessDeniedException("Only owners can delete households");
    }

    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new ResourceNotFoundException(HOUSEHOLD_NOT_FOUND));

    householdRepository.delete(household);
  }

  public Household restoreHousehold(UUID householdId, JwtClaimAccessor jwt) {
    UserEntity user = userService.findOrCreateUser(jwt);
    householdRepository.restoreById(householdId);
    householdMemberRepository.restoreByHouseholdId(householdId);
    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new ResourceNotFoundException(HOUSEHOLD_NOT_FOUND));

    HouseholdRole role =
        householdMemberRepository
            .findById(new HouseholdMemberId(householdId, user.getId()))
            .map(HouseholdMemberEntity::getRole)
            .orElse(MEMBER);

    return Household.builder().id(household.getId()).name(household.getName()).role(role).build();
  }
}
