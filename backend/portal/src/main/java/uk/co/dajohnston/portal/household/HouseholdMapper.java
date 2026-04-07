package uk.co.dajohnston.portal.household;

import org.mapstruct.Mapper;
import uk.co.dajohnston.model.HouseholdDto;
import uk.co.dajohnston.model.InvitationDto;

@Mapper
public interface HouseholdMapper {
  HouseholdDto toDto(Household household);

  InvitationDto toDto(Invitation invitation);
}
