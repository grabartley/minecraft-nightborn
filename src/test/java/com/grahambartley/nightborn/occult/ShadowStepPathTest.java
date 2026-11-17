package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ShadowStepPathTest {

  @ParameterizedTest(name = "caster ({0},{1}) target ({2},{3}) behind {4} -> ({5},{6})")
  @CsvSource({
    "0, 0, 10, 0, 1.5, 11.5, 0",
    "0, 0, 0, 10, 2.0, 0, 12",
    "5, 5, 5, 5, 1.5, 5, 5",
    "0, 0, -4, 0, 0.0, -4, 0"
  })
  @DisplayName("a stepping mob lands just past its target, on the far side from where it stood")
  void behind(
      final double cx,
      final double cz,
      final double tx,
      final double tz,
      final double distance,
      final double ex,
      final double ez) {
    assertEquals(
        new Vec3d(ex, 64.0, ez),
        ShadowStepPath.behind(new Vec3d(cx, 70.0, cz), new Vec3d(tx, 64.0, tz), distance));
  }

  @ParameterizedTest(name = "target ({0},{1}) behind {2} range {3} -> eye at ({4},{5},{6})")
  @CsvSource({
    "6, 0, 1.5, 16, 7.5, 65.6, 0",
    "20, 0, 1.5, 16, 16, 65.6, 0",
    "0, 4, 2.0, 16, 0, 65.6, 6"
  })
  @DisplayName("a stepping mob aims just past its target, cut short at its range")
  void aimBehind(
      final double tx,
      final double tz,
      final double behind,
      final double range,
      final double ex,
      final double ey,
      final double ez) {
    final Vec3d aim =
        ShadowStepPath.aimBehind(
            new Vec3d(0.0, 64.0, 0.0), 1.6, new Vec3d(tx, 64.0, tz), behind, range);
    assertEquals(ex, aim.x, 1.0E-9);
    assertEquals(ey, aim.y, 1.0E-9);
    assertEquals(ez, aim.z, 1.0E-9);
  }
}
