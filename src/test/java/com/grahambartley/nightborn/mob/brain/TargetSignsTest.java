package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TargetSignsTest {

  @ParameterizedTest(name = "target at {0},{1} moved {2},{3}: running {4}")
  @CsvSource({
    "0, 10, 0, 0.2, true",
    "0, 10, 0, 0.15, true",
    "0, 10, 0, 0.1, false",
    "0, 10, 0, -0.3, false",
    "0, 10, 0.3, 0, false",
    "10, 10, 0.2, 0.2, true",
    "0, 0, 0.3, 0, false"
  })
  @DisplayName("a target is running when it moves away from the mob at a run")
  void running(
      final double x, final double z, final double dx, final double dz, final boolean running) {
    assertEquals(
        running,
        TargetSigns.running(new Vec3d(0, 64, 0), new Vec3d(x, 64, z), new Vec3d(dx, 0.0, dz)));
  }

  @ParameterizedTest(name = "ground {0}, fluid {1}, climbing {2}, riding {3}: airborne {4}")
  @CsvSource({
    "false, false, false, false, true",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "false, false, true, false, false",
    "false, false, false, true, false"
  })
  @DisplayName("a target is in the air when it stands, swims, climbs and rides on nothing")
  void airborne(
      final boolean ground,
      final boolean fluid,
      final boolean climbing,
      final boolean riding,
      final boolean airborne) {
    assertEquals(airborne, TargetSigns.airborne(ground, fluid, climbing, riding));
  }
}
