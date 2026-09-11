package eu.cepol.eventoperations.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CommitmentLedgerTest {
  @Test void rejectsNegativeEnvelopeAndNonPositiveValues() {
    assertThrows(IllegalArgumentException.class, () -> new CommitmentLedger(Money.of("-1", "EUR")));
    var ledger = new CommitmentLedger(Money.of("100", "EUR"));
    assertThrows(IllegalArgumentException.class, () -> ledger.hold("a", "hash", Money.of("0", "EUR")));
    assertThrows(IllegalArgumentException.class, () -> ledger.authoriseHeld(Money.of("-1", "EUR")));
  }

  @Test void rejectsUncoveredAuthorisationAndInvoice() {
    var ledger = new CommitmentLedger(Money.of("100", "EUR"));
    assertThrows(IllegalStateException.class, () -> ledger.authoriseHeld(Money.of("10", "EUR")));
    assertThrows(IllegalStateException.class, () -> ledger.recogniseInvoice(Money.of("10", "EUR")));
  }

  @Test void rejectsCurrencyMismatch() {
    var ledger = new CommitmentLedger(Money.of("100", "EUR"));
    assertThrows(IllegalArgumentException.class, () -> ledger.hold("a", "hash", Money.of("10", "USD")));
    assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").plus(Money.of("1", "USD")));
    assertThrows(IllegalArgumentException.class, () -> Money.of("1", "EUR").minus(Money.of("1", "USD")));
  }

  @Test void holdAndInvoiceRecognitionDoNotDoubleDebit() {
    var ledger = new CommitmentLedger(Money.of("1000", "EUR")); var six = Money.of("600", "EUR");
    ledger.hold("request-1", "hash-a", six); ledger.authoriseHeld(six); assertEquals(Money.of("400", "EUR"), ledger.available());
    ledger.recogniseInvoice(six); assertEquals(Money.of("400", "EUR"), ledger.available()); assertEquals(Money.of("600", "EUR"), ledger.consumption());
  }
  @Test void competingLastHundredAllowsOnlyOneEightyHold() {
    var ledger = new CommitmentLedger(Money.of("100", "EUR")); ledger.hold("a", "a", Money.of("80", "EUR"));
    assertThrows(IllegalStateException.class, () -> ledger.hold("b", "b", Money.of("80", "EUR")));
  }
  @Test void replayAndPayloadCollisionHaveNoSecondFinancialEffect() {
    var ledger = new CommitmentLedger(Money.of("100", "EUR")); ledger.hold("a", "body-a", Money.of("10", "EUR"));
    assertThrows(IllegalStateException.class, () -> ledger.hold("a", "body-a", Money.of("10", "EUR")));
    assertThrows(IllegalArgumentException.class, () -> ledger.hold("a", "body-b", Money.of("10", "EUR"))); assertEquals(Money.of("90", "EUR"), ledger.available());
  }
}
