package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BearingTest {

  @ParameterizedTest(name = "dx {0}, dz {1} -> {2}")
  @CsvSource({
    "0, -100, NORTH",
    "100, -100, NORTH_EAST",
    "100, 0, EAST",
    "100, 100, SOUTH_EAST",
    "0, 100, SOUTH",
    "-100, 100, SOUTH_WEST",
    "-100, 0, WEST",
    "-100, -100, NORTH_WEST",
    "30, -100, NORTH",
    "100, -30, EAST",
    "0, 0, SOUTH"
  })
  @DisplayName("north is negative z and east is positive x, as the game's compass reads")
  void of(final double dx, final double dz, final Bearing bearing) {
    assertEquals(bearing, Bearing.of(dx, dz));
  }

  @ParameterizedTest
  @EnumSource(Bearing.class)
  @DisplayName("each bearing has its own words")
  void key(final Bearing bearing) {
    assertEquals(
        "nightborn.bearing." + bearing.name().toLowerCase(java.util.Locale.ROOT),
        bearing.translationKey());
  }
}
