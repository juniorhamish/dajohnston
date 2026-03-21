package uk.co.dajohnston.portal.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedController {

  @GetMapping("/api/protected")
  public Map<String, String> protectedEndpoint() {
    return Map.of("message", "This is a protected endpoint");
  }
}
