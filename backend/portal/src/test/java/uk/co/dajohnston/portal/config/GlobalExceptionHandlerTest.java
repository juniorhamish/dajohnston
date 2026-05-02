package uk.co.dajohnston.portal.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import uk.co.dajohnston.security.exception.DuplicateResourceException;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void handleResourceNotFound_returns404() {
    ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
    ProblemDetail result = handler.handleResourceNotFound(ex);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    assertThat(result.getDetail()).isEqualTo("Not found");
  }

  @Test
  void handleDuplicateResource_returns409() {
    DuplicateResourceException ex = new DuplicateResourceException("Already exists");
    ProblemDetail result = handler.handleDuplicateResource(ex);

    assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
    assertThat(result.getDetail()).isEqualTo("Already exists");
  }
}
