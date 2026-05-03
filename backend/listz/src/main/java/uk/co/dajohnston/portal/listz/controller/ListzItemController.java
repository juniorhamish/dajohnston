package uk.co.dajohnston.portal.listz.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.portal.listz.api.ItemsApi;
import uk.co.dajohnston.portal.listz.mapper.ListzMapper;
import uk.co.dajohnston.portal.listz.model.ItemsDto;
import uk.co.dajohnston.portal.listz.service.ListzItemService;

@RestController
@RequiredArgsConstructor
class ListzItemController implements ItemsApi {
  private final ListzItemService itemService;
  private final ListzMapper mapper;

  @Override
  public ResponseEntity<ItemsDto> searchItems(String q, UUID xHouseholdId) {
    return ResponseEntity.ok(new ItemsDto(mapper.toItemDtoList(itemService.searchItems(q))));
  }
}
