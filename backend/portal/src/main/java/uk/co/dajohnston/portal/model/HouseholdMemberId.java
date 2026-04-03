package uk.co.dajohnston.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberId implements Serializable {

  @Column(name = "household_id")
  private UUID householdId;

  @Column(name = "user_id")
  private UUID userId;
}
