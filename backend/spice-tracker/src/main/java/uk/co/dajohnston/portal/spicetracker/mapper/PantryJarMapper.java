package uk.co.dajohnston.portal.spicetracker.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.co.dajohnston.portal.spicetracker.model.PantryJarDto;
import uk.co.dajohnston.portal.spicetracker.repository.PantryJarEntity;

@Mapper(componentModel = "spring")
public interface PantryJarMapper {
  @Mapping(target = "spiceId", source = "spice.id")
  @Mapping(target = "spiceName", source = "spice.name")
  PantryJarDto toDto(PantryJarEntity entity);
}
