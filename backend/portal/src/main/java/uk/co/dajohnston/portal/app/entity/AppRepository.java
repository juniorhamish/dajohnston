package uk.co.dajohnston.portal.app.entity;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppRepository extends JpaRepository<AppEntity, String> {
  List<AppEntity> findByActiveTrue();
}
