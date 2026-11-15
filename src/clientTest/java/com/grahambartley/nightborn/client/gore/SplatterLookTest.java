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
}
