package uk.co.dajohnston.portal.listz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import uk.co.dajohnston.portal.listz.repository.ItemEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateItemEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateItemRepository;
import uk.co.dajohnston.portal.listz.repository.TemplateRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ListzTemplateServiceTest {

  private final UUID householdId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
  @Mock private TemplateRepository templateRepository;
  @Mock private TemplateItemRepository templateItemRepository;
  @Mock private ListzItemService itemService;
  @InjectMocks private ListzTemplateService templateService;
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
  void listTemplates_returnsActiveTemplates() {
    TemplateEntity template = TemplateEntity.builder().name("Template 1").build();
    when(templateRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(template));

    List<TemplateEntity> result = templateService.listTemplates();

    assertThat(result).containsExactly(template);
  }

  @Test
  void createTemplate_savesNewTemplate() {
    when(templateRepository.save(any(TemplateEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TemplateEntity result = templateService.createTemplate("Holiday");

    assertThat(result.getName()).isEqualTo("Holiday");
    assertThat(result.getHouseholdId()).isEqualTo(householdId);
    verify(templateRepository).save(any(TemplateEntity.class));
  }

  @Test
  void getTemplate_returnsTemplate_whenExistsAndNotDeleted() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateEntity template = TemplateEntity.builder().id(id).name("Test").build();
    when(templateRepository.findById(id)).thenReturn(Optional.of(template));

    TemplateEntity result = templateService.getTemplate(id);

    assertThat(result).isEqualTo(template);
  }

  @Test
  void getTemplate_throwsException_whenDeleted() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateEntity template =
        TemplateEntity.builder()
            .id(id)
            .name("Test")
            .deletedAt(java.time.OffsetDateTime.now())
            .build();
    when(templateRepository.findById(id)).thenReturn(Optional.of(template));

    assertThatThrownBy(() -> templateService.getTemplate(id))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void updateTemplate_updatesName() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateEntity template = TemplateEntity.builder().id(id).name("Old").build();
    when(templateRepository.findById(id)).thenReturn(Optional.of(template));
    when(templateRepository.save(any(TemplateEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TemplateEntity result = templateService.updateTemplate(id, "New");

    assertThat(result.getName()).isEqualTo("New");
    verify(templateRepository).save(template);
  }

  @Test
  void deleteTemplate_setsDeletedAt() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateEntity template = TemplateEntity.builder().id(id).name("Test").build();
    when(templateRepository.findById(id)).thenReturn(Optional.of(template));

    templateService.deleteTemplate(id);

    assertThat(template.getDeletedAt()).isNotNull();
    verify(templateRepository).save(template);
  }

  @Test
  void addItemToTemplate_createsTemplateItem() {
    UUID templateId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateEntity template = TemplateEntity.builder().id(templateId).build();
    ItemEntity item = ItemEntity.builder().name("Socks").build();

    when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));
    when(itemService.getOrCreateItem("Socks", "Clothing")).thenReturn(item);
    when(templateItemRepository.save(any(TemplateItemEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TemplateItemEntity result =
        templateService.addItemToTemplate(
            templateId, "Socks", "PER_DAY", BigDecimal.valueOf(1), "Clothing");

    assertThat(result.getTemplate()).isEqualTo(template);
    assertThat(result.getItem()).isEqualTo(item);
    assertThat(result.getQuantityRuleType()).isEqualTo("PER_DAY");
    assertThat(result.getQuantityRuleValue()).isEqualTo(BigDecimal.valueOf(1));
    assertThat(result.getCategoryOverride()).isEqualTo("Clothing");
    verify(templateItemRepository).save(any(TemplateItemEntity.class));
  }

  @Test
  void updateTemplateItem_updatesFields() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateItemEntity templateItem = TemplateItemEntity.builder().id(id).build();
    when(templateItemRepository.findById(id)).thenReturn(Optional.of(templateItem));
    when(templateItemRepository.save(any(TemplateItemEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TemplateItemEntity result =
        templateService.updateTemplateItem(id, "FIXED", BigDecimal.valueOf(5), "Toiletries");

    assertThat(result.getQuantityRuleType()).isEqualTo("FIXED");
    assertThat(result.getQuantityRuleValue()).isEqualTo(BigDecimal.valueOf(5));
    assertThat(result.getCategoryOverride()).isEqualTo("Toiletries");
    verify(templateItemRepository).save(templateItem);
  }

  @Test
  void removeTemplateItem_deletesFromRepository() {
    UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    TemplateItemEntity templateItem = TemplateItemEntity.builder().id(id).build();
    when(templateItemRepository.findById(id)).thenReturn(Optional.of(templateItem));

    templateService.removeTemplateItem(id);

    verify(templateItemRepository).delete(templateItem);
  }
}
