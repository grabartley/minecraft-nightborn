package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerCastingTest {

  @ParameterizedTest(name = "engaged {0}, now {1}, next think {2} -> thinks {3}")
  @CsvSource({
    "true, 100, 100, true",
    "true, 101, 100, true",
    "true, 99, 100, false",
    "false, 100, 100, false",
    "false, 500, 100, false"
  })
  @DisplayName(
      "a caster thinks about a power only while fighting a target or escaping, once its pause"
          + " is over")
  void thinksOnlyWhileEngagedAfterItsPause(
      final boolean engaged, final long now, final long next, final boolean thinks) {
    assertEquals(thinks, PowerCasting.thinks(engaged, now, next));
  }
}
