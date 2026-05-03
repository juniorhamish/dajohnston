package uk.co.dajohnston.portal.listz.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<ItemEntity, UUID> {
  Optional<ItemEntity> findByNameIgnoreCase(String name);

  List<ItemEntity> findByNameContainingIgnoreCase(String name);
}
