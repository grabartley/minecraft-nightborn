package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ZoneShapeTest {

  @ParameterizedTest(name = "({0}, {1}) in a circle of 3 at (5, -5): {2}")
  @CsvSource({
    "5, -5, true",
    "8, -5, true",
    "8.01, -5, false",
    "5, -8, true",
    "7.2, -7.2, false",
    "7.1, -7.1, true"
  })
  @DisplayName("a circle holds every point within its radius")
  void circle(final double x, final double z, final boolean inside) {
    assertEquals(inside, ZoneShape.circle(5.0, -5.0, 3.0).contains(x, z));
  }

  @ParameterizedTest(name = "({0}, {1}) in a line east from the origin: {2}")
  @CsvSource({
    "0, 0, true",
    "5, 0, true",
    "10, 0, true",
    "10.1, 0, false",
    "-0.1, 0, false",
    "5, 1.5, true",
    "5, -1.5, true",
    "5, 1.6, false",
    "5, -1.6, false"
  })
  @DisplayName("a line runs from its start toward its aim, as long and as wide as it is told")
  void line(final double x, final double z, final boolean inside) {
    assertEquals(inside, ZoneShape.line(0.0, 0.0, 40.0, 0.0, 1.5, 10.0).contains(x, z));
  }

  @ParameterizedTest(name = "aimed at ({0}, {1}): ends at ({2}, {3}), yaw {4}")
  @CsvSource({
    "0, 9, 0, 6, 0",
    "0, -9, 0, -6, 180",
    "9, 0, 6, 0, -90",
    "-9, 0, -6, 0, 90",
    "0, 0, 0, 6, 0"
  })
  @DisplayName("a line's end and yaw follow its aim, and no aim points south")
  void lineAim(
      final double aimX, final double aimZ, final double endX, final double endZ, final float yaw) {
    final ZoneShape shape = ZoneShape.line(0.0, 0.0, aimX, aimZ, 1.0, 6.0);
    assertEquals(endX, shape.endX(), 1.0E-9);
    assertEquals(endZ, shape.endZ(), 1.0E-9);
    assertEquals(endX / 2.0, shape.midX(), 1.0E-9);
    assertEquals(endZ / 2.0, shape.midZ(), 1.0E-9);
    assertEquals(yaw, shape.yaw(), 1.0E-3F);
  }

  @ParameterizedTest(name = "a circle at ({0}, {1})")
  @CsvSource({"0, 0", "4.5, -7.25"})
  @DisplayName("a circle's middle and end are its centre")
  void circleMiddle(final double x, final double z) {
    final ZoneShape shape = ZoneShape.circle(x, z, 2.0);
    assertEquals(x, shape.midX());
    assertEquals(z, shape.midZ());
    assertEquals(x, shape.endX());
    assertEquals(z, shape.endZ());
  }
}
