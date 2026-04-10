package uk.co.dajohnston.portal.user.auth0;

import com.auth0.client.mgmt.ManagementApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Auth0ManagementConfig {

  @Value("${auth0.management.domain}")
  private String domain;

  @Value("${auth0.management.clientId}")
  private String clientId;

  @Value("${auth0.management.clientSecret}")
  private String clientSecret;

  @Bean
  public ManagementApi managementAPI() {
    return ManagementApi.builder().domain(domain).clientCredentials(clientId, clientSecret).build();
  }
}
