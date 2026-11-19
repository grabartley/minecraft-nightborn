package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LungePathTest {

  @ParameterizedTest(name = "from {0},{1} to {2},{3} at y {4}, gap {5}: {6},{7},{8}")
  @CsvSource({
    "0, 0, 5, 0, 0, 1, 4, 1.62, 0",
    "0, 0, 0, 4, 2, 1, 0, 3.62, 3",
    "0, 0, 3, 4, 0, 1, 2.4, 1.62, 3.2",
    "0, 0, 0.5, 0, 0, 1, 0, 1.62, 0"
  })
  @DisplayName("a lunge stops just short of its target, at the target's height")
  void wanted(
      final double fromX,
      final double fromZ,
      final double toX,
      final double toZ,
      final double toY,
      final double gap,
      final double x,
      final double y,
      final double z) {
    final Vec3d eye =
        LungePath.wanted(new Vec3d(fromX, 0.0, fromZ), new Vec3d(toX, toY, toZ), 1.62, gap);
    assertEquals(x, eye.x, 1.0E-9);
    assertEquals(y, eye.y, 1.0E-9);
    assertEquals(z, eye.z, 1.0E-9);
  }
}
