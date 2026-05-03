package uk.co.dajohnston.portal.listz.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateItemRepository extends JpaRepository<TemplateItemEntity, UUID> {}
