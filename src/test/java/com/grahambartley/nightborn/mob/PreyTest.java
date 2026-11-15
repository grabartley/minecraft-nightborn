package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PreyTest {

  @ParameterizedTest(name = "warm blood {0}, vampire {1}, untouchable player {2}: prey {3}")
  @CsvSource({
    "true, false, false, true",
    "false, false, false, false",
    "true, true, false, false",
    "true, false, true, false"
  })
  @DisplayName("only warm-blooded creatures that are not vampires are prey")
  void prey(
      final boolean warmBlood,
      final boolean vampire,
      final boolean untouchable,
      final boolean prey) {
    assertEquals(prey, Prey.isPrey(warmBlood, vampire, untouchable));
  }

  @ParameterizedTest(name = "fixed time {0}, time {1}: vampire hunts {2}")
  @CsvSource({
    "false, 6000, false",
    "false, 14000, true",
    "false, 23500, false",
    "true, 14000, false"
  })
  @DisplayName("vampire mobs hunt only at night")
  void vampireHunts(final boolean fixedTime, final long time, final boolean hunts) {
    assertEquals(hunts, Prey.vampireHunts(fixedTime, time));
  }

  @ParameterizedTest(name = "time {0}, open sky {1}: bat hunts {2}")
  @CsvSource({"6000, true, false", "6000, false, true", "14000, true, true", "14000, false, true"})
  @DisplayName("vampire bats hunt in the dark under cover, and in the open only at night")
  void batHunts(final long time, final boolean openSky, final boolean hunts) {
    assertEquals(hunts, Prey.batHunts(false, time, openSky));
  }
}
