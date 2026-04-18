package uk.co.dajohnston.portal.household.entity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<InvitationEntity, UUID> {
  Optional<InvitationEntity> findByHouseholdIdAndEmail(UUID householdId, String email);

  List<InvitationEntity> findByEmailAndStatus(String email, String status);
}
