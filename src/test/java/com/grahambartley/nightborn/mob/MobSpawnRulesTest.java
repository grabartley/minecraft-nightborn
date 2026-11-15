package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobSpawnRulesTest {

  @ParameterizedTest(name = "time {0}, open sky {1}, roll {2}, chance {3}: spawns {4}")
  @CsvSource({
    "14000, true, 10, 100, true",
    "6000, true, 10, 100, false",
    "14000, false, 10, 100, false",
    "14000, true, 50, 50, false",
    "14000, true, 49, 50, true",
    "14000, true, 0, 0, false"
  })
  @DisplayName("vampires spawn only at night in the open, within the spawn chance")
  void vampire(
      final long time,
      final boolean openSky,
      final int roll,
      final int chance,
      final boolean spawns) {
    assertEquals(spawns, MobSpawnRules.vampireMaySpawn(false, time, openSky, roll, chance));
  }

  @ParameterizedTest(name = "time {0}, open sky {1}, light {2}, roll {3}: spawns {4}")
  @CsvSource({
    "6000, false, 0, 10, true",
    "6000, false, 4, 10, true",
    "6000, false, 5, 10, false",
    "6000, true, 0, 10, false",
    "14000, true, 0, 10, true",
    "14000, true, 0, 100, false"
  })
  @DisplayName("vampire bats spawn in dark caves and at night, within the spawn chance")
  void bat(
      final long time,
      final boolean openSky,
      final int light,
      final int roll,
      final boolean spawns) {
    assertEquals(spawns, MobSpawnRules.batMaySpawn(false, time, openSky, light, roll, 100));
  }

  @ParameterizedTest(name = "roll {0}, chance {1}, extra {2}: {3} bats")
  @CsvSource({"39, 40, 0, 1", "39, 40, 1, 2", "39, 40, 7, 2", "40, 40, 1, 0", "0, 0, 1, 0"})
  @DisplayName("a vampire comes alone or with one or two bats")
  void escort(final int roll, final int chance, final int extra, final int bats) {
    assertEquals(bats, MobSpawnRules.escortSize(roll, chance, extra));
  }
}
