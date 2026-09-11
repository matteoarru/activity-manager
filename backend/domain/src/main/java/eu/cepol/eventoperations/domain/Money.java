package eu.cepol.eventoperations.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/** Monetary value constructed from a wire-safe decimal string. */
public record Money(BigDecimal amount, Currency currency) {
  public Money {
    Objects.requireNonNull(currency);
    amount = Objects.requireNonNull(amount)
        .setScale(currency.getDefaultFractionDigits(), RoundingMode.HALF_UP);
  }

  public static Money of(String amount, String currency) {
    return new Money(new BigDecimal(amount), Currency.getInstance(currency));
  }

  public Money plus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.add(other.amount), currency);
  }

  public Money minus(Money other) {
    requireSameCurrency(other);
    return new Money(amount.subtract(other.amount), currency);
  }

  public boolean isNegative() {
    return amount.signum() < 0;
  }

  private void requireSameCurrency(Money other) {
    Objects.requireNonNull(other);
    if (!currency.equals(other.currency)) {
      throw new IllegalArgumentException("Currency mismatch");
    }
  }
}
