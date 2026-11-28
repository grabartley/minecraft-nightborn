package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalJoinTest {

  @ParameterizedTest(name = "{0} at ({1}, {2}, {3}) joins a 0.5 splat on UP at the origin: {4}")
  @CsvSource({
    "UP, 0.0, 1.0, 0.0, true",
    "UP, 0.29, 1.0, 0.0, true",
    "UP, 0.31, 1.0, 0.0, false",
    "UP, 0.2, 1.0, 0.2, true",
    "UP, 0.25, 1.0, 0.25, false",
    "UP, 0.0, 1.01, 0.0, false",
    "UP, 0.0, 1.0005, 0.0, true",
    "NORTH, 0.0, 1.0, 0.0, false",
    "DOWN, 0.0, 1.0, 0.0, false"
  })
  @DisplayName("a splat landing mostly on another on the same face joins it")
  void joins(
      final Direction face, final double x, final double y, final double z, final boolean joins) {
    assertEquals(
        joins,
        DecalJoin.joins(Direction.UP, new Vec3d(0.0, 1.0, 0.0), 0.5, face, new Vec3d(x, y, z)));
  }

  @ParameterizedTest(name = "{0} grown by {1}: {2}")
  @CsvSource({
    "0.3, 0.4, 0.4123",
    "0.3, 0.0, 0.3",
    "0.9, 0.9, 1.0",
    "1.0, 0.2, 1.0",
    "0.2, 0.2, 0.2449"
  })
  @DisplayName("a joined patch grows by part of the new splat's area, up to a cap")
  void grown(final double half, final double incoming, final double expected) {
    assertEquals(expected, DecalJoin.grown(half, incoming), 1.0e-4);
  }
}
