package uk.co.dajohnston.portal.notification.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.co.dajohnston.portal.config.ConfigurationException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PushServiceConfig {

  @Valid private final VapidProperties vapidProperties;

  @Bean
  public PushService pushService() {
    try {
      return new PushService(
          vapidProperties.getPublicKey(),
          vapidProperties.getPrivateKey(),
          vapidProperties.getSubject());
    } catch (GeneralSecurityException e) {
      log.error("Security error initializing PushService with VAPID properties", e);
      throw new ConfigurationException(e);
    }
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
