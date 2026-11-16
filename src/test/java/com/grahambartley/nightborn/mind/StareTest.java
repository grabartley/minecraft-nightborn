package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StareTest {

  @ParameterizedTest(name = "steered {0}, {1} deg, {2}/{3} blocks, sees {4} -> {5}")
  @CsvSource({
    "false, 0.0, 5.0, 12.0, true, true",
    "false, 12.0, 5.0, 12.0, true, true",
    "false, 12.5, 5.0, 12.0, true, false",
    "true, 90.0, 5.0, 12.0, true, true",
    "false, 0.0, 12.5, 12.0, true, false",
    "true, 0.0, 12.0, 12.0, true, true",
    "false, 0.0, 5.0, 12.0, false, false",
    "true, 0.0, 5.0, 12.0, false, false"
  })
  @DisplayName("a stare holds only in sight and in range, and a player must keep looking at it")
  void holds(
      final boolean steered,
      final double angle,
      final double distance,
      final double range,
      final boolean sees,
      final boolean expected) {
    assertEquals(expected, Stare.holds(steered, angle, distance, range, sees));
  }

  @ParameterizedTest(name = "({0},{1},{2}) to ({3},{4},{5}) = {6}")
  @CsvSource({
    "1, 0, 0, 1, 0, 0, 0.0",
    "1, 0, 0, 0, 0, 1, 90.0",
    "1, 0, 0, -1, 0, 0, 180.0",
    "1, 0, 0, 1, 1, 0, 45.0",
    "0, 0, 0, 1, 0, 0, 180.0"
  })
  @DisplayName("the angle between where the caster looks and where the target stands")
  void angle(
      final double lx,
      final double ly,
      final double lz,
      final double tx,
      final double ty,
      final double tz,
      final double expected) {
    assertEquals(
        expected, Stare.angleDegrees(new Vec3d(lx, ly, lz), new Vec3d(tx, ty, tz)), 1.0E-6);
  }
}
