package uk.co.dajohnston.portal.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

class SecurityConfigCoverageTest {

  @Test
  void jwtDecoder_createsDecoderWithValidators() {
    SecurityConfig securityConfig = new SecurityConfig();
    ReflectionTestUtils.setField(securityConfig, "issuer", "http://issuer");
    ReflectionTestUtils.setField(securityConfig, "audience", "my-audience");

    NimbusJwtDecoder mockDecoder = mock(NimbusJwtDecoder.class);

    try (MockedStatic<JwtDecoders> mockedDecoders = mockStatic(JwtDecoders.class)) {
      mockedDecoders
          .when(() -> JwtDecoders.fromOidcIssuerLocation("http://issuer"))
          .thenReturn(mockDecoder);

      JwtDecoder result = securityConfig.jwtDecoder();

      assertNotNull(result);
      verify(mockDecoder).setJwtValidator(any());
    }
  }
}
