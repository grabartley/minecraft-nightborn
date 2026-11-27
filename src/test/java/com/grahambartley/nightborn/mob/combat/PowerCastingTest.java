package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerCastingTest {

  @ParameterizedTest(name = "target {0}, now {1}, next think {2} -> thinks {3}")
  @CsvSource({
    "true, 100, 100, true",
    "true, 101, 100, true",
    "true, 99, 100, false",
    "false, 100, 100, false",
    "false, 500, 100, false"
  })
  @DisplayName("a caster thinks about a power only with a target and once its pause is over")
  void thinksOnlyWithATargetAfterItsPause(
      final boolean target, final long now, final long next, final boolean thinks) {
    assertEquals(thinks, PowerCasting.thinks(target, now, next));
  }
}
