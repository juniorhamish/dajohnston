package uk.co.dajohnston.portal.listz.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import uk.co.dajohnston.portal.listz.repository.ItemEntity;
import uk.co.dajohnston.portal.listz.repository.ItemRepository;
import uk.co.dajohnston.security.context.TenantContext;

@ExtendWith(MockitoExtension.class)
class ListzItemServiceTest {

  private final UUID householdId = UUID.fromString("6f369f9e-3d1b-4b1a-9f9e-3d1b4b1a9f9e");
  @Mock private ItemRepository itemRepository;
  @InjectMocks private ListzItemService itemService;
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
  void searchItems_returnsMatchingItems() {
    ItemEntity item = ItemEntity.builder().name("Socks").build();
    when(itemRepository.findByNameContainingIgnoreCase("soc")).thenReturn(List.of(item));

    List<ItemEntity> result = itemService.searchItems("soc");

    assertThat(result).containsExactly(item);
  }

  @Test
  void getOrCreateItem_returnsExistingItem_whenFound() {
    ItemEntity existingItem = ItemEntity.builder().name("Socks").build();
    when(itemRepository.findByNameIgnoreCase("Socks")).thenReturn(Optional.of(existingItem));

    ItemEntity result = itemService.getOrCreateItem("Socks", "Clothing");

    assertThat(result).isEqualTo(existingItem);
  }

  @Test
  void getOrCreateItem_createsNewItem_whenNotFound() {
    when(itemRepository.findByNameIgnoreCase("Pants")).thenReturn(Optional.empty());
    when(itemRepository.save(any(ItemEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ItemEntity result = itemService.getOrCreateItem("Pants", "Clothing");

    assertThat(result.getName()).isEqualTo("Pants");
    assertThat(result.getHouseholdId()).isEqualTo(householdId);
    assertThat(result.getDefaultCategory()).isEqualTo("Clothing");
    verify(itemRepository).save(any(ItemEntity.class));
  }
}
