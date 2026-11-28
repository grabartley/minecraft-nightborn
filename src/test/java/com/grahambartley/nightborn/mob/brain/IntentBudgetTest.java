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
    "8, 12, 14, 3, false",
    "10, 0, 0, 0, true",
    "10, 2, 3, 0, true",
    "10, 4, 3, 0, false"
  })
  @DisplayName(
      "a cast must leave the higher of the mob's floor and the intent's reserve, and a free one"
          + " answers to the mob's floor alone")
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

  @ParameterizedTest(name = "thrift {0}, blood {1}, cooldown {2}, own health {3}: keeps {4}")
  @CsvSource({
    "0, 6, 6000, 1, 1",
    "0.5, 0, 0, 1, 1",
    "0.5, 6, 6000, 1, 0.5",
    "0.5, 6, 6000, 0.2, 0.9",
    "0.5, 6, 6000, 0, 1",
    "0.5, 2, 300, 1, 0.875",
    "1, 10, 0, 1, 0",
    "1, 20, 12000, 0.5, 0.5"
  })
  @DisplayName(
      "a healthy mob keeps a costly power back by its thrift, and spends freely as it loses")
  void kept(
      final float thrift,
      final int blood,
      final int cooldown,
      final float health,
      final double kept) {
    assertEquals(
        kept, new IntentBudget(0, 0, 0.0F, thrift, 0.0F).kept(blood, cooldown, health), 1.0E-6);
  }

  @ParameterizedTest(name = "spread {0}, score {1}, best {2}: close {3}")
  @CsvSource({"0, 2, 2, true", "0, 1.9, 2, false", "0.1, 1.8, 2, true", "0.1, 1.79, 2, false"})
  @DisplayName("a score close enough to the best one is a fair choice")
  void close(final float spread, final double score, final double best, final boolean close) {
    assertEquals(close, new IntentBudget(0, 0, 0.0F, 0.0F, spread).close(score, best));
  }

  @Test
  @DisplayName("thrift and spread are read from data")
  void parsesThriftAndSpread() {
    assertEquals(
        new IntentBudget(0, 0, 0.0F, 0.5F, 0.1F),
        IntentBudget.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"thrift\":0.5,\"spread\":0.1}"))
            .getOrThrow());
  }

  @Test
  @DisplayName("an empty budget is free")
  void parsesDefaults() {
    assertEquals(
        IntentBudget.FREE,
        IntentBudget.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "scale {0}, fight think {1}: {2}")
  @CsvSource({"1, 10, 10", "4, 10, 40", "4, 25, 100", "20, 5, 100"})
  @DisplayName("an intent outside a fight thinks its scale times slower than a fight")
  void thinkTicks(final int scale, final int fight, final int ticks) {
    assertEquals(ticks, new IntentBudget(0, 0, 0.0F, 0.0F, 0.0F, scale).thinkTicks(fight));
  }

  @Test
  @DisplayName("the think scale is read from data")
  void parsesThinkScale() {
    assertEquals(
        4,
        IntentBudget.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"think_scale\":4}"))
            .getOrThrow()
            .thinkScale());
  }
}
