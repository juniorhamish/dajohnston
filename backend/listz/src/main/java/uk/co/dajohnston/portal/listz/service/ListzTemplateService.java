package uk.co.dajohnston.portal.listz.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.listz.repository.ItemEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateItemEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateItemRepository;
import uk.co.dajohnston.portal.listz.repository.TemplateRepository;
import uk.co.dajohnston.security.context.TenantContext;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ListzTemplateService {
  private final TemplateRepository templateRepository;
  private final TemplateItemRepository templateItemRepository;
  private final ListzItemService itemService;

  @Transactional(readOnly = true)
  public List<TemplateEntity> listTemplates() {
    return templateRepository.findAllByDeletedAtIsNull();
  }

  @Transactional
  public TemplateEntity createTemplate(String name) {
    TemplateEntity template =
        TemplateEntity.builder().householdId(TenantContext.getTenantId()).name(name).build();
    return templateRepository.save(template);
  }

  @Transactional(readOnly = true)
  public TemplateEntity getTemplate(UUID id) {
    return templateRepository
        .findById(id)
        .filter(t -> t.getDeletedAt() == null)
        .orElseThrow(() -> new ResourceNotFoundException("Template not found"));
  }

  @Transactional
  public TemplateEntity updateTemplate(UUID id, String name) {
    TemplateEntity template = getTemplate(id);
    template.setName(name);
    return templateRepository.save(template);
  }

  @Transactional
  public void deleteTemplate(UUID id) {
    TemplateEntity template = getTemplate(id);
    template.setDeletedAt(OffsetDateTime.now());
    templateRepository.save(template);
  }

  @Transactional
  public TemplateItemEntity addItemToTemplate(
      UUID templateId,
      String itemName,
      String quantityRuleType,
      BigDecimal quantityRuleValue,
      String category) {
    TemplateEntity template = getTemplate(templateId);
    ItemEntity item = itemService.getOrCreateItem(itemName, category);

    TemplateItemEntity templateItem =
        TemplateItemEntity.builder()
            .householdId(TenantContext.getTenantId())
            .template(template)
            .item(item)
            .quantityRuleType(quantityRuleType)
            .quantityRuleValue(quantityRuleValue)
            .categoryOverride(category)
            .build();
    return templateItemRepository.save(templateItem);
  }

  @Transactional
  public TemplateItemEntity updateTemplateItem(
      UUID id, String quantityRuleType, BigDecimal quantityRuleValue, String category) {
    TemplateItemEntity templateItem =
        templateItemRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template item not found"));
    if (quantityRuleType != null) {
      templateItem.setQuantityRuleType(quantityRuleType);
    }
    if (quantityRuleValue != null) {
      templateItem.setQuantityRuleValue(quantityRuleValue);
    }
    if (category != null) {
      templateItem.setCategoryOverride(category);
    }
    return templateItemRepository.save(templateItem);
  }

  @Transactional
  public void removeTemplateItem(UUID id) {
    TemplateItemEntity templateItem =
        templateItemRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Template item not found"));
    templateItemRepository.delete(templateItem);
  }
}
