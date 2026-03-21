package uk.co.dajohnston.portal.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

class AudienceValidator implements OAuth2TokenValidator<Jwt> {
  private final String audience;

  AudienceValidator(String audience) {
    this.audience = audience;
  }

  public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (jwt.getAudience().contains(audience)) {
      return OAuth2TokenValidatorResult.success();
    }
    var oAuth2Error = new OAuth2Error("invalid_token", "The required audience is missing", null);
    return OAuth2TokenValidatorResult.failure(oAuth2Error);
  }
}
