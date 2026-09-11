package eu.cepol.eventoperations.api;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Deliberately contains no operational data or authentication bypass. */
@RestController
class HealthController {
  @GetMapping("/api/health") Map<String, String> health() { return Map.of("status", "UP", "profile", "synthetic"); }
}
