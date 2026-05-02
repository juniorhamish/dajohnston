package uk.co.dajohnston.portal.spicetracker.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpiceRepository extends JpaRepository<SpiceEntity, UUID> {
  List<SpiceEntity> findAllByHouseholdId(UUID householdId);

  boolean existsByHouseholdIdAndName(UUID householdId, String name);
}
