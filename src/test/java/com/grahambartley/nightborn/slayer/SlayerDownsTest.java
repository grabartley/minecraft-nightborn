package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlayerDownsTest {

  @ParameterizedTest(
      name =
          "vampire {0}, boss {1}, reachable {2}, downed {3}, {4}/{5} below {6}, rank {7} up to"
              + " {8}: {9}")
  @CsvSource({
    "true, false, true, false, 7, 20, 0.4, 2, 2, true",
    "true, false, true, false, 8, 20, 0.4, 2, 2, false",
    "true, false, true, false, 7, 20, 0.4, 3, 2, false",
    "true, false, true, false, 5, 20, 0.3, 0, 2, true",
    "true, true, true, false, 1, 20, 0.4, 2, 2, false",
    "false, false, true, false, 1, 20, 0.4, 0, 2, false",
    "true, false, false, false, 1, 20, 0.4, 0, 2, false",
    "true, false, true, true, 1, 20, 0.4, 0, 2, false",
    "true, false, true, false, 0, 20, 0.4, 0, 2, false",
    "true, false, true, false, 1, 20, 0.4, 0, -1, false"
  })
  @DisplayName(
      "a skill downs a reachable, standing vampire no older than its stage and below its health"
          + " share, and never a boss")
  void downs(
      final boolean vampire,
      final boolean boss,
      final boolean reachable,
      final boolean downed,
      final float health,
      final float maxHealth,
      final float below,
      final int rank,
      final int mostRank,
      final boolean expected) {
    assertEquals(
        expected,
        SlayerDowns.downs(
            vampire, boss, reachable, downed, health, maxHealth, below, rank, mostRank));
  }
}
