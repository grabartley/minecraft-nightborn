package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerUseTest {

  @ParameterizedTest(
      name =
          "power {0}, downed {1}, weakened {2}, cooling {3}, blood {4}/{5}, unseen {6}, nowhere {7}"
              + " -> {8}")
  @CsvSource({
    "true, false, false, 0, 10, 3, false, false, READY",
    "true, false, false, 0, 3, 3, false, false, READY",
    "true, false, false, 0, 0, 0, false, false, READY",
    "false, false, false, 0, 20, 0, false, false, NO_POWER",
    "true, true, false, 0, 20, 3, false, false, HELPLESS",
    "true, true, true, 40, 0, 3, true, false, HELPLESS",
    "true, false, true, 0, 20, 3, false, false, WEAKENED",
    "true, false, false, 1, 20, 3, false, false, COOLING",
    "true, false, false, 1, 20, 3, true, false, COOLING",
    "true, false, false, 0, 2, 3, false, false, THIRSTY",
    "true, false, false, 0, 2, 3, true, false, THIRSTY",
    "true, false, false, 0, 20, 3, true, false, UNSEEN",
    "true, false, false, 0, 20, 3, true, true, UNSEEN",
    "true, false, false, 0, 20, 3, false, true, NOWHERE",
    "true, false, false, 0, 2, 3, false, true, THIRSTY"
  })
  @DisplayName(
      "a power is refused while downed, weakened, cooling, short of blood with nothing to stare at or nowhere to step")
  void checks(
      final boolean hasPower,
      final boolean helpless,
      final boolean weakened,
      final long cooling,
      final int blood,
      final int cost,
      final boolean unseen,
      final boolean nowhere,
      final PowerUse.Verdict expected) {
    assertEquals(
        expected,
        PowerUse.check(hasPower, helpless, weakened, cooling, blood, cost, unseen, nowhere));
  }
}
