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

  @ParameterizedTest(
      name = "below sea {0}, light {1}, darkness roll {2}, structure {3}, roll {4}: spawns {5}")
  @CsvSource({
    "true, 0, 0, false, 10, true",
    "true, 3, 3, false, 10, true",
    "true, 3, 2, false, 10, false",
    "true, 15, 3, false, 10, false",
    "false, 0, 3, false, 10, false",
    "false, 15, 0, true, 10, true",
    "true, 15, 0, true, 10, true",
    "true, 0, 3, false, 100, false",
    "false, 0, 0, true, 100, false"
  })
  @DisplayName(
      "vampire bats spawn only in dark places below sea level or inside a vampire structure")
  void bat(
      final boolean belowSea,
      final int light,
      final int darknessRoll,
      final boolean inStructure,
      final int roll,
      final boolean spawns) {
    assertEquals(
        spawns,
        MobSpawnRules.batMaySpawn(belowSea, light, darknessRoll, () -> inStructure, roll, 100));
  }

  @ParameterizedTest(name = "chance roll {0}")
  @CsvSource({"100", "57"})
  @DisplayName("a failed chance roll never looks for a structure")
  void skipsTheStructureLookup(final int roll) {
    assertEquals(
        false,
        MobSpawnRules.batMaySpawn(
            false,
            0,
            0,
            () -> {
              throw new AssertionError("looked for a structure");
            },
            roll,
            50));
  }

  @ParameterizedTest(name = "roll {0}, chance {1}, extra {2}: {3} bats")
  @CsvSource({"39, 40, 0, 1", "39, 40, 1, 2", "39, 40, 7, 2", "40, 40, 1, 0", "0, 0, 1, 0"})
  @DisplayName("a vampire comes alone or with one or two bats")
  void escort(final int roll, final int chance, final int extra, final int bats) {
    assertEquals(bats, MobSpawnRules.escortSize(roll, chance, extra));
  }

  @ParameterizedTest(name = "{0} bats near: crowded {1}")
  @CsvSource({"0, false", "1, false", "2, true", "9, true"})
  @DisplayName("no more vampire bats spawn where a few already fly")
  void crowded(final int nearby, final boolean crowded) {
    assertEquals(crowded, MobSpawnRules.crowded(nearby));
  }
}
