package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MesmeriseRulesTest {

  @ParameterizedTest(name = "immune {0}, resistance {1}, roll {2} -> {3}")
  @CsvSource({
    "false, 0.0, 0.0, MESMERISED",
    "false, 0.0, 0.99, MESMERISED",
    "false, 0.35, 0.34, RESISTED",
    "false, 0.35, 0.36, MESMERISED",
    "false, 1.0, 0.99, RESISTED",
    "true, 0.0, 0.5, IMMUNE",
    "true, 1.0, 0.0, IMMUNE"
  })
  @DisplayName("a gaze lands unless the target is immune or its mesmerise resistance wins the roll")
  void lands(
      final boolean immune,
      final double resistance,
      final float roll,
      final MesmeriseRules.Outcome expected) {
    assertEquals(expected, MesmeriseRules.land(immune, resistance, roll));
  }

  @ParameterizedTest(name = "boss {0}, self {1}, untouchable {2}, alive {3}, sheltered {4} -> {5}")
  @CsvSource({
    "false, false, false, true, false, false",
    "true, false, false, true, false, true",
    "false, true, false, true, false, true",
    "false, false, true, true, false, true",
    "false, false, false, false, false, true",
    "false, false, false, true, true, true"
  })
  @DisplayName(
      "bosses, the caster, creative players, the dead and those under a sanctuary cannot be"
          + " mesmerised")
  void immune(
      final boolean boss,
      final boolean self,
      final boolean untouchable,
      final boolean alive,
      final boolean sheltered,
      final boolean expected) {
    assertEquals(expected, MesmeriseRules.immune(boss, self, untouchable, alive, sheltered));
  }
}
