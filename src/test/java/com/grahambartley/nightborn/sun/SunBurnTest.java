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

  @ParameterizedTest(name = "age {0} at {1}, last shaded at {2}: burns now {3}")
  @CsvSource({
    "20, 500, -9223372036854775808, true",
    "21, 500, -9223372036854775808, false",
    "21, 500, 499, true",
    "21, 500, 500, false",
    "21, 500, 450, false",
    "40, 500, 500, true"
  })
  @DisplayName("the sun burns once a second, and at once on losing a parasol's shade")
  void due(final int age, final long now, final long shadedAt, final boolean due) {
    assertEquals(due, SunBurn.due(age, now, shadedAt));
  }
}
