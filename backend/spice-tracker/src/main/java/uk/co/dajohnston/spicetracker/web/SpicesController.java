package uk.co.dajohnston.spicetracker.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.spicetracker.api.SpicesApi;
import uk.co.dajohnston.spicetracker.model.SpicesDto;

@RestController
@RequiredArgsConstructor
@Slf4j
class SpicesController implements SpicesApi {

  @Override
  public ResponseEntity<SpicesDto> listSpices() {
    log.info("Listing spices for household: {}", TenantContext.getTenantId());
    return ResponseEntity.ok(SpicesDto.builder().spices(List.of()).build());
  }
}
