package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HuntingTest {

  @ParameterizedTest(name = "peaceful {0}, time {1}, open sky {2}: bat hunts {3}")
  @CsvSource({
    "false, 6000, true, false",
    "false, 6000, false, true",
    "false, 14000, true, true",
    "false, 14000, false, true",
    "true, 14000, true, false",
    "true, 6000, false, false"
  })
  @DisplayName("vampire bats hunt under cover, or in the open at night, never in peaceful")
  void batHunts(
      final boolean peaceful, final long time, final boolean openSky, final boolean hunts) {
    assertEquals(hunts, Hunting.batHunts(peaceful, false, time, openSky));
  }

  @ParameterizedTest(name = "has prey {0}, age {1}: searches {2}")
  @CsvSource({
    "false, 0, true",
    "false, 20, true",
    "false, 40, true",
    "false, 1, false",
    "false, 19, false",
    "false, 21, false",
    "true, 0, false",
    "true, 20, false",
    "true, 7, false"
  })
  @DisplayName("a bat with no prey looks for some once every search interval")
  void batSearches(final boolean hasPrey, final int age, final boolean searches) {
    assertEquals(searches, Hunting.batSearches(hasPrey, age, 20));
  }

  @ParameterizedTest(name = "has prey {0}, bite cooldown {1}: swoops {2}")
  @CsvSource({
    "true, 0, true",
    "true, 1, false",
    "true, 50, false",
    "false, 0, false",
    "false, 50, false"
  })
  @DisplayName("a bat swoops only at prey and only once its last bite has worn off")
  void batSwoops(final boolean hasPrey, final int biteCooldown, final boolean swoops) {
    assertEquals(swoops, Hunting.batSwoops(hasPrey, biteCooldown));
  }

  @ParameterizedTest(
      name = "hours {0}, hungry {1}, endangered {2}, provoked {3}, out of range {4}: drops {5}")
  @CsvSource({
    "true, true, false, false, false, false",
    "true, false, false, false, false, true",
    "false, true, false, false, false, true",
    "true, true, false, false, true, true",
    "true, false, false, true, false, false",
    "false, false, false, true, false, false",
    "false, false, false, true, true, true",
    "true, true, true, false, false, true",
    "false, true, true, true, false, true"
  })
  @DisplayName(
      "a vampire keeps prey only while hungry at night in range, and a foe until it strays too"
          + " far from home or needs shade")
  void dropsTarget(
      final boolean hours,
      final boolean hungry,
      final boolean endangered,
      final boolean provoked,
      final boolean outOfRange,
      final boolean drops) {
    assertEquals(
        drops,
        Hunting.dropsTarget(new Hunting.Pursuit(hours, hungry, endangered, provoked, outOfRange)));
  }
}
