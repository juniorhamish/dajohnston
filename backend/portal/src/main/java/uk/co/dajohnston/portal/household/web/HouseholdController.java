package uk.co.dajohnston.portal.household.web;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.api.HouseholdsApi;
import uk.co.dajohnston.model.CreateHouseholdRequestDto;
import uk.co.dajohnston.model.HouseholdDto;
import uk.co.dajohnston.model.InvitationDto;
import uk.co.dajohnston.model.InviteUserRequestDto;
import uk.co.dajohnston.portal.household.HouseholdMapper;
import uk.co.dajohnston.portal.household.HouseholdRole;
import uk.co.dajohnston.portal.household.HouseholdService;

@RestController
@RequiredArgsConstructor
class HouseholdController implements HouseholdsApi {

  private final HouseholdService householdService;
  private final HouseholdMapper householdMapper;

  @Override
  public ResponseEntity<HouseholdDto> createHousehold(
      @AuthenticationPrincipal JwtClaimAccessor jwt, CreateHouseholdRequestDto body) {
    return status(CREATED)
        .body(householdMapper.toDto(householdService.createHousehold(jwt, body.name())));
  }

  @Override
  public ResponseEntity<HouseholdDto> joinHousehold(
      UUID householdId, @AuthenticationPrincipal JwtClaimAccessor jwt) {
    return ok(householdMapper.toDto(householdService.joinHousehold(householdId, jwt)));
  }

  @Override
  public ResponseEntity<InvitationDto> inviteUser(
      UUID householdId, @AuthenticationPrincipal JwtClaimAccessor jwt, InviteUserRequestDto body) {
    return status(CREATED)
        .body(
            householdMapper.toDto(
                householdService.inviteUser(
                    householdId, jwt, body.email(), HouseholdRole.valueOf(body.role().name()))));
  }
}
