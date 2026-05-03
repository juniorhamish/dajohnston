package uk.co.dajohnston.portal.listz.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.listz.repository.ItemEntity;
import uk.co.dajohnston.portal.listz.repository.ItemRepository;
import uk.co.dajohnston.security.context.TenantContext;

@Service
@RequiredArgsConstructor
public class ListzItemService {
  private final ItemRepository itemRepository;

  @Transactional(readOnly = true)
  public List<ItemEntity> searchItems(String query) {
    return itemRepository.findByNameContainingIgnoreCase(query);
  }

  @Transactional
  public ItemEntity getOrCreateItem(String name, String defaultCategory) {
    return itemRepository
        .findByNameIgnoreCase(name)
        .orElseGet(
            () ->
                itemRepository.save(
                    ItemEntity.builder()
                        .householdId(TenantContext.getTenantId())
                        .name(name)
                        .defaultCategory(defaultCategory)
                        .build()));
  }
}
