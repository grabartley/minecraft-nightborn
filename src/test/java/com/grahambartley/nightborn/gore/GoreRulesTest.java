package com.grahambartley.nightborn.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.fight.Blow;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GoreRulesTest {

  @ParameterizedTest(name = "bleeds {0}, vampire {1}, bite {2}, stake {3}, silver {4} -> {5}")
  @CsvSource({
    "true, false, true, false, false, BITE",
    "true, true, true, false, false, BITE",
    "true, true, false, true, false, STAKE",
    "true, false, false, true, false, STAKE",
    "true, true, false, true, true, STAKE",
    "true, true, false, false, true, SILVER",
    "true, false, false, false, true, ",
    "true, false, false, false, false, ",
    "false, false, true, true, true, "
  })
  @DisplayName(
      "only bites, stakes and silver on a vampire spray blood, and only from things that bleed")
  void onHit(
      final boolean bleeds,
      final boolean vampire,
      final boolean bite,
      final boolean stake,
      final boolean silver,
      final Gore expected) {
    assertEquals(
        Optional.ofNullable(expected),
        GoreRules.onHit(bleeds, vampire, new Blow(bite, stake, silver, false, false)));
  }

  @ParameterizedTest(name = "bleeds {0}, vampire {1}, staked {2}, bite {3}, silver {4} -> {5}")
  @CsvSource({
    "true, true, true, false, false, STAKED",
    "true, false, true, false, false, STAKE",
    "true, false, false, true, false, DRAINED",
    "true, true, false, true, false, BITE",
    "true, true, false, false, true, SILVER",
    "true, false, false, false, false, ",
    "false, true, true, true, true, "
  })
  @DisplayName("a staked vampire and a drained victim get their own death effects")
  void onDeath(
      final boolean bleeds,
      final boolean vampire,
      final boolean staked,
      final boolean bite,
      final boolean silver,
      final Gore expected) {
    assertEquals(
        Optional.ofNullable(expected),
        GoreRules.onDeath(bleeds, vampire, new Blow(bite, staked, silver, false, false)));
  }

  @ParameterizedTest(name = "dx {0}, dz {1} -> {2}")
  @CsvSource({"1, 0, 0", "0, 1, 90", "-1, 0, 180", "0, -1, -90", "1, 1, 45"})
  @DisplayName("blood sprays away from whatever struck the blow")
  void sprayYaw(final double dx, final double dz, final float expected) {
    assertEquals(expected, GoreRules.sprayYaw(dx, dz), 1.0e-4F);
  }

  @ParameterizedTest(name = "dx {0}, dz {1}")
  @CsvSource({"0, 0", "0.0, -0.0"})
  @DisplayName("a blow from the same spot has no direction, so the client picks one")
  void noDirection(final double dx, final double dz) {
    assertEquals(Float.NaN, GoreRules.sprayYaw(dx, dz));
  }
}
