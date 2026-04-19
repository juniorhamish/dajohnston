package uk.co.dajohnston.portal.notification.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.dajohnston.portal.user.entity.UserEntity;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscriptionEntity, UUID> {
  Optional<PushSubscriptionEntity> findByEndpoint(String endpoint);

  List<PushSubscriptionEntity> findByUser(UserEntity user);
}
