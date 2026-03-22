package uk.co.dajohnston.portal.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidatorTest {

  @Test
  void validate_withMatchingAudience_returnsSuccess() {
    AudienceValidator validator = new AudienceValidator("my-audience");
    Jwt jwt =
        Jwt.withTokenValue("token").header("alg", "none").audience(List.of("my-audience")).build();

    OAuth2TokenValidatorResult result = validator.validate(jwt);
    assertFalse(result.hasErrors());
  }

  @Test
  void validate_withoutMatchingAudience_returnsFailure() {
    AudienceValidator validator = new AudienceValidator("my-audience");
    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .audience(List.of("other-audience"))
            .build();

    OAuth2TokenValidatorResult result = validator.validate(jwt);
    assertTrue(result.hasErrors());
  }
}
