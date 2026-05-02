package uk.co.dajohnston.portal.spicetracker.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceRepository;
import uk.co.dajohnston.security.context.TenantContext;

@Service
@RequiredArgsConstructor
public class SpicesService {

  private final SpiceRepository spiceRepository;

  @Transactional(readOnly = true)
  public List<SpiceEntity> listSpices() {
    return spiceRepository.findAllByHouseholdId(TenantContext.getTenantId());
  }

  @Transactional
  public SpiceEntity createSpice(String name) {
    SpiceEntity spice =
        SpiceEntity.builder()
            .id(UUID.randomUUID())
            .householdId(TenantContext.getTenantId())
            .name(name)
            .build();
    return spiceRepository.save(spice);
  }
}
