package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KindleRulesTest {

  @ParameterizedTest(name = "health {0}, cost {1} -> {2}")
  @CsvSource({"20, 4, true", "4.5, 4, true", "4, 4, false", "2, 4, false"})
  @DisplayName("a hunter must have more health than the cut costs")
  void affords(final float health, final float cost, final boolean affords) {
    assertEquals(affords, KindleRules.affords(health, cost));
  }

  @ParameterizedTest(name = "fire immune {0}, weapon blow {1} -> {2}")
  @CsvSource({
    "false, true, true",
    "true, true, false",
    "false, false, false",
    "true, false, false"
  })
  @DisplayName("a burning blade sets alight anything it strikes that can burn")
  void sets(final boolean fireImmune, final boolean blow, final boolean sets) {
    assertEquals(sets, KindleRules.sets(fireImmune, blow));
  }

  @ParameterizedTest(name = "vampire {0} -> seared {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("only a vampire is seared so it cannot heal while it burns")
  void sears(final boolean vampire, final boolean seared) {
    assertEquals(seared, KindleRules.sears(vampire));
  }

  @ParameterizedTest(name = "{0} healing, seared {1}, on fire {2} -> {3}")
  @CsvSource({"4, false, false, 4", "4, true, true, 0", "4, true, false, 4", "4, false, true, 4"})
  @DisplayName("a vampire set alight by blood fire does not heal while it burns")
  void heal(final float amount, final boolean seared, final boolean onFire, final float expected) {
    assertEquals(expected, KindleRules.heal(amount, seared, onFire));
  }
}
