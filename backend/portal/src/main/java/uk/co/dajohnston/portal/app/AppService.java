package uk.co.dajohnston.portal.app;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.app.entity.AppRepository;

@Service
@RequiredArgsConstructor
public class AppService {

  private final AppRepository appRepository;
  private final AppMapper appMapper;

  @Transactional(readOnly = true)
  public List<App> listActiveApps() {
    return appRepository.findByActiveTrue().stream().map(appMapper::toDomain).toList();
  }
}
