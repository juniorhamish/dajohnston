package uk.co.dajohnston.portal.spicetracker.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PantryJarRepository extends JpaRepository<PantryJarEntity, UUID> {
  List<PantryJarEntity> findAllByHouseholdId(UUID householdId);

  void deleteAllBySpice(SpiceEntity spice);
}
