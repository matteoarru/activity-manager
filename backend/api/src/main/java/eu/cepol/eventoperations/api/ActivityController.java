package eu.cepol.eventoperations.api;

import eu.cepol.eventoperations.infrastructure.activity.ActivityRecord;
import eu.cepol.eventoperations.infrastructure.activity.ActivityRepository;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activities")
class ActivityController {
  private final ActivityRepository activities;
  ActivityController(ActivityRepository activities) {
    this.activities = activities;
  }

  @GetMapping
  List<ActivityRecord> list(Authentication actor) {
    return hasGlobalVisibility(actor) ? activities.findAll() : activities.findVisibleTo(actor.getName());
  }

  private boolean hasGlobalVisibility(Authentication actor) {
    return actor.getAuthorities().stream()
        .anyMatch(authority -> authority.getAuthority().equals("ROLE_FINANCE")
            || authority.getAuthority().equals("ROLE_TECHNICAL_ADMIN")
            || authority.getAuthority().equals("ROLE_AO"));
  }
}
