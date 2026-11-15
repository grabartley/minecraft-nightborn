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
    "64, 64, 0.42",
    "64.5, 64, 0.225",
    "65.1, 64, -0.03",
    "70, 64, -0.05",
    "64, 64.5, 0.42"
  })
  @DisplayName("it floats one block above the ground, rises up steps and sinks slowly down drops")
  void hover(final double feet, final double ground, final double velocity) {
    assertEquals(
        velocity, LevitationLift.verticalVelocity(feet, OptionalDouble.of(ground)), 0.0001);
  }

  @Test
  @DisplayName("with nothing below it sinks slowly instead of falling")
  void nothingBelow() {
    assertEquals(
        -LevitationLift.SINK_SPEED, LevitationLift.verticalVelocity(10, OptionalDouble.empty()));
  }

  @Test
  @DisplayName("settling a full block of drop reaches the hover height without overshooting")
  void settlesWithoutOvershoot() {
    double feet = 66.0;
    for (int tick = 0; tick < 400; tick++) {
      feet += LevitationLift.verticalVelocity(feet, OptionalDouble.of(64.0));
      assertTrue(feet >= 65.0 - 0.0001, "never below the hover height");
    }
    assertEquals(65.0, feet, 0.01);
  }
}
