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

  @ParameterizedTest(name = "hours {0}, hungry {1}, endangered {2}, out of range {3}: hunts {4}")
  @CsvSource({
    "true, true, false, false, true",
    "true, false, false, false, false",
    "false, true, false, false, false",
    "true, true, true, false, false",
    "true, true, false, true, false"
  })
  @DisplayName("only a hungry vampire hunts, at night, out of the sun and near enough to home")
  void huntsPrey(
      final boolean hours,
      final boolean hungry,
      final boolean endangered,
      final boolean outOfRange,
      final boolean hunts) {
    assertEquals(
        hunts,
        Hunting.huntsPrey(new Hunting.Pursuit(hours, hungry, endangered, false, outOfRange)));
  }
}
