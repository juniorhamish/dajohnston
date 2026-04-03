package uk.co.dajohnston.portal.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "household_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMember {

  @EmbeddedId private HouseholdMemberId id;

  @ManyToOne
  @MapsId("householdId")
  @JoinColumn(name = "household_id")
  private Household household;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private String role;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;
}
