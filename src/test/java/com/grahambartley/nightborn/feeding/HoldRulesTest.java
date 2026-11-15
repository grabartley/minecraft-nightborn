package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HoldRulesTest {

  @ParameterizedTest(name = "velocity {0}, {1}, {2}, holds height {3}")
  @CsvSource({
    "0.0, 0.0, 0.0, false",
    "0.3, 0.0, -0.2, false",
    "-0.5, -0.08, 0.5, false",
    "0.1, 0.42, 0.1, false",
    "-1.2, -3.0, 2.4, false",
    "0.3, 0.2, -0.2, true",
    "-0.5, -0.08, 0.5, true"
  })
  @DisplayName("a held victim never moves sideways and keeps its fall unless it flies")
  void pinned(final double x, final double y, final double z, final boolean holdsHeight) {
    assertEquals(
        new Vec3d(0.0, holdsHeight ? 0.0 : y, 0.0),
        HoldRules.pinned(new Vec3d(x, y, z), holdsHeight));
  }

  @ParameterizedTest(name = "at {0}, {1}, {2}, holds height {3}")
  @CsvSource({
    "10.0, 64.0, 10.0, false",
    "12.5, 64.0, 9.0, false",
    "10.0, 66.0, 10.0, false",
    "12.5, 66.0, 9.0, true",
    "10.0, 60.0, 10.0, true"
  })
  @DisplayName("a drifted victim is put back on its anchor, at its own height unless it flies")
  void anchored(final double x, final double y, final double z, final boolean holdsHeight) {
    final Vec3d anchor = new Vec3d(10.0, 64.0, 10.0);
    assertEquals(
        new Vec3d(10.0, holdsHeight ? 64.0 : y, 10.0),
        HoldRules.anchored(anchor, new Vec3d(x, y, z), holdsHeight));
  }

  @ParameterizedTest(name = "offset {0}, {1}, {2}, holds height {3}: drifted {4}")
  @CsvSource({
    "0.0, 0.0, 0.0, false, false",
    "0.04, 0.0, 0.0, false, false",
    "0.06, 0.0, 0.0, false, true",
    "0.0, 0.0, -0.3, false, true",
    "0.0, 2.0, 0.0, false, false",
    "0.0, 2.0, 0.0, true, true",
    "0.0, 0.04, 0.0, true, false"
  })
  @DisplayName("drift counts past a small tolerance, and height only matters for a flier")
  void drifted(
      final double x,
      final double y,
      final double z,
      final boolean holdsHeight,
      final boolean drifted) {
    final Vec3d anchor = new Vec3d(10.0, 64.0, 10.0);
    assertEquals(drifted, HoldRules.drifted(anchor, anchor.add(new Vec3d(x, y, z)), holdsHeight));
  }

  @ParameterizedTest(name = "{1} ticks left at amplifier {0}: {2}")
  @CsvSource({
    "6, 4, false",
    "6, 2, false",
    "6, 1, true",
    "6, 0, true",
    "5, 40, true",
    "0, 600, true",
    "7, 1, true"
  })
  @DisplayName("the hold is applied again when it is weaker than ours or about to run out")
  void needsRefresh(final int amplifier, final int remaining, final boolean refresh) {
    assertEquals(refresh, HoldRules.needsRefresh(remaining, amplifier));
  }
}
