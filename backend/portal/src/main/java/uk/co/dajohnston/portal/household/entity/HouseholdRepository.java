package uk.co.dajohnston.portal.household.entity;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseholdRepository extends JpaRepository<HouseholdEntity, UUID> {}
