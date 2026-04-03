package uk.co.dajohnston.portal.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.co.dajohnston.portal.model.Household;

public interface HouseholdRepository extends JpaRepository<Household, UUID> {}
