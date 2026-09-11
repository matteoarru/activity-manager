package eu.cepol.eventoperations.application;

import java.time.Instant;
import java.util.Set;

public record Actor(String id, Set<Role> roles, Set<String> activityIds, Instant delegationEndsAt) {
  public enum Role {
    AM, PO, IA, AO, FINANCE, PROVIDER, CNU, ATTENDEE, TECHNICAL_ADMIN
  }

  public boolean isAssignedTo(String activityId) {
    return activityIds.contains(activityId);
  }

  public boolean hasValidDelegation(Instant now) {
    return delegationEndsAt != null && delegationEndsAt.isAfter(now);
  }
}
