package uk.co.dajohnston.portal.household;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Invitation(
    UUID id,
    UUID householdId,
    String householdName,
    String email,
    HouseholdRole role,
    String status) {}
