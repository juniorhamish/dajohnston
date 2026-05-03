package uk.co.dajohnston.portal.listz.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, UUID> {
  List<TemplateEntity> findAllByDeletedAtIsNull();
}
