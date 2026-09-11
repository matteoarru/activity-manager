package eu.cepol.eventoperations.application;

import java.time.Clock;

/** Business guard independent from HTTP claims and transport concerns. */
public final class ApprovalAuthorisation {
  private final Clock clock;

  public ApprovalAuthorisation(Clock clock) {
    this.clock = clock;
  }

  public void approve(Actor actor, String activityId) {
    requireAssignment(actor, activityId);
    requireApprover(actor);
    requireCurrentDelegation(actor);
  }

  public void issueOrder(Actor actor, String activityId) {
    approve(actor, activityId);
  }

  private void requireAssignment(Actor actor, String activityId) {
    if (!actor.isAssignedTo(activityId)) {
      throw new SecurityException("Actor is not assigned to activity");
    }
  }

  private void requireApprover(Actor actor) {
    boolean canApprove = actor.roles().contains(Actor.Role.AM)
        || actor.roles().contains(Actor.Role.PO);
    if (!canApprove) {
      throw new SecurityException("Only AM or PO may approve");
    }
  }

  private void requireCurrentDelegation(Actor actor) {
    if (!actor.hasValidDelegation(clock.instant())) {
      throw new SecurityException("Financial delegation is missing or expired");
    }
  }
}
