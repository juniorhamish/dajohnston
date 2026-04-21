package uk.co.dajohnston.portal.household.entity;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HouseholdRepository extends JpaRepository<HouseholdEntity, UUID> {
  @Modifying
  @Query(
      value = "UPDATE households SET deleted = false, deleted_at = NULL WHERE id = ?1",
      nativeQuery = true)
  void restoreById(UUID id);
}
