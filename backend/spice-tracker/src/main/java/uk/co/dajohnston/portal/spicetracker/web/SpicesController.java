package uk.co.dajohnston.portal.spicetracker.web;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.portal.spicetracker.api.SpicesApi;
import uk.co.dajohnston.portal.spicetracker.mapper.SpiceMapper;
import uk.co.dajohnston.portal.spicetracker.model.CreateSpiceDto;
import uk.co.dajohnston.portal.spicetracker.model.SpiceDto;
import uk.co.dajohnston.portal.spicetracker.model.SpicesDto;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.service.SpicesService;

@RestController
@RequiredArgsConstructor
@Slf4j
class SpicesController implements SpicesApi {

  private final SpicesService spicesService;
  private final SpiceMapper spiceMapper;

  @Override
  public ResponseEntity<SpicesDto> listSpices() {
    log.info("Listing spices");
    List<SpiceDto> spices = spicesService.listSpices().stream().map(spiceMapper::toDto).toList();
    return ResponseEntity.ok(SpicesDto.builder().spices(spices).build());
  }

  @Override
  public ResponseEntity<SpiceDto> createSpice(CreateSpiceDto createSpiceDto) {
    log.info("Creating spice: {}", createSpiceDto.name());
    SpiceEntity spice = spicesService.createSpice(createSpiceDto.name());
    SpiceDto dto = spiceMapper.toDto(spice);
    return ResponseEntity.created(URI.create("/api/spices/" + dto.id())).body(dto);
  }
}
