package uk.co.dajohnston.portal.notification.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import java.security.GeneralSecurityException;
import java.security.Security;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import uk.co.dajohnston.portal.config.ConfigurationException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PushServiceConfig {

  @Valid private final VapidProperties vapidProperties;

  @Bean("bouncyCastleInitialization")
  public Object bouncyCastleInitialization() {
    return new Object() {
      @PostConstruct
      public void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
          Security.addProvider(new BouncyCastleProvider());
        }
      }
    };
  }

  @Bean
  @DependsOn("bouncyCastleInitialization")
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
}
