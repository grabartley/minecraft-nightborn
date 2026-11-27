package com.grahambartley.nightborn.mob.home;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobHungerTest {

  @ParameterizedTest(name = "blood {0}, threshold {1}: hungry {2}")
  @CsvSource({"13, 14, true", "14, 14, false", "20, 14, false", "0, 0, false", "0, 1, true"})
  @DisplayName("a vampire mob is hungry below its threshold")
  void hungry(final int blood, final int threshold, final boolean hungry) {
    assertEquals(hungry, MobHunger.hungry(blood, threshold));
  }

  @ParameterizedTest(name = "threshold {0}, roll {1}: starts with {2}")
  @CsvSource({
    "14, 0, 14",
    "14, 6, 20",
    "14, 7, 14",
    "14, -1, 20",
    "0, 20, 20",
    "0, 21, 0",
    "20, 5, 20",
    "30, 3, 20",
    "-4, 3, 3"
  })
  @DisplayName("a new vampire starts fed, somewhere between its threshold and a full bar")
  void startingBlood(final int threshold, final int roll, final int blood) {
    assertEquals(blood, MobHunger.startingBlood(threshold, roll));
  }

  @ParameterizedTest(name = "threshold {0}, roll {1}: starts with {2}")
  @CsvSource({
    "14, 0, 6",
    "14, 7, 13",
    "14, 8, 6",
    "14, -1, 13",
    "1, 5, 0",
    "30, 0, 10",
    "30, 10, 20",
    "0, 3, 3",
    "-4, 3, 3"
  })
  @DisplayName("a vampire that starts hungry has between half its threshold and just under it")
  void hungryBlood(final int threshold, final int roll, final int blood) {
    assertEquals(blood, MobHunger.hungryBlood(threshold, roll));
  }
}
