package uk.co.dajohnston.portal.spicetracker.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pantry_jars")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PantryJarEntity {

  @Id private UUID id;

  @Column(name = "household_id", nullable = false)
  private UUID householdId;

  @ManyToOne
  @JoinColumn(name = "spice_id", nullable = false)
  private SpiceEntity spice;

  @Column(nullable = false)
  private int quantity;
}
