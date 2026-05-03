package uk.co.dajohnston.portal.listz.controller;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.portal.listz.api.TemplatesApi;
import uk.co.dajohnston.portal.listz.mapper.ListzMapper;
import uk.co.dajohnston.portal.listz.model.AddTemplateItemRequestDto;
import uk.co.dajohnston.portal.listz.model.CreateTemplateRequestDto;
import uk.co.dajohnston.portal.listz.model.TemplateDto;
import uk.co.dajohnston.portal.listz.model.TemplateItemDto;
import uk.co.dajohnston.portal.listz.model.TemplatesDto;
import uk.co.dajohnston.portal.listz.model.UpdateTemplateItemRequestDto;
import uk.co.dajohnston.portal.listz.model.UpdateTemplateRequestDto;
import uk.co.dajohnston.portal.listz.service.ListzTemplateService;

@RestController
@RequiredArgsConstructor
class ListzTemplateController implements TemplatesApi {
  private final ListzTemplateService templateService;
  private final ListzMapper mapper;

  @Override
  public ResponseEntity<TemplatesDto> listTemplates(UUID xHouseholdId) {
    return ResponseEntity.ok(
        new TemplatesDto(mapper.toTemplateDtoList(templateService.listTemplates())));
  }

  @Override
  public ResponseEntity<TemplateDto> createTemplate(
      UUID xHouseholdId, CreateTemplateRequestDto body) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(mapper.toDto(templateService.createTemplate(body.name())));
  }

  @Override
  public ResponseEntity<TemplateDto> getTemplate(UUID id, UUID xHouseholdId) {
    return ResponseEntity.ok(mapper.toDto(templateService.getTemplate(id)));
  }

  @Override
  public ResponseEntity<TemplateDto> updateTemplate(
      UUID id, UUID xHouseholdId, UpdateTemplateRequestDto body) {
    return ResponseEntity.ok(mapper.toDto(templateService.updateTemplate(id, body.name())));
  }

  @Override
  public ResponseEntity<Void> deleteTemplate(UUID id, UUID xHouseholdId) {
    templateService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<TemplateItemDto> addTemplateItem(
      UUID id, UUID xHouseholdId, AddTemplateItemRequestDto body) {
    String ruleType;
    if (body.quantityRuleType() != null) {
      ruleType = body.quantityRuleType().name();
    } else {
      ruleType = "FIXED";
    }
    BigDecimal ruleValue;
    if (body.quantityRuleValue() != null) {
      ruleValue = BigDecimal.valueOf(body.quantityRuleValue());
    } else {
      ruleValue = BigDecimal.ONE;
    }
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            mapper.toDto(
                templateService.addItemToTemplate(
                    id, body.itemName(), ruleType, ruleValue, body.categoryOverride())));
  }

  @Override
  public ResponseEntity<TemplateItemDto> updateTemplateItem(
      UUID id, UUID xHouseholdId, UpdateTemplateItemRequestDto body) {
    String ruleType;
    if (body.quantityRuleType() != null) {
      ruleType = body.quantityRuleType().name();
    } else {
      ruleType = null;
    }
    BigDecimal ruleValue;
    if (body.quantityRuleValue() != null) {
      ruleValue = BigDecimal.valueOf(body.quantityRuleValue());
    } else {
      ruleValue = null;
    }

    return ResponseEntity.ok(
        mapper.toDto(
            templateService.updateTemplateItem(id, ruleType, ruleValue, body.categoryOverride())));
  }

  @Override
  public ResponseEntity<Void> deleteTemplateItem(UUID id, UUID xHouseholdId) {
    templateService.removeTemplateItem(id);
    return ResponseEntity.noContent().build();
  }
}
