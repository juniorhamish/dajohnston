package uk.co.dajohnston.portal.household.entity;

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
import uk.co.dajohnston.portal.user.entity.UserEntity;

@Entity
@Table(name = "household_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HouseholdMemberEntity {

  @ManyToOne
  @MapsId("householdId")
  @JoinColumn(name = "household_id")
  public HouseholdEntity household;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  public UserEntity user;

  @Column(nullable = false)
  public String role;

  @EmbeddedId private HouseholdMemberId id;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;
}
