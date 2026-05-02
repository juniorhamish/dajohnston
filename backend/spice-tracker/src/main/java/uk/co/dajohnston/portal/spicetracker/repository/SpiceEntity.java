package uk.co.dajohnston.portal.spicetracker.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "spices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpiceEntity {

  @Id private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @Column(nullable = false)
  private String name;
}
