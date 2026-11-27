package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntentBudgetTest {

  @ParameterizedTest(name = "reserve {0}, mob floor {1}, blood {2}, cost {3}: {4}")
  @CsvSource({
    "0, 0, 3, 3, true",
    "0, 0, 2, 3, false",
    "0, 10, 13, 3, true",
    "0, 10, 12, 3, false",
    "8, 0, 11, 3, true",
    "8, 0, 10, 3, false",
    "8, 10, 13, 3, true",
    "8, 12, 14, 3, false"
  })
  @DisplayName("a cast must leave the higher of the mob's floor and the intent's reserve")
  void affords(
      final int reserve,
      final int mobFloor,
      final int blood,
      final int cost,
      final boolean affords) {
    assertEquals(affords, new IntentBudget(reserve, 0, 0.0F).affords(blood, cost, mobFloor));
  }

  @ParameterizedTest(name = "threshold {0}, score {1}: {2}")
  @CsvSource({"0, 0, false", "0, 0.01, true", "0.5, 0.5, false", "0.5, 0.6, true"})
  @DisplayName("a score must be above the threshold")
  void passes(final float threshold, final double score, final boolean passes) {
    assertEquals(passes, new IntentBudget(0, 0, threshold).passes(score));
  }

  @Test
  @DisplayName("an empty budget is free")
  void parsesDefaults() {
    assertEquals(
        IntentBudget.FREE,
        IntentBudget.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }
}
