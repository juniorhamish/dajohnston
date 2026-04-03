package uk.co.dajohnston.portal.dto;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
  private UUID id;
  private String auth0Id;
  private String email;
  private String displayName;
  private List<HouseholdDto> households;

  @Data
  @Builder
  public static class HouseholdDto {
    private UUID id;
    private String name;
    private String role;
  }
}
