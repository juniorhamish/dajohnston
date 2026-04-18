package uk.co.dajohnston.portal.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.co.dajohnston.model.PushSubscriptionRequestDto;

@Mapper
public interface NotificationMapper {
  @Mapping(target = "p256Dh", source = "keys.p256Dh")
  @Mapping(target = "auth", source = "keys.auth")
  SubscriptionDetails fromDto(PushSubscriptionRequestDto dto);
}
