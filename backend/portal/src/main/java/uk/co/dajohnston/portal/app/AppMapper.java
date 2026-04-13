package uk.co.dajohnston.portal.app;

import org.mapstruct.Mapper;
import uk.co.dajohnston.model.AppDto;
import uk.co.dajohnston.portal.app.entity.AppEntity;

@Mapper
public interface AppMapper {
  App toDomain(AppEntity entity);

  AppDto toDto(App app);
}
