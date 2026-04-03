package uk.co.dajohnston.portal.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.dajohnston.portal.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByAuth0Id(String auth0Id);
}
