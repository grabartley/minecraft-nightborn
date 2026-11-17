package com.grahambartley.nightborn.landing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class LandingPathTest {

  static Stream<Arguments> paths() {
    return Stream.of(
        Arguments.of(
            new Vec3d(4.0, 0.0, 0.0),
            List.of(
                new Vec3d(4.0, 0.0, 0.0),
                new Vec3d(3.5, 0.0, 0.0),
                new Vec3d(3.0, 0.0, 0.0),
                new Vec3d(2.5, 0.0, 0.0),
                new Vec3d(2.0, 0.0, 0.0))),
        Arguments.of(
            new Vec3d(0.0, 0.0, -3.0),
            List.of(
                new Vec3d(0.0, 0.0, -3.0), new Vec3d(0.0, 0.0, -2.5), new Vec3d(0.0, 0.0, -2.0))),
        Arguments.of(new Vec3d(0.0, 2.0, 0.0), List.of(new Vec3d(0.0, 2.0, 0.0))),
        Arguments.of(new Vec3d(1.0, 0.0, 1.0), List.of()));
  }

  @ParameterizedTest(name = "to {0}")
  @MethodSource("paths")
  @DisplayName("landing spots run from the far end back toward the caster, stopping short of them")
  void candidates(final Vec3d to, final List<Vec3d> expected) {
    assertEquals(expected, LandingPath.candidates(Vec3d.ZERO, to));
  }

  @ParameterizedTest(name = "{0} away -> {1} spots")
  @CsvSource({"0.0, 0", "1.9, 0", "2.0, 1", "16.0, 29"})
  @DisplayName("a move too short to matter has nowhere to land")
  void tooShort(final double length, final int expected) {
    assertEquals(expected, LandingPath.candidates(Vec3d.ZERO, new Vec3d(0.0, 0.0, length)).size());
  }
}
