package uk.co.dajohnston.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "uk.co.dajohnston")
public class PortalBackendApplication {
  static void main(String[] args) {
    SpringApplication.run(PortalBackendApplication.class, args);
  }
}
