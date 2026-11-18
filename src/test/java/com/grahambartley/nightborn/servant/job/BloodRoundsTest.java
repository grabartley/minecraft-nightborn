package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodRoundsTest {

  @ParameterizedTest(name = "lancet {0}, empty bottle {1} -> {2}")
  @CsvSource({
    "true, true, true",
    "true, false, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a servant bleeds only with a lancet and an empty bottle, as a player does")
  void equipped(final boolean lancet, final boolean bottle, final boolean equipped) {
    assertEquals(equipped, BloodRounds.equipped(lancet, bottle));
  }

  @ParameterizedTest(name = "{0} ticks since -> {1}")
  @CsvSource({"0, false", "19, false", "20, true", "100, true"})
  @DisplayName("a servant draws one bottle at a time, not a flurry")
  void drawsNow(final int since, final boolean draws) {
    assertEquals(draws, BloodRounds.drawsNow(since));
  }
}
