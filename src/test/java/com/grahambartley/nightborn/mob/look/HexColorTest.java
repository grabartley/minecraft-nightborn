package com.grahambartley.nightborn.mob.look;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class HexColorTest {

  @ParameterizedTest(name = "{0} is {1}")
  @CsvSource({"#000000, 0", "#5A1A24, 5904932", "#ffffff, 16777215", "#1F4A3A, 2050618"})
  @DisplayName("a #RRGGBB colour reads as its RGB value and writes back in capitals")
  void parses(final String text, final int rgb) {
    assertEquals(rgb, HexColor.parse(text).getOrThrow());
    assertEquals(text.toUpperCase(java.util.Locale.ROOT), HexColor.format(rgb));
  }

  @ParameterizedTest(name = "\"{0}\"")
  @ValueSource(strings = {"", "5A1A24", "#5A1A2", "#5A1A245", "#GGGGGG", "red"})
  @DisplayName("anything that is not #RRGGBB is rejected")
  void rejects(final String text) {
    assertFalse(HexColor.parse(text).isSuccess());
  }
}
