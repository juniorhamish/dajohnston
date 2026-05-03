package uk.co.dajohnston.portal.spicetracker.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarRepository;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.DuplicateResourceException;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class SpicesService {

  private final SpiceRepository spiceRepository;
  private final PantryJarRepository pantryJarRepository;

  @Transactional(readOnly = true)
  public List<SpiceEntity> listSpices() {
    return spiceRepository.findAllByHouseholdId(TenantContext.getTenantId());
  }

  @Transactional
  public SpiceEntity createSpice(String name) {
    if (spiceRepository.existsByHouseholdIdAndName(TenantContext.getTenantId(), name)) {
      throw new DuplicateResourceException("Spice with name %s already exists".formatted(name));
    }
    SpiceEntity spice =
        SpiceEntity.builder()
            .id(UUID.randomUUID())
            .householdId(TenantContext.getTenantId())
            .name(name)
            .build();
    return spiceRepository.save(spice);
  }

  @Transactional
  public void removeSpice(UUID id) {
    SpiceEntity spice =
        spiceRepository
            .findByHouseholdIdAndId(TenantContext.getTenantId(), id)
            .orElseThrow(() -> new ResourceNotFoundException("Spice not found"));
    pantryJarRepository.deleteAllBySpice(spice);
    spiceRepository.delete(spice);
  }
}
