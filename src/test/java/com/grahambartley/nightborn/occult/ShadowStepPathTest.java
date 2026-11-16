package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @Test
  @DisplayName("landing spots run from the far end back toward the caster, stopping short of them")
  void candidates() {
    final List<Vec3d> points = ShadowStepPath.candidates(Vec3d.ZERO, new Vec3d(4.0, 0.0, 0.0));
    assertEquals(
        List.of(
            new Vec3d(4.0, 0.0, 0.0),
            new Vec3d(3.5, 0.0, 0.0),
            new Vec3d(3.0, 0.0, 0.0),
            new Vec3d(2.5, 0.0, 0.0),
            new Vec3d(2.0, 0.0, 0.0)),
        points);
  }

  @ParameterizedTest(name = "{0} away -> {1} spots")
  @CsvSource({"0.0, 0", "1.9, 0", "2.0, 1", "16.0, 29"})
  @DisplayName("a step too short to matter has nowhere to land")
  void tooShort(final double length, final int expected) {
    assertEquals(
        expected, ShadowStepPath.candidates(Vec3d.ZERO, new Vec3d(0.0, 0.0, length)).size());
  }
}
