package uk.co.dajohnston.portal.notification.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "vapid")
@Data
public class VapidProperties {
  @NotNull private String publicKey;
  @NotNull private String privateKey;
  @NotNull private String subject = "mailto:admin@dajohnston.co.uk";
}
