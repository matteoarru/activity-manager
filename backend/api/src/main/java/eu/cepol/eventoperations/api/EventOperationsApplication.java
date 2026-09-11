package eu.cepol.eventoperations.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "eu.cepol.eventoperations")
public class EventOperationsApplication {
  public static void main(String[] args) { SpringApplication.run(EventOperationsApplication.class, args); }
}
