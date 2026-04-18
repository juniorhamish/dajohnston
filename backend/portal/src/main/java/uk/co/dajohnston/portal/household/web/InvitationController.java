package uk.co.dajohnston.portal.household.web;

import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.api.InvitationsApi;
import uk.co.dajohnston.model.InvitationDto;
import uk.co.dajohnston.model.InvitationsDto;
import uk.co.dajohnston.portal.household.HouseholdMapper;
import uk.co.dajohnston.portal.household.HouseholdService;

@RestController
@RequiredArgsConstructor
class InvitationController implements InvitationsApi {

  private final HouseholdService householdService;
  private final HouseholdMapper householdMapper;

  @Override
  public ResponseEntity<InvitationsDto> listPendingInvitations(
      @AuthenticationPrincipal JwtClaimAccessor jwt) {
    var invitations =
        householdService.listPendingInvitations(jwt).stream().map(householdMapper::toDto).toList();
    return ok(InvitationsDto.builder().invitations(invitations).build());
  }

  @Override
  public ResponseEntity<InvitationDto> acceptInvitation(
      UUID invitationId, @AuthenticationPrincipal JwtClaimAccessor jwt) {
    return ok(householdMapper.toDto(householdService.acceptInvitation(invitationId, jwt)));
  }

  @Override
  public ResponseEntity<InvitationDto> declineInvitation(
      UUID invitationId, @AuthenticationPrincipal JwtClaimAccessor jwt) {
    return ok(householdMapper.toDto(householdService.declineInvitation(invitationId, jwt)));
  }
}
