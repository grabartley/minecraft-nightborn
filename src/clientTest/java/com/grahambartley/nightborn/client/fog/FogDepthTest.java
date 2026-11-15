package com.grahambartley.nightborn.client.fog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FogDepthTest {

  private static final float THREAT_RANGE = 8.0F;

  @ParameterizedTest(name = "vanilla end {0} at closeness {1} gives {2}")
  @CsvSource({
    "192, 0.0, 192",
    "192, 1.0, 20",
    "192, 0.5, 63",
    "100, 1.0, 20",
    "16, 1.0, 16",
    "20, 0.7, 20"
  })
  @DisplayName("fog closes in towards the heart and never pushes back a nearer vanilla fog")
  void end(final float vanilla, final float closeness, final float expected) {
    assertEquals(expected, FogDepth.end(vanilla, closeness), 0.01F);
  }

  @ParameterizedTest(name = "vanilla end {0} at closeness {1}")
  @CsvSource({"192, 1.0", "192, 0.9", "64, 1.0", "32, 0.5", "300, 1.0", "192, 2.0"})
  @DisplayName("a threat 8 blocks away is never fully hidden by structure fog")
  void neverHidesNearbyThreats(final float vanilla, final float closeness) {
    final float end = FogDepth.end(vanilla, closeness);
    final float start = FogDepth.start(vanilla * 0.9F, closeness, end);
    assertTrue(end > THREAT_RANGE, "end " + end);
    assertTrue(start <= end, "start " + start + " passes end " + end);
  }

  @ParameterizedTest(name = "start {0}, closeness {1}, end {2} gives {3}")
  @CsvSource({"170, 0.0, 192, 170", "170, 1.0, 20, 0", "170, 0.5, 63, 42.5", "170, 0.0, 100, 100"})
  @DisplayName("fog starts at the eye at the heart and never starts beyond its end")
  void start(final float vanilla, final float closeness, final float end, final float expected) {
    assertEquals(expected, FogDepth.start(vanilla, closeness, end), 0.01F);
  }

  @ParameterizedTest(name = "{0} towards {1} is {2}")
  @CsvSource({"0.0, 1.0, 0.05", "1.0, 0.0, 0.95", "0.5, 0.52, 0.52", "0.3, 0.3, 0.3"})
  @DisplayName("the fog shown eases towards its target one small step a tick")
  void approaches(final float shown, final float target, final float expected) {
    assertEquals(expected, FogDepth.approach(shown, target), 1.0E-6F);
  }
}
