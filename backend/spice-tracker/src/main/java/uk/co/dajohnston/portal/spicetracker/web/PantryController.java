package uk.co.dajohnston.portal.spicetracker.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.portal.spicetracker.api.PantryApi;
import uk.co.dajohnston.portal.spicetracker.mapper.PantryJarMapper;
import uk.co.dajohnston.portal.spicetracker.model.AddPantryJarDto;
import uk.co.dajohnston.portal.spicetracker.model.PantryJarDto;
import uk.co.dajohnston.portal.spicetracker.model.PantryJarsDto;
import uk.co.dajohnston.portal.spicetracker.model.UpdatePantryJarDto;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarEntity;
import uk.co.dajohnston.portal.spicetracker.service.PantryService;

@RestController
@RequiredArgsConstructor
@Slf4j
class PantryController implements PantryApi {

  private final PantryService pantryService;
  private final PantryJarMapper pantryJarMapper;

  @Override
  public ResponseEntity<PantryJarsDto> listPantryJars() {
    log.info("Listing pantry jars");
    List<PantryJarDto> jars =
        pantryService.listPantryJars().stream().map(pantryJarMapper::toDto).toList();
    return ResponseEntity.ok(PantryJarsDto.builder().jars(jars).build());
  }

  @Override
  public ResponseEntity<PantryJarDto> addPantryJar(AddPantryJarDto addPantryJarDto) {
    log.info("Adding pantry jar for spice: {}", addPantryJarDto.spiceId());
    PantryJarEntity jar =
        pantryService.addPantryJar(addPantryJarDto.spiceId(), addPantryJarDto.quantity());
    PantryJarDto dto = pantryJarMapper.toDto(jar);
    return ResponseEntity.created(URI.create("/api/pantry/" + dto.id())).body(dto);
  }

  @Override
  public ResponseEntity<PantryJarDto> updatePantryJar(
      UUID id, UpdatePantryJarDto updatePantryJarDto) {
    log.info("Updating pantry jar: {}", id);
    PantryJarEntity jar = pantryService.updatePantryJar(id, updatePantryJarDto.quantity());
    return ResponseEntity.ok(pantryJarMapper.toDto(jar));
  }

  @Override
  public ResponseEntity<Void> removePantryJar(UUID id) {
    log.info("Removing pantry jar: {}", id);
    pantryService.removePantryJar(id);
    return ResponseEntity.noContent().build();
  }
}
