package uk.co.dajohnston.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortalBackendApplication {
  private PortalBackendApplication() {
    /* This utility class should not be instantiated  */
  }

  static void main(String[] args) {
    SpringApplication.run(PortalBackendApplication.class, args);
  }
}
