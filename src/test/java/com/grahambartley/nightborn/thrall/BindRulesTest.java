package com.grahambartley.nightborn.thrall;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BindRulesTest {

  @ParameterizedTest(
      name = "bindable {0}, mesmerised {1}, took {2}, owned {3}/{4}, roll {5}, chance {6} -> {7}")
  @CsvSource({
    "true, true, true, 0, 1, 0.1, 35, BOUND",
    "true, true, true, 0, 1, 0.34, 35, BOUND",
    "true, true, true, 0, 1, 0.35, 35, REFUSED",
    "true, true, true, 2, 3, 0.0, 100, BOUND",
    "true, true, true, 1, 1, 0.0, 100, AT_LIMIT",
    "true, true, true, 3, 3, 0.0, 100, AT_LIMIT",
    "true, true, true, 0, 0, 0.0, 100, NO_BOND",
    "false, true, true, 0, 1, 0.0, 100, NO_BOND",
    "true, false, true, 0, 1, 0.0, 100, NO_BOND",
    "true, true, false, 0, 1, 0.0, 100, NO_BOND",
    "true, true, true, 0, 1, 0.0, 0, REFUSED"
  })
  @DisplayName("a sip binds a villager you mesmerised by chance, up to your thrall limit")
  void decide(
      final boolean bindable,
      final boolean mesmerised,
      final boolean took,
      final int owned,
      final int limit,
      final float roll,
      final int chance,
      final BindRules.Outcome expected) {
    assertEquals(
        expected, BindRules.decide(bindable, mesmerised, took, owned, limit, roll, chance));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "BOUND, nightborn.thrall.bind.bound",
    "AT_LIMIT, nightborn.thrall.bind.at_limit",
    "REFUSED, nightborn.thrall.bind.refused",
    "NO_BOND, ''"
  })
  @DisplayName("the owner is told the outcome, except when no bond was tried")
  void keys(final BindRules.Outcome outcome, final String key) {
    assertEquals(key.isEmpty() ? Optional.empty() : Optional.of(key), outcome.translationKey());
  }
}
