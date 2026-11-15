package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

  @ParameterizedTest(name = "roll {0} of {1}%, vanilla cave {2}, structure {3}: spawns {4}")
  @CsvSource({
    "10, 100, true, false, true",
    "10, 100, false, true, true",
    "10, 100, true, true, true",
    "10, 100, false, false, false",
    "99, 100, true, false, true",
    "50, 50, true, true, false",
    "0, 0, true, true, false"
  })
  @DisplayName("vampire bats spawn only where a vanilla bat could or inside a vampire structure")
  void bat(
      final int roll,
      final int chance,
      final boolean cave,
      final boolean structure,
      final boolean spawns) {
    assertEquals(spawns, MobSpawnRules.batMaySpawn(roll, chance, () -> cave, () -> structure));
  }

  @ParameterizedTest(name = "chance roll {0}")
  @CsvSource({"100", "57"})
  @DisplayName("a failed chance roll never checks the cave or looks for a structure")
  void skipsTheLookups(final int roll) {
    assertFalse(
        MobSpawnRules.batMaySpawn(
            roll,
            50,
            () -> {
              throw new AssertionError("checked the cave");
            },
            () -> {
              throw new AssertionError("looked for a structure");
            }));
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
