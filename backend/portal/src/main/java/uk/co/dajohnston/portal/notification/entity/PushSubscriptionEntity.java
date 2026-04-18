package uk.co.dajohnston.portal.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@Entity
@Table(name = "push_subscriptions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushSubscriptionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserEntity user;

  @Column(nullable = false)
  private String endpoint;

  @Column(nullable = false)
  private String p256dh;

  @Column(nullable = false)
  private String auth;

  @Column(name = "expiration_time")
  private Long expirationTime;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
