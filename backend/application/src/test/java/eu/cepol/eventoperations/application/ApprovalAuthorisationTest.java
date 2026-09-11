package eu.cepol.eventoperations.application;

import static org.junit.jupiter.api.Assertions.*;
import java.time.*;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ApprovalAuthorisationTest {
  private final Clock clock=Clock.fixed(Instant.parse("2026-09-11T12:00:00Z"), ZoneOffset.UTC);
  private final ApprovalAuthorisation guard=new ApprovalAuthorisation(clock);
  @Test void assignedIaCannotApproveOrIssue() { var ia=new Actor("ia", Set.of(Actor.Role.IA), Set.of("a"), Instant.parse("2026-10-01T00:00:00Z")); assertThrows(SecurityException.class,()->guard.approve(ia,"a")); assertThrows(SecurityException.class,()->guard.issueOrder(ia,"a")); }
  @Test void expiredAssignedAmCannotApprove() { var am=new Actor("am", Set.of(Actor.Role.AM), Set.of("a"), Instant.parse("2026-09-01T00:00:00Z")); assertThrows(SecurityException.class,()->guard.approve(am,"a")); }
  @Test void delegatedAssignedAmCanApprove() { var am=new Actor("am", Set.of(Actor.Role.AM), Set.of("a"), Instant.parse("2026-10-01T00:00:00Z")); assertDoesNotThrow(()->guard.approve(am,"a")); }

  @Test
  void unassignedApproverCannotApprove() {
    var am = new Actor("am", Set.of(Actor.Role.AM), Set.of(), Instant.parse("2026-10-01T00:00:00Z"));

    assertThrows(SecurityException.class, () -> guard.approve(am, "a"));
  }

  @Test
  void delegatedApproverCanIssueOrder() {
    var po = new Actor(
        "po", Set.of(Actor.Role.PO), Set.of("a"), Instant.parse("2026-10-01T00:00:00Z"));

    assertDoesNotThrow(() -> guard.issueOrder(po, "a"));
  }
}
