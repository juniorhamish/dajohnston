package uk.co.dajohnston.portal.app.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.api.AppsApi;
import uk.co.dajohnston.model.AppsDto;
import uk.co.dajohnston.portal.app.App;
import uk.co.dajohnston.portal.app.AppMapper;
import uk.co.dajohnston.portal.app.AppService;

@RestController
@RequiredArgsConstructor
class AppController implements AppsApi {

  private final AppService appService;
  private final AppMapper appMapper;

  @Override
  public ResponseEntity<AppsDto> listApps() {
    List<App> apps = appService.listActiveApps();
    return ResponseEntity.ok(
        AppsDto.builder().apps(apps.stream().map(appMapper::toDto).toList()).build());
  }
}
