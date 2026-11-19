package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AreaRulesTest {

  @ParameterizedTest(name = "distance² {0}, radius {1} -> {2}")
  @CsvSource({"0, 6, true", "15.9, 4, true", "16, 4, true", "16.1, 4, false", "100.5, 10, false"})
  @DisplayName("an area reaches its radius and no further")
  void inside(final double distance, final float radius, final boolean inside) {
    assertEquals(inside, AreaRules.inside(distance, radius));
  }

  @ParameterizedTest(name = "({0}, {1}) from the centre -> {2}")
  @CsvSource({"0, 0, 0", "3, 4, 25", "-2, 0, 4"})
  @DisplayName("a flat distance is measured only across the ground, whatever the height")
  void flat(final double x, final double z, final double expected) {
    assertEquals(expected, AreaRules.flat(new Vec3d(x, 40.0, z), Vec3d.ZERO));
  }

  @ParameterizedTest(name = "at ({0}, {1}, {2}), radius {3}, height {4} -> {5}")
  @CsvSource({
    "0, 0, 0, 2, 8, true",
    "1, 7.5, 1, 2, 8, true",
    "0, 9, 0, 2, 8, false",
    "0, -0.5, 0, 2, 8, true",
    "0, -2, 0, 2, 8, false",
    "2, 3, 1, 2, 8, false"
  })
  @DisplayName("a column covers its circle from just below the ground up to its height")
  void column(
      final double x,
      final double y,
      final double z,
      final float radius,
      final float height,
      final boolean expected) {
    assertEquals(expected, AreaRules.column(new Vec3d(x, y, z), Vec3d.ZERO, radius, height));
  }

  @ParameterizedTest(name = "now {0}, until {1} -> {2}")
  @CsvSource({"0, 400, true", "399, 400, true", "400, 400, false", "401, 400, false"})
  @DisplayName("something timed lasts until its time is up")
  void lasts(final long now, final long until, final boolean lasts) {
    assertEquals(lasts, AreaRules.lasts(now, until));
  }
}
