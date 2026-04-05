package uk.co.dajohnston.portal.household.entity;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdMemberRepository
    extends JpaRepository<HouseholdMemberEntity, HouseholdMemberId> {
  List<HouseholdMemberEntity> findByUserId(UUID userId);
}
