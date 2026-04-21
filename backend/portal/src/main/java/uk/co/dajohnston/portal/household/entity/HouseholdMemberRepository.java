package uk.co.dajohnston.portal.household.entity;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface HouseholdMemberRepository
    extends JpaRepository<HouseholdMemberEntity, HouseholdMemberId> {
  List<HouseholdMemberEntity> findByUserId(UUID userId);

  @Modifying
  @Query(
      value =
          "UPDATE household_members SET deleted = false, deleted_at = NULL WHERE household_id = ?1",
      nativeQuery = true)
  void restoreByHouseholdId(UUID householdId);
}
