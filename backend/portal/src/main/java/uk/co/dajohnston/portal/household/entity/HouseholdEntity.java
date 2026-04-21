package uk.co.dajohnston.portal.household.entity;

import static jakarta.persistence.CascadeType.ALL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SoftDelete;

@Entity
@Table(name = "households")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SoftDelete
public class HouseholdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "deleted_at")
  private OffsetDateTime deletedAt;

  @ToString.Exclude
  @Builder.Default
  @OneToMany(mappedBy = "household", cascade = ALL, orphanRemoval = true)
  private List<HouseholdMemberEntity> members = new ArrayList<>();

  @PreRemove
  public void preRemove() {
    this.deletedAt = OffsetDateTime.now();
  }
}
