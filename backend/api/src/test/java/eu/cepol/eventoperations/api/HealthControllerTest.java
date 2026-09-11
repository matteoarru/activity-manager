package eu.cepol.eventoperations.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class HealthControllerTest {
  @Test void exposesSyntheticHealthWithoutBusinessData() { assertEquals("UP", new HealthController().health().get("status")); }
}
