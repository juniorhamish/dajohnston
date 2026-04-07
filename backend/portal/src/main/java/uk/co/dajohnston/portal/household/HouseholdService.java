package uk.co.dajohnston.portal.household;

import static uk.co.dajohnston.portal.household.HouseholdRole.*;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

  private final HouseholdRepository householdRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final InvitationRepository invitationRepository;
  private final UserService userService;

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
    var email = jwt.getClaimAsString("email");
    HouseholdEntity household =
        householdRepository
            .findById(householdId)
            .orElseThrow(() -> new IllegalArgumentException("Household not found"));

    InvitationEntity invitation =
        invitationRepository
            .findByHouseholdIdAndEmail(householdId, email)
            .filter(i -> "PENDING".equals(i.getStatus()))
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
            .orElseThrow(() -> new IllegalArgumentException("Household not found"));
    InvitationEntity invitation =
        invitationRepository.save(
            InvitationEntity.builder()
                .household(household)
                .email(email)
                .role(role)
                .status("PENDING")
                .build());

    return Invitation.builder()
        .id(invitation.getId())
        .householdId(household.getId())
        .email(invitation.getEmail())
        .role(invitation.getRole())
        .status(invitation.getStatus())
        .build();
  }
}
