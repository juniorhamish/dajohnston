package uk.co.dajohnston.portal.notification;

import java.security.GeneralSecurityException;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.Subscription;
import org.springframework.stereotype.Service;

@Service
public class NotificationCreator {
  public Notification create(String endpoint, String p256Dh, String auth, String payload)
      throws GeneralSecurityException {
    var subscription = new Subscription(endpoint, new Subscription.Keys(p256Dh, auth));
    return new Notification(subscription, payload);
  }
}
