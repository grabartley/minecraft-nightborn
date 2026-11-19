package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BladePointsTest {

  private static final double EPSILON = 1.0E-6;

  @ParameterizedTest(name = "yaw {0}, right arm {1}, along {2}: ({3}, {4}, {5})")
  @CsvSource({
    "0, true, 0.0, -0.37, 0.756, 0.15",
    "0, false, 0.0, 0.37, 0.756, 0.15",
    "0, true, 1.0, -0.37, 0.806, 0.9",
    "90, true, 1.0, -0.9, 0.806, -0.37",
    "180, false, 0.5, -0.37, 0.781, -0.525"
  })
  @DisplayName("blood fire runs along the blade held out from the wielder's sword hand")
  void along(
      final float yaw,
      final boolean right,
      final double t,
      final double x,
      final double y,
      final double z) {
    final Vec3d at = BladePoints.along(yaw, right, 1.8, t);
    assertEquals(x, at.x, EPSILON);
    assertEquals(y, at.y, EPSILON);
    assertEquals(z, at.z, EPSILON);
  }
}
