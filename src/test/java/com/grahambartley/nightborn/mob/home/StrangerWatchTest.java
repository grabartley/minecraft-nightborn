package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StrangerWatchTest {

  @ParameterizedTest(name = "player {0}, vampire {1}, creative or spectator {2}: stranger {3}")
  @CsvSource({
    "true, false, false, true",
    "true, true, false, false",
    "true, false, true, false",
    "false, false, false, false"
  })
  @DisplayName("a stranger is a human player the vampire could see as a threat")
  void stranger(
      final boolean player, final boolean vampire, final boolean exempt, final boolean stranger) {
    assertEquals(stranger, StrangerWatch.stranger(player, vampire, exempt));
  }

  @ParameterizedTest(name = "{0} blocks away: {1}")
  @CsvSource({"12, FOLLOW", "6.1, FOLLOW", "6, WATCH", "3, WATCH", "2.9, WARN", "0.5, WARN"})
  @DisplayName("a fed vampire follows a far stranger, watches a near one and warns off a close one")
  void move(final double distance, final StrangerWatch.Move move) {
    assertEquals(move, StrangerWatch.move(distance));
  }

  @ParameterizedTest(name = "{0} blocks away, near home {1}: keeps watching {2}")
  @CsvSource({"10, true, true", "18, true, true", "18.5, true, false", "4, false, false"})
  @DisplayName("a vampire lets a stranger go once it is far off or the vampire strays from home")
  void keepsWatching(final double distance, final boolean nearHome, final boolean keeps) {
    assertEquals(keeps, StrangerWatch.keepsWatching(distance, nearHome));
  }
}
