package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodRoundsTest {

  @ParameterizedTest(name = "animal {0}, tame {1}, spared {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("a servant bleeds only wild animals, never pets, mounts or beasts that fight back")
  void prey(final boolean animal, final boolean tame, final boolean spared, final boolean prey) {
    assertEquals(prey, BloodRounds.prey(animal, tame, spared));
  }

  @ParameterizedTest(name = "{0} ticks since -> {1}")
  @CsvSource({"0, false", "19, false", "20, true", "100, true"})
  @DisplayName("a servant draws one bottle at a time, not a flurry")
  void drawsNow(final int since, final boolean draws) {
    assertEquals(draws, BloodRounds.drawsNow(since));
  }
}
