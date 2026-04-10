package uk.co.dajohnston.portal.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class GravatarServiceTest {

  private final GravatarService gravatarService = new GravatarService();

  @Test
  void getGravatarUrl_nullEmail_returnsNull() {
    assertThat(gravatarService.getGravatarUrl(null)).isNull();
  }

  @Test
  void getGravatarUrl_emptyEmail_returnsNull() {
    assertThat(gravatarService.getGravatarUrl("")).isNull();
  }

  @Test
  void getGravatarUrl_blankEmail_returnsNull() {
    assertThat(gravatarService.getGravatarUrl("   ")).isNull();
  }

  @Test
  void getGravatarUrl_normalEmail_returnsCorrectUrl() {
    // MD5 for test@example.com is 55502f40dc8b7c769880b10874abc9d0
    assertThat(gravatarService.getGravatarUrl("test@example.com"))
        .isEqualTo("https://www.gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0");
  }

  @Test
  void getGravatarUrl_uppercaseAndWhitespace_returnsSameUrl() {
    assertThat(gravatarService.getGravatarUrl(" TEST@EXAMPLE.COM  "))
        .isEqualTo("https://www.gravatar.com/avatar/55502f40dc8b7c769880b10874abc9d0");
  }

  @Test
  void getGravatarUrl_md5NotSupported_returnsUrlWithEmptyHash() {
    try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
      mockedMessageDigest
          .when(() -> MessageDigest.getInstance(eq("MD5")))
          .thenThrow(new NoSuchAlgorithmException("MD5 not found"));

      assertThat(gravatarService.getGravatarUrl("test@example.com"))
          .isEqualTo("https://www.gravatar.com/avatar/");
    }
  }
}
