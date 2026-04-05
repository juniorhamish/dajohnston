package uk.co.dajohnston.portal.household;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Household(UUID id, String name, String role) {}
