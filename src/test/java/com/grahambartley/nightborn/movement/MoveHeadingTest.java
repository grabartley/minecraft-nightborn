package com.grahambartley.nightborn.movement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoveHeadingTest {

  @ParameterizedTest(name = "strafe {0}, forward {1}, yaw {2}: ({3}, {4})")
  @CsvSource({
    "0, 1, 0, 0, 1",
    "0, 0.98, 90, -1, 0",
    "0, 1, 180, 0, -1",
    "1, 0, 0, 1, 0",
    "0, -1, 0, 0, -1",
    "0.98, 0.98, 0, 0.7071, 0.7071"
  })
  @DisplayName("movement keys and yaw give the way the body is being moved, as a unit length")
  void of(
      final double strafe, final double forward, final float yaw, final double x, final double z) {
    final MoveHeading heading = MoveHeading.of(strafe, forward, yaw).orElseThrow();
    assertEquals(x, heading.x(), 0.0001);
    assertEquals(z, heading.z(), 0.0001);
  }

  @ParameterizedTest(name = "strafe {0}, forward {1}")
  @CsvSource({"0, 0", "0.00001, 0", "0, -0.00001"})
  @DisplayName("no movement input gives no heading")
  void stillHasNoHeading(final double strafe, final double forward) {
    assertTrue(MoveHeading.of(strafe, forward, 45.0F).isEmpty());
  }
}
