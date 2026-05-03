package uk.co.dajohnston.portal.spicetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarRepository;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.DuplicateResourceException;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class SpicesServiceTest {

  private final UUID householdId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
  @Mock private SpiceRepository spiceRepository;
  @Mock private PantryJarRepository pantryJarRepository;
  @InjectMocks private SpicesService spicesService;
  private MockedStatic<TenantContext> tenantContextMock;

  @BeforeEach
  void setUp() {
    tenantContextMock = mockStatic(TenantContext.class);
    tenantContextMock.when(TenantContext::getTenantId).thenReturn(householdId);
  }

  @AfterEach
  void tearDown() {
    tenantContextMock.close();
  }

  @Test
  void listSpices_returnsSpicesFromRepository() {
    SpiceEntity spice =
        SpiceEntity.builder()
            .id(UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e"))
            .householdId(householdId)
            .name("Cinnamon")
            .build();
    when(spiceRepository.findAllByHouseholdId(householdId)).thenReturn(List.of(spice));

    List<SpiceEntity> result = spicesService.listSpices();

    assertThat(result).containsExactly(spice);
  }

  @Test
  void createSpice_savesSpiceToRepository() {
    when(spiceRepository.existsByHouseholdIdAndName(householdId, "Cumin")).thenReturn(false);
    when(spiceRepository.save(any(SpiceEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    SpiceEntity result = spicesService.createSpice("Cumin");

    assertThat(result.getName()).isEqualTo("Cumin");
    assertThat(result.getHouseholdId()).isEqualTo(householdId);
    assertThat(result.getId()).isNotNull();
    verify(spiceRepository).save(any(SpiceEntity.class));
  }

  @Test
  void createSpice_duplicateName_throwsException() {
    when(spiceRepository.existsByHouseholdIdAndName(householdId, "Cumin")).thenReturn(true);

    assertThatThrownBy(() -> spicesService.createSpice("Cumin"))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Spice with name Cumin already exists");
  }

  @Test
  void removeSpice_deletesFromRepository() {
    UUID spiceId = UUID.randomUUID();
    SpiceEntity spice = SpiceEntity.builder().id(spiceId).householdId(householdId).build();
    when(spiceRepository.findByHouseholdIdAndId(householdId, spiceId))
        .thenReturn(java.util.Optional.of(spice));

    spicesService.removeSpice(spiceId);

    verify(pantryJarRepository).deleteAllBySpice(spice);
    verify(spiceRepository).delete(spice);
  }

  @Test
  void removeSpice_notFound_throwsException() {
    UUID spiceId = UUID.randomUUID();
    when(spiceRepository.findByHouseholdIdAndId(householdId, spiceId))
        .thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> spicesService.removeSpice(spiceId))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Spice not found");
  }
}
