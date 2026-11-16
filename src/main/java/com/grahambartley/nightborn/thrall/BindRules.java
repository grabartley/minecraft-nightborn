package com.grahambartley.nightborn.thrall;

import java.util.Locale;
import java.util.Optional;

public final class BindRules {

  public enum Outcome {
    BOUND,
    NO_BOND,
    AT_LIMIT,
    REFUSED;

    public Optional<String> translationKey() {
      return this == NO_BOND
          ? Optional.empty()
          : Optional.of("nightborn.thrall.bind." + name().toLowerCase(Locale.ROOT));
    }
  }

  private BindRules() {}

  public static Outcome decide(
      final boolean bindable,
      final boolean mesmerisedByFeeder,
      final boolean tookBlood,
      final int owned,
      final int limit,
      final float roll,
      final int chancePercent) {
    if (!bindable || !mesmerisedByFeeder || !tookBlood || limit <= 0) {
      return Outcome.NO_BOND;
    }
    if (owned >= limit) {
      return Outcome.AT_LIMIT;
    }
    return roll < chancePercent / 100.0F ? Outcome.BOUND : Outcome.REFUSED;
  }
}
