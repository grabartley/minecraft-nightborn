package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerSlotViewTest {

  @ParameterizedTest(name = "{0} ticks -> {1} s")
  @CsvSource({"0, 0", "1, 1", "10, 1", "20, 1", "21, 2", "600, 30", "-5, 0"})
  @DisplayName("seconds left round up, so a cooldown never shows 0 while it runs")
  void seconds(final long ticks, final long seconds) {
    assertEquals(seconds, PowerSlotView.seconds(ticks));
  }

  @ParameterizedTest(name = "{0} -> {1} rows")
  @CsvSource({"0.0, 0", "0.5, 8", "1.0, 16", "1.5, 16", "-1.0, 0", "0.04, 1"})
  @DisplayName("the shade covers the share of the icon still cooling")
  void shadedRows(final float fraction, final int rows) {
    assertEquals(rows, PowerSlotView.shadedRows(fraction));
  }

  @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
  @CsvSource({
    "R, R",
    "g, G",
    "F1, F1",
    "Left Shift, LS",
    "Mouse 4, M4",
    "Space, SP",
    "Right Control, RC",
    "Keypad 7, K7"
  })
  @DisplayName("every key shows a short label that fits in the slot corner")
  void keyLabel(final String bound, final String label) {
    assertEquals(label, PowerSlotView.keyLabel(bound, false));
  }

  @Test
  @DisplayName("an unbound key shows no label rather than a stray abbreviation")
  void unbound() {
    assertEquals("", PowerSlotView.keyLabel("Not bound", true));
  }
}
