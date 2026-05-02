package uk.co.dajohnston.portal.spicetracker.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarEntity;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarRepository;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class PantryServiceTest {

  private final UUID householdId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
  @Mock private PantryJarRepository pantryJarRepository;
  @Mock private SpiceRepository spiceRepository;
  @InjectMocks private PantryService pantryService;
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
  void listPantryJars_returnsJarsFromRepository() {
    PantryJarEntity jar =
        PantryJarEntity.builder()
            .id(UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e"))
            .householdId(householdId)
            .quantity(50)
            .build();
    when(pantryJarRepository.findAllByHouseholdId(householdId)).thenReturn(List.of(jar));

    List<PantryJarEntity> result = pantryService.listPantryJars();

    assertThat(result).containsExactly(jar);
  }

  @Test
  void addPantryJar_savesJarToRepository() {
    UUID spiceId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    SpiceEntity spice = SpiceEntity.builder().id(spiceId).name("Cinnamon").build();
    when(spiceRepository.findById(spiceId)).thenReturn(Optional.of(spice));
    when(pantryJarRepository.save(any(PantryJarEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PantryJarEntity result = pantryService.addPantryJar(spiceId, 75);

    assertThat(result.getSpice()).isEqualTo(spice);
    assertThat(result.getQuantity()).isEqualTo(75);
    assertThat(result.getHouseholdId()).isEqualTo(householdId);
  }

  @Test
  void addPantryJar_defaultsQuantityTo100() {
    UUID spiceId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    SpiceEntity spice = SpiceEntity.builder().id(spiceId).name("Cinnamon").build();
    when(spiceRepository.findById(spiceId)).thenReturn(Optional.of(spice));
    when(pantryJarRepository.save(any(PantryJarEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PantryJarEntity result = pantryService.addPantryJar(spiceId, null);

    assertThat(result.getQuantity()).isEqualTo(100);
  }

  @Test
  void addPantryJar_throwsExceptionIfSpiceNotFound() {
    UUID spiceId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    when(spiceRepository.findById(spiceId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pantryService.addPantryJar(spiceId, 100))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Spice not found");
  }

  @Test
  void updatePantryJar_updatesQuantity() {
    UUID jarId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    PantryJarEntity jar = PantryJarEntity.builder().id(jarId).quantity(50).build();
    when(pantryJarRepository.findById(jarId)).thenReturn(Optional.of(jar));
    when(pantryJarRepository.save(any(PantryJarEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PantryJarEntity result = pantryService.updatePantryJar(jarId, 25);

    assertThat(result.getQuantity()).isEqualTo(25);
  }

  @Test
  void updatePantryJar_throwsExceptionIfJarNotFound() {
    UUID jarId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
    when(pantryJarRepository.findById(jarId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> pantryService.updatePantryJar(jarId, 25))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Jar not found");
  }

  @Test
  void updatePantryJar_throwsExceptionIfQuantityInvalid() {
    UUID jarId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");

    assertThatThrownBy(() -> pantryService.updatePantryJar(jarId, 101))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Quantity must be between 0 and 100");

    assertThatThrownBy(() -> pantryService.updatePantryJar(jarId, -1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Quantity must be between 0 and 100");
  }

  @Test
  void removePantryJar_deletesFromRepository() {
    UUID jarId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");

    pantryService.removePantryJar(jarId);

    verify(pantryJarRepository).deleteById(jarId);
  }
}
