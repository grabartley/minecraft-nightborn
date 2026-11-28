package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.client.state.GoreLevel;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SplatterLookTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"FULL, FRESH, true", "REDUCED, DRIED, true", "OFF, HIDDEN, false"})
  void followsTheGoreOption(final GoreLevel gore, final SplatterLook look, final boolean drawn) {
    assertEquals(look, SplatterLook.of(gore));
    assertEquals(drawn, look.drawn());
  }

  @ParameterizedTest(name = "{0} lives {1} of the full time")
  @CsvSource({"FRESH, 1.0", "DRIED, 0.5", "HIDDEN, 0.0"})
  void reducedGoreKeepsBloodForLess(final SplatterLook look, final float share) {
    assertEquals(share, look.lifeShare(), 1.0e-6F);
  }

  @ParameterizedTest(name = "{0} over 0x{1}")
  @CsvSource({
    "FRESH, FF8C1620, FF8C1620",
    "HIDDEN, FF8C1620, FF8C1620",
    "DRIED, FFFFFFFF, FF6E5248",
    "DRIED, 80000000, 80000000",
    "DRIED, FF8C1620, FF3C0709"
  })
  void driedBloodIsDarkerAndKeepsItsAlpha(
      final SplatterLook look, final String argb, final String expected) {
    assertEquals(
        Integer.toUnsignedString((int) Long.parseLong(expected, 16), 16),
        Integer.toUnsignedString(look.apply((int) Long.parseLong(argb, 16)), 16));
  }

  @ParameterizedTest(name = "{0} -> {1} redraws={2}")
  @CsvSource({
    "FULL, FULL, false",
    "FULL, REDUCED, true",
    "REDUCED, OFF, true",
    "OFF, FULL, true",
    "OFF, OFF, false"
  })
  void redrawsOnlyWhenTheLookChanges(
      final GoreLevel before, final GoreLevel after, final boolean redraw) {
    assertEquals(redraw, SplatterLook.redrawNeeded(before, after));
    assertTrue(SplatterLook.FRESH.drawn());
  }

  @ParameterizedTest(name = "{0} at shade {1}: ({2}, {3}, {4})")
  @CsvSource({
    "FRESH, 1.0, 1.0, 1.0, 1.0",
    "FRESH, 0.5, 0.5, 0.5, 0.5",
    "DRIED, 1.0, 0.43137, 0.32157, 0.28235",
    "DRIED, 0.5, 0.21569, 0.16078, 0.14118"
  })
  void rgb(
      final SplatterLook look,
      final float shade,
      final float red,
      final float green,
      final float blue) {
    final float[] rgb = look.rgb(shade);
    assertEquals(red, rgb[0], 1.0e-4F);
    assertEquals(green, rgb[1], 1.0e-4F);
    assertEquals(blue, rgb[2], 1.0e-4F);
  }
}
