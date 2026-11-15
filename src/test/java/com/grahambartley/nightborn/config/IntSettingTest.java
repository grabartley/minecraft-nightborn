package com.grahambartley.nightborn.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntSettingTest {

  private static final IntSetting RANGE = new IntSetting("test", "range", 5, 1, 10, c -> 0);

  @ParameterizedTest(name = "{0} becomes {1}")
  @CsvSource({"-4, 1", "0, 1", "1, 1", "7, 7", "10, 10", "11, 10", "2147483647, 10"})
  @DisplayName("values are clamped into the setting's bounds")
  void clamps(final int value, final int expected) {
    assertEquals(expected, RANGE.clamp(value));
  }

  @ParameterizedTest(name = "{0} inside: {1}")
  @CsvSource({"0, false", "1, true", "10, true", "11, false", "9999999999, false"})
  @DisplayName("contains reports whether a raw number is within bounds")
  void contains(final long value, final boolean expected) {
    assertEquals(expected, RANGE.contains(value));
  }

  @Test
  @DisplayName("a missing value normalises to the default")
  void missingIsDefault() {
    assertEquals(5, RANGE.normalise(null));
  }

  @ParameterizedTest(name = "default {0} in {1}..{2}")
  @CsvSource({"0, 1, 10", "11, 1, 10", "5, 10, 1"})
  @DisplayName("a default outside its own bounds is a programming error")
  void rejectsBadDefaults(final int defaultValue, final int min, final int max) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new IntSetting("test", "bad", defaultValue, min, max, c -> 0));
  }

  @Test
  @DisplayName("ids and translation keys follow section and key")
  void naming() {
    assertEquals("test.range", RANGE.id());
    assertEquals("nightborn.setting.test.range", RANGE.translationKey());
    assertEquals("nightborn.setting.test.range.tooltip", RANGE.tooltipKey());
  }
}
