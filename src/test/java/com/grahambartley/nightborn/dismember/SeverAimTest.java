package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeverAimTest {

  @ParameterizedTest(name = "direct {0}, projectile {1}, player {2}: {3}")
  @CsvSource({
    "true, false, true, LINE_OF_SIGHT",
    "true, false, false, MOB_WEIGHTS",
    "false, true, true, IMPACT",
    "false, true, false, IMPACT",
    "false, false, true, RANDOM_LIMB",
    "false, false, false, RANDOM_LIMB"
  })
  @DisplayName("players aim, mobs roll by weight, shots use the impact and blasts pick at random")
  void picks(
      final boolean direct, final boolean projectile, final boolean player, final SeverAim aim) {
    assertEquals(aim, SeverAim.of(direct, projectile, player));
  }
}
