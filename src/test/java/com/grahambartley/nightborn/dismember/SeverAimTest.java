package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SeverAimTest {

  @ParameterizedTest(name = "blast {0}, direct {1}, projectile {2}, player {3}: {4}")
  @CsvSource({
    "false, true, false, true, LINE_OF_SIGHT",
    "false, true, false, false, MOB_WEIGHTS",
    "false, false, true, true, IMPACT",
    "false, false, true, false, IMPACT",
    "false, false, false, true, RANDOM_LIMB",
    "false, false, false, false, RANDOM_LIMB",
    "true, true, false, false, RANDOM_LIMB",
    "true, true, false, true, RANDOM_LIMB",
    "true, false, true, false, RANDOM_LIMB"
  })
  @DisplayName("players aim, mobs roll by weight, shots use the impact and blasts pick at random")
  void picks(
      final boolean blast,
      final boolean direct,
      final boolean projectile,
      final boolean player,
      final SeverAim aim) {
    assertEquals(aim, SeverAim.of(blast, direct, projectile, player));
  }
}
