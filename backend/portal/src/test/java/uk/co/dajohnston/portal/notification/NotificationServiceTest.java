package uk.co.dajohnston.portal.notification;

import static nl.martijndwars.webpush.Encoding.AES128GCM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import uk.co.dajohnston.portal.notification.config.VapidProperties;
import uk.co.dajohnston.portal.notification.entity.PushSubscriptionEntity;
import uk.co.dajohnston.portal.notification.entity.PushSubscriptionRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private PushSubscriptionRepository pushSubscriptionRepository;
  @Mock private UserService userService;
  @Mock private VapidProperties vapidProperties;
  @Mock private JwtClaimAccessor jwt;
  @Mock private PushService pushService;
  @Mock private NotificationCreator notificationCreator;
  @Mock private Notification notification;
  @Mock private ObjectMapper objectMapper;

  @InjectMocks private NotificationService notificationService;

  @Test
  void registerSubscription_endpointExists_updatesSubscription() {
    UserEntity user =
        UserEntity.builder().id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(pushSubscriptionRepository.findByEndpoint("https://endpoint"))
        .thenReturn(
            Optional.of(
                PushSubscriptionEntity.builder()
                    .endpoint("https://endpoint")
                    .id(UUID.fromString("123e4567-e89b-12d3-a456-426614174123"))
                    .p256dh("old-p256dh")
                    .auth("old-auth")
                    .expirationTime(123L)
                    .user(user)
                    .build()));

    notificationService.registerSubscription(
        jwt, new SubscriptionDetails("https://endpoint", 10L, "p256dh", "auth"));

    verify(pushSubscriptionRepository)
        .save(
            argThat(
                x ->
                    x.getEndpoint().equals("https://endpoint")
                        && x.getExpirationTime() == 10L
                        && x.getP256dh().equals("p256dh")
                        && x.getAuth().equals("auth")
                        && x.getUser().equals(user)));
  }

  @Test
  void registerSubscription_endpointDoesNotExist_savesNewSubscription() {
    UserEntity user =
        UserEntity.builder().id(UUID.fromString("123e4567-e89b-12d3-a456-426614174000")).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(pushSubscriptionRepository.findByEndpoint("https://endpoint"))
        .thenReturn(Optional.empty());

    SubscriptionDetails request =
        new SubscriptionDetails("https://endpoint", 10L, "p256dh", "auth");

    notificationService.registerSubscription(jwt, request);

    verify(pushSubscriptionRepository)
        .save(
            argThat(
                x ->
                    x.getEndpoint().equals("https://endpoint")
                        && x.getExpirationTime() == 10L
                        && x.getP256dh().equals("p256dh")
                        && x.getAuth().equals("auth")
                        && x.getUser().equals(user)));
  }

  @Test
  void getVapidPublicKey_returnsPublicKey() {
    when(vapidProperties.getPublicKey()).thenReturn("public-key");

    String publicKey = notificationService.getVapidPublicKey();

    assertThat(publicKey).isEqualTo("public-key");
  }

  @Test
  void sendNotification_sendsNotification() throws Exception {
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);

    notificationService.sendNotification(
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build(),
        "payload");

    verify(pushService).send(notification, AES128GCM);
  }

  @Test
  void sendNotification_onGoneException_deletesSubscription() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);
    when(pushService.send(notification, AES128GCM)).thenThrow(new IOException("410 Gone"));

    PushSubscriptionEntity pushSubscriptionEntity =
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build();
    notificationService.sendNotification(pushSubscriptionEntity, "payload");

    verify(pushSubscriptionRepository).delete(pushSubscriptionEntity);
    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Error sending push notification");
  }

  @Test
  void sendNotification_exceptionHasNoMessage_doesNotDeleteSubscription() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);
    when(pushService.send(notification, AES128GCM)).thenThrow(new IOException((String) null));

    PushSubscriptionEntity pushSubscriptionEntity =
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build();
    notificationService.sendNotification(pushSubscriptionEntity, "payload");

    verify(pushSubscriptionRepository, never()).delete(pushSubscriptionEntity);
    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Error sending push notification");
  }

  @Test
  void sendNotification_exceptionHasWrongMessage_doesNotDeleteSubscription() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);
    when(pushService.send(notification, AES128GCM)).thenThrow(new IOException("Not a gone"));

    PushSubscriptionEntity pushSubscriptionEntity =
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build();
    notificationService.sendNotification(pushSubscriptionEntity, "payload");

    verify(pushSubscriptionRepository, never()).delete(pushSubscriptionEntity);
    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Error sending push notification");
  }

  @Test
  void sendNotification_generalSecurityException_logsMessage() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);
    when(pushService.send(notification, AES128GCM)).thenThrow(new GeneralSecurityException());

    PushSubscriptionEntity pushSubscriptionEntity =
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build();
    notificationService.sendNotification(pushSubscriptionEntity, "payload");

    verify(pushSubscriptionRepository, never()).delete(pushSubscriptionEntity);
    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Security error sending push notification");
  }

  @Test
  void sendNotification_interruptedException_logsMessage() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    when(notificationCreator.create("https://endpoint", "p256dh", "auth", "payload"))
        .thenReturn(notification);
    when(pushService.send(notification, AES128GCM)).thenThrow(new InterruptedException());

    PushSubscriptionEntity pushSubscriptionEntity =
        PushSubscriptionEntity.builder()
            .p256dh("p256dh")
            .auth("auth")
            .endpoint("https://endpoint")
            .build();
    notificationService.sendNotification(pushSubscriptionEntity, "payload");

    verify(pushSubscriptionRepository, never()).delete(pushSubscriptionEntity);
    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Thread interrupted sending push notification");
  }

  @Test
  void sendNotificationToUser_userNotFound_throwsException() {
    when(userService.findByEmail("user@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> notificationService.sendNotificationToUser("user@example.com", "title", "body"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("User not found: user@example.com");
  }

  @Test
  void sendNotificationToUser_sendsNotificationToAllSubscriptions() throws Exception {
    UserEntity user = UserEntity.builder().email("user@example.com").build();
    PushSubscriptionEntity sub1 =
        PushSubscriptionEntity.builder().endpoint("e1").p256dh("p1").auth("a1").build();
    PushSubscriptionEntity sub2 =
        PushSubscriptionEntity.builder().endpoint("e2").p256dh("p2").auth("a2").build();

    when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(pushSubscriptionRepository.findByUser(user)).thenReturn(List.of(sub1, sub2));
    when(objectMapper.writeValueAsString(any()))
        .thenReturn("{\"title\":\"title\",\"body\":\"body\"}");
    when(notificationCreator.create(any(), any(), any(), any())).thenReturn(notification);

    notificationService.sendNotificationToUser("user@example.com", "title", "body");

    verify(pushService, times(2)).send(notification, AES128GCM);
  }

  @Test
  void sendNotificationToUser_jsonException_logsError() throws Exception {
    ListAppender<ILoggingEvent> logger = mockLogger();
    UserEntity user = UserEntity.builder().email("user@example.com").build();

    when(userService.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(pushSubscriptionRepository.findByUser(user)).thenReturn(List.of());
    when(objectMapper.writeValueAsString(any()))
        .thenAnswer(
            _ -> {
              throw new JsonParseException("");
            });

    notificationService.sendNotificationToUser("user@example.com", "title", "body");

    assertThat(logger.list)
        .extracting(ILoggingEvent::getFormattedMessage)
        .contains("Error creating notification payload");
  }

  private ListAppender<ILoggingEvent> mockLogger() {
    var logger = (Logger) LoggerFactory.getLogger(NotificationService.class);
    var listAppender = new ListAppender<ILoggingEvent>();
    listAppender.start();
    logger.addAppender(listAppender);
    return listAppender;
  }
}
