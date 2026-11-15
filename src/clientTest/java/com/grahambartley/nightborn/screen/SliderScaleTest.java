package com.grahambartley.nightborn.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SliderScaleTest {

  @ParameterizedTest(name = "{0}..{1} at {2} = {3}")
  @CsvSource({
    "0, 100, 0.0, 0",
    "0, 100, 1.0, 100",
    "0, 100, 0.333, 33",
    "5, 600, 0.5, 303",
    "1, 1, 0.7, 1"
  })
  void valueAt(final int min, final int max, final double fraction, final int value) {
    assertEquals(value, new SliderScale(min, max).valueAt(fraction));
  }

  @ParameterizedTest(name = "{0}..{1}: {2} at {3}")
  @CsvSource({"0, 100, 25, 0.25", "0, 100, -5, 0.0", "0, 100, 500, 1.0", "1, 1, 1, 0.0"})
  void fractionOf(final int min, final int max, final int value, final double fraction) {
    assertEquals(fraction, new SliderScale(min, max).fractionOf(value), 0.0001);
  }
}
