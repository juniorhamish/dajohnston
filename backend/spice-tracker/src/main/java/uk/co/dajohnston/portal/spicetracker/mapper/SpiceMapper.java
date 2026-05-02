package uk.co.dajohnston.portal.spicetracker.mapper;

import org.mapstruct.Mapper;
import uk.co.dajohnston.portal.spicetracker.model.SpiceDto;
import uk.co.dajohnston.portal.spicetracker.repository.SpiceEntity;

@Mapper(componentModel = "spring")
public interface SpiceMapper {
  SpiceDto toDto(SpiceEntity entity);
}
