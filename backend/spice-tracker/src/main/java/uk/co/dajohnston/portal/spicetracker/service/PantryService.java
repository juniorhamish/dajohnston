package uk.co.dajohnston.portal.spicetracker.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarEntity;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarRepository;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class PantryService {

  public static final int FULL = 100;
  private final PantryJarRepository pantryJarRepository;
  private final SpiceRepository spiceRepository;

  private static PantryJarEntity buildJar(SpiceEntity spice, Integer quantity) {
    var jarQuantity = FULL;
    if (quantity != null) {
      jarQuantity = quantity;
    }
    return PantryJarEntity.builder()
        .id(UUID.randomUUID())
        .householdId(TenantContext.getTenantId())
        .spice(spice)
        .quantity(jarQuantity)
        .build();
  }

  @Transactional(readOnly = true)
  public List<PantryJarEntity> listPantryJars() {
    return pantryJarRepository.findAllByHouseholdId(TenantContext.getTenantId());
  }

  @Transactional
  public PantryJarEntity addPantryJar(UUID spiceId, Integer quantity) {
    return spiceRepository
        .findById(spiceId)
        .map((SpiceEntity spice) -> pantryJarRepository.save(buildJar(spice, quantity)))
        .orElseThrow(() -> new ResourceNotFoundException("Spice not found: " + spiceId));
  }

  @Transactional
  public PantryJarEntity updatePantryJar(UUID id, int quantity) {
    if (quantity < 0 || quantity > FULL) {
      throw new IllegalArgumentException("Quantity must be between 0 and 100");
    }
    return pantryJarRepository
        .findById(id)
        .map(
            (PantryJarEntity jar) -> {
              jar.setQuantity(quantity);
              return pantryJarRepository.save(jar);
            })
        .orElseThrow(() -> new ResourceNotFoundException("Jar not found: " + id));
  }

  @Transactional
  public void removePantryJar(UUID id) {
    pantryJarRepository.deleteById(id);
  }
}
