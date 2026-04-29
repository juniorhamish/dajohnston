package uk.co.dajohnston.spicetracker.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.spicetracker.api.SpicesApi;
import uk.co.dajohnston.spicetracker.model.SpicesDto;

@RestController
@RequiredArgsConstructor
class SpicesController implements SpicesApi {

  @Override
  public ResponseEntity<SpicesDto> listSpices() {
    return ResponseEntity.ok(SpicesDto.builder().spices(List.of()).build());
  }
}
