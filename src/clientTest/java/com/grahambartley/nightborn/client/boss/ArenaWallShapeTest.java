package com.grahambartley.nightborn.client.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ArenaWallShapeTest {

  @ParameterizedTest(name = "radius {0}")
  @ValueSource(ints = {1, 4, 8, 16, 48})
  @DisplayName("the wall is a closed ring of short panels, every corner on the arena's edge")
  void ring(final int radius) {
    final int segments = ArenaWallShape.segments(radius);
    assertTrue(segments >= ArenaWallShape.FEWEST);
    for (int i = 0; i < segments; i++) {
      final double x = ArenaWallShape.x(i, segments, radius);
      final double z = ArenaWallShape.z(i, segments, radius);
      assertEquals(radius, Math.hypot(x, z), 1.0E-9);
      final double panel =
          Math.hypot(
              ArenaWallShape.x(i + 1, segments, radius) - x,
              ArenaWallShape.z(i + 1, segments, radius) - z);
      assertTrue(panel <= ArenaWallShape.SEGMENT + 1.0E-9 || segments == ArenaWallShape.FEWEST);
    }
    assertEquals(
        ArenaWallShape.x(0, segments, radius),
        ArenaWallShape.x(segments, segments, radius),
        1.0E-9);
    assertEquals(
        ArenaWallShape.z(0, segments, radius),
        ArenaWallShape.z(segments, segments, radius),
        1.0E-9);
  }

  @ParameterizedTest(name = "{0} blocks from the wall: alpha {1}")
  @CsvSource({"0, 0.75", "6, 0.45", "-6, 0.45", "12, 0.15", "-12, 0.15", "40, 0.15", "-40, 0.15"})
  @DisplayName("the wall is strongest up close and fades to a faint veil from either side")
  void alpha(final double fromWall, final float alpha) {
    assertEquals(alpha, ArenaWallShape.alpha(fromWall), 1.0E-6F);
  }
}
