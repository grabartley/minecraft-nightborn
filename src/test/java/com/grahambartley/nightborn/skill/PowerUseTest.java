package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerUseTest {

  @ParameterizedTest(
      name = "power {0}, downed {1}, weakened {2}, cooling {3}, blood {4}/{5}, unseen {6} -> {7}")
  @CsvSource({
    "true, false, false, 0, 10, 3, false, READY",
    "true, false, false, 0, 3, 3, false, READY",
    "true, false, false, 0, 0, 0, false, READY",
    "false, false, false, 0, 20, 0, false, NO_POWER",
    "true, true, false, 0, 20, 3, false, HELPLESS",
    "true, true, true, 40, 0, 3, true, HELPLESS",
    "true, false, true, 0, 20, 3, false, WEAKENED",
    "true, false, false, 1, 20, 3, false, COOLING",
    "true, false, false, 1, 20, 3, true, COOLING",
    "true, false, false, 0, 2, 3, false, THIRSTY",
    "true, false, false, 0, 2, 3, true, THIRSTY",
    "true, false, false, 0, 20, 3, true, UNSEEN"
  })
  @DisplayName(
      "a power is refused while downed, weakened, cooling, short of blood or with nothing to stare at")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean weakened,
      final long cooling,
      final int blood,
      final int cost,
      final boolean unseen,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected, PowerUse.check(hasPower, helpless, weakened, cooling, blood, cost, unseen));
  }
}
