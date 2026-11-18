package com.grahambartley.nightborn.sun;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.OptionalLong;
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

  @ParameterizedTest(name = "now {0}, last lit at {1}: burns again {2}")
  @CsvSource({
    "100, -1, true",
    "100, 100, false",
    "119, 100, false",
    "120, 100, true",
    "500, 100, true"
  })
  @DisplayName("the sun sets a vampire alight the moment it reaches them and every second after")
  void due(final long now, final long litAt, final boolean due) {
    assertEquals(
        due,
        SunBurn.due(
            now,
            litAt < 0
                ? OptionalLong.empty()
                : OptionalLong.of(litAt + SunFire.ticks(SunBurn.FIRE_SECONDS))));
  }
}
