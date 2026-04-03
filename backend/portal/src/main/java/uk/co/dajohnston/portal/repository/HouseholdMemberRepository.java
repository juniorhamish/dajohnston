package uk.co.dajohnston.portal.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.dajohnston.portal.model.HouseholdMember;
import uk.co.dajohnston.portal.model.HouseholdMemberId;

public interface HouseholdMemberRepository
    extends JpaRepository<HouseholdMember, HouseholdMemberId> {
  List<HouseholdMember> findByUserId(UUID userId);
}
