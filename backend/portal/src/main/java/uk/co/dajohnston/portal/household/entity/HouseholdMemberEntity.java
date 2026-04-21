package uk.co.dajohnston.portal.household.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.type.SqlTypes;
import uk.co.dajohnston.portal.household.HouseholdRole;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@Entity
@Table(name = "household_members")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete
public class HouseholdMemberEntity {

  @ToString.Exclude
  @ManyToOne
  @MapsId("householdId")
  @JoinColumn(name = "household_id")
  public HouseholdEntity household;

  @ManyToOne
  @MapsId("userId")
  @JoinColumn(name = "user_id")
  public UserEntity user;

  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(nullable = false)
  public HouseholdRole role;

  @EmbeddedId private HouseholdMemberId id;

  @CreationTimestamp
  @Column(name = "joined_at", nullable = false, updatable = false)
  private OffsetDateTime joinedAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @PreRemove
  public void preRemove() {
    this.deletedAt = OffsetDateTime.now();
  }
}
