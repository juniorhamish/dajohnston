package uk.co.dajohnston.portal.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

  @GetMapping("/api/test")
  public Map<String, String> test() {
    return Map.of("status", "UP", "message", "Test endpoint is working correctly!");
  }
}
