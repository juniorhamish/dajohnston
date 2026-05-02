package uk.co.dajohnston.portal.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import uk.co.dajohnston.security.exception.DuplicateResourceException;
import uk.co.dajohnston.security.exception.ResourceNotFoundException;

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  ProblemDetail handleDuplicateResource(DuplicateResourceException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
  }
}
