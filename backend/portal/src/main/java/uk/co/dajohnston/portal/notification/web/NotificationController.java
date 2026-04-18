package uk.co.dajohnston.portal.notification.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.api.NotificationsApi;
import uk.co.dajohnston.model.PushSubscriptionRequestDto;
import uk.co.dajohnston.model.VapidPublicKeyDto;
import uk.co.dajohnston.portal.notification.NotificationMapper;
import uk.co.dajohnston.portal.notification.NotificationService;

@RestController
@RequiredArgsConstructor
class NotificationController implements NotificationsApi {

  private final NotificationService notificationService;
  private final NotificationMapper notificationMapper;

  @Override
  public ResponseEntity<VapidPublicKeyDto> getVapidPublicKey(
      @AuthenticationPrincipal JwtClaimAccessor jwt) {
    return ResponseEntity.ok(
        VapidPublicKeyDto.builder().publicKey(notificationService.getVapidPublicKey()).build());
  }

  @Override
  public ResponseEntity<Void> registerSubscription(
      @AuthenticationPrincipal JwtClaimAccessor jwt,
      PushSubscriptionRequestDto pushSubscriptionRequestDto) {
    notificationService.registerSubscription(
        jwt, notificationMapper.fromDto(pushSubscriptionRequestDto));
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }
}
