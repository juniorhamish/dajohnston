package uk.co.dajohnston.portal.listz.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.co.dajohnston.portal.listz.model.ItemDto;
import uk.co.dajohnston.portal.listz.model.TemplateDto;
import uk.co.dajohnston.portal.listz.model.TemplateItemDto;
import uk.co.dajohnston.portal.listz.repository.ItemEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateEntity;
import uk.co.dajohnston.portal.listz.repository.TemplateItemEntity;

@Mapper
public interface ListzMapper {
  ItemDto toDto(ItemEntity entity);

  List<ItemDto> toItemDtoList(List<ItemEntity> entities);

  TemplateDto toDto(TemplateEntity entity);

  List<TemplateDto> toTemplateDtoList(List<TemplateEntity> entities);

  @Mapping(target = "itemId", source = "item.id")
  @Mapping(target = "name", source = "item.name")
  TemplateItemDto toDto(TemplateItemEntity entity);
}
