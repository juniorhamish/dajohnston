package uk.co.dajohnston.portal.user;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import uk.co.dajohnston.portal.household.Household;

@Builder
public record UserProfile(
    UUID id, String auth0Id, String email, String displayName, List<Household> households) {}
