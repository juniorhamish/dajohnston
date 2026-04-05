package uk.co.dajohnston.portal.user;

import org.mapstruct.Mapper;
import uk.co.dajohnston.model.UserProfileDto;

@Mapper
public interface UserMapper {
  UserProfileDto toDto(UserProfile userProfile);
}
