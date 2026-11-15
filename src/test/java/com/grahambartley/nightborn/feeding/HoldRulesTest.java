package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HoldRulesTest {

  @ParameterizedTest(name = "velocity {0}, {1}, {2}")
  @CsvSource({
    "0.0, 0.0, 0.0",
    "0.3, 0.0, -0.2",
    "-0.5, -0.08, 0.5",
    "0.1, 0.42, 0.1",
    "-1.2, -3.0, 2.4"
  })
  @DisplayName("a held victim keeps only its fall and never moves sideways")
  void pinned(final double x, final double y, final double z) {
    assertEquals(new Vec3d(0.0, y, 0.0), HoldRules.pinned(new Vec3d(x, y, z)));
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
