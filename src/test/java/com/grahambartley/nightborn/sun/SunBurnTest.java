package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SunBurnTest {

  @ParameterizedTest(name = "{0} sun damage at {1}: {2}")
  @CsvSource({"4.0, 1.0, 4.0", "4.0, 1.25, 5.0", "2.0, 1.25, 2.5", "0.0, 1.25, 0.0"})
  @DisplayName("Sunmarked makes the sun burn a quarter harder")
  void damage(final float sun, final double sunmarked, final float expected) {
    assertEquals(expected, SunBurn.damage(sun, sunmarked), 1.0E-6F);
  }
}
