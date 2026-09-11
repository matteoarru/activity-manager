package eu.cepol.eventoperations.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** In-memory commitment rules; persistence must execute them under a row lock. */
public final class CommitmentLedger {
  private final Money envelope;
  private final Map<String, String> requests = new HashMap<>();
  private Money holds;
  private Money exposure;
  private Money consumption;

  public CommitmentLedger(Money envelope) {
    if (envelope.isNegative()) {
      throw new IllegalArgumentException("Envelope cannot be negative");
    }
    this.envelope = envelope;
    this.holds = zero();
    this.exposure = zero();
    this.consumption = zero();
  }

  public synchronized void hold(String key, String requestHash, Money value) {
    replay(key, requestHash);
    requirePositive(value);
    if (available().amount().compareTo(value.amount()) < 0) {
      throw new IllegalStateException("Insufficient protected envelope");
    }
    holds = holds.plus(value);
    requests.put(key, requestHash);
  }

  public synchronized void authoriseHeld(Money value) {
    requirePositive(value);
    if (holds.amount().compareTo(value.amount()) < 0) {
      throw new IllegalStateException("No matching hold");
    }
    holds = holds.minus(value);
    exposure = exposure.plus(value);
  }

  /** A covered invoice moves an existing liability without changing availability. */
  public synchronized void recogniseInvoice(Money value) {
    requirePositive(value);
    if (exposure.amount().compareTo(value.amount()) < 0) {
      throw new IllegalStateException("Invoice exceeds authorised exposure");
    }
    exposure = exposure.minus(value);
    consumption = consumption.plus(value);
  }

  public synchronized Money available() {
    return envelope.minus(holds).minus(exposure).minus(consumption);
  }

  public synchronized Money exposure() {
    return exposure;
  }

  public synchronized Money consumption() {
    return consumption;
  }

  private Money zero() {
    return Money.of("0", envelope.currency().getCurrencyCode());
  }

  private void requirePositive(Money value) {
    Objects.requireNonNull(value);
    envelope.plus(value);
    if (value.amount().signum() <= 0) {
      throw new IllegalArgumentException("Value must be positive");
    }
  }

  private void replay(String key, String requestHash) {
    var prior = requests.get(key);
    if (prior != null && prior.equals(requestHash)) {
      throw new IllegalStateException("Request already applied");
    }
    if (prior != null) {
      throw new IllegalArgumentException("Idempotency key payload mismatch");
    }
  }
}
