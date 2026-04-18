package uk.co.dajohnston.portal.notification;

import static nl.martijndwars.webpush.Encoding.AES128GCM;

import jakarta.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.jose4j.lang.JoseException;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.portal.notification.config.VapidProperties;
import uk.co.dajohnston.portal.notification.entity.PushSubscriptionEntity;
import uk.co.dajohnston.portal.notification.entity.PushSubscriptionRepository;
import uk.co.dajohnston.portal.user.UserService;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

  private final PushSubscriptionRepository pushSubscriptionRepository;
  private final UserService userService;
  @Valid private final VapidProperties vapidProperties;
  private final PushService pushService;
  private final NotificationCreator notificationCreator;

  public void registerSubscription(JwtClaimAccessor jwt, SubscriptionDetails request) {
    var user = userService.findOrCreateUser(jwt);
    var subscription =
        pushSubscriptionRepository
            .findByEndpoint(request.endpoint())
            .orElseGet(
                () ->
                    PushSubscriptionEntity.builder()
                        .user(user)
                        .endpoint(request.endpoint())
                        .build());

    subscription.setP256dh(request.p256Dh());
    subscription.setAuth(request.auth());
    subscription.setExpirationTime(request.expirationTime());

    pushSubscriptionRepository.save(subscription);
  }

  public String getVapidPublicKey() {
    return vapidProperties.getPublicKey();
  }

  public void sendNotification(PushSubscriptionEntity subscriptionEntity, String payload) {
    try {
      var notification =
          notificationCreator.create(
              subscriptionEntity.getEndpoint(),
              subscriptionEntity.getP256dh(),
              subscriptionEntity.getAuth(),
              payload);
      pushService.send(notification, AES128GCM);
    } catch (GeneralSecurityException e) {
      log.error("Security error sending push notification", e);
    } catch (IOException | JoseException | ExecutionException e) {
      log.error("Error sending push notification", e);
      // If the subscription is no longer valid, we should probably remove it
      if (e.getMessage() != null && e.getMessage().contains("410 Gone")) {
        log.info("Subscription gone, removing: {}", subscriptionEntity.getEndpoint());
        pushSubscriptionRepository.delete(subscriptionEntity);
      }
    } catch (InterruptedException e) {
      log.error("Thread interrupted sending push notification", e);
      Thread.currentThread().interrupt();
    }
  }
}
