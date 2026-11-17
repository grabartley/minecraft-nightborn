package com.grahambartley.nightborn.levitation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalDouble;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LevitationLiftTest {

  @ParameterizedTest(name = "feet {0} over ground {1}: {2}")
  @CsvSource({
    "65, 64, 0",
    "64, 64, 0.3",
    "64.5, 64, 0.15",
    "64.9, 64, 0.05",
    "64.97, 64, 0.03",
    "65.1, 64, -0.03",
    "70, 64, -0.05",
    "64, 64.5, 0.42"
  })
  @DisplayName("it floats one block above the ground, rises up steps and sinks slowly down drops")
  void hover(final double feet, final double ground, final double velocity) {
    assertEquals(
        velocity, LevitationLift.verticalVelocity(feet, OptionalDouble.of(ground), false), 0.0001);
  }

  @Test
  @DisplayName("with nothing below it sinks slowly instead of falling")
  void nothingBelow() {
    assertEquals(
        -LevitationLift.SINK_SPEED,
        LevitationLift.verticalVelocity(10, OptionalDouble.empty(), false));
  }

  @ParameterizedTest(name = "feet {0} over ground {1}: sinks fast")
  @CsvSource({"90, 64", "65, 64", "64.2, 64", "64, 64.5", "90,"})
  @DisplayName(
      "holding sneak sinks at the fast speed over a drop, through the hover height and all the"
          + " way onto the ground")
  void sinksFast(final double feet, final Double ground) {
    final OptionalDouble surface =
        ground == null ? OptionalDouble.empty() : OptionalDouble.of(ground);
    assertEquals(
        -LevitationLift.SNEAK_SINK_SPEED, LevitationLift.verticalVelocity(feet, surface, true));
  }

  @Test
  @DisplayName("a sneak sink covers a 20 block drop in a few seconds, far faster than the float")
  void sneakSinkIsFast() {
    final int sneaking = ticksToFall(20.0, true);
    final int floating = ticksToFall(20.0, false);
    assertTrue(sneaking <= 80, "took " + sneaking + " ticks");
    assertTrue(floating >= 5 * sneaking, "the float took " + floating + " ticks");
  }

  private static int ticksToFall(final double drop, final boolean sinksFast) {
    final double hover = 64.0 + LevitationLift.HOVER_HEIGHT;
    double feet = hover + drop;
    int ticks = 0;
    while (feet > hover + 0.01 && ticks < 10_000) {
      feet += LevitationLift.verticalVelocity(feet, OptionalDouble.of(64.0), sinksFast);
      ticks++;
    }
    return ticks;
  }

  @Test
  @DisplayName("settling a full block of drop reaches the hover height without overshooting")
  void settlesWithoutOvershoot() {
    double feet = 66.0;
    for (int tick = 0; tick < 400; tick++) {
      feet += LevitationLift.verticalVelocity(feet, OptionalDouble.of(64.0), false);
      assertTrue(feet >= 65.0 - 0.0001, "never below the hover height");
    }
    assertEquals(65.0, feet, 0.01);
  }

  @Test
  @DisplayName(
      "rising onto a one-block step reaches the hover height exactly, so the body clears the"
          + " step top instead of catching on its edge")
  void risesExactlyOntoAStep() {
    double feet = 65.0;
    int ticks = 0;
    while (feet < 66.0 && ticks < 40) {
      feet += LevitationLift.verticalVelocity(feet, OptionalDouble.of(65.0), false);
      assertTrue(feet <= 66.0, "never above the hover height");
      ticks++;
    }
    assertEquals(66.0, feet, 1.0E-9);
    assertTrue(ticks <= 20, "reaches the top within a second, took " + ticks);
  }

  @ParameterizedTest(name = "feet {0}, levitating {1}: path feet {2}")
  @CsvSource({"65, true, 64", "65.2, true, 64.2", "64, false, 64", "65, false, 65"})
  @DisplayName(
      "a levitating vampire follows a path from the ground under it, so it reaches each step it"
          + " floats over")
  void pathFeetY(final double feetY, final boolean levitating, final double pathFeet) {
    assertEquals(pathFeet, LevitationLift.pathFeetY(feetY, levitating), 1.0e-9);
  }
}
