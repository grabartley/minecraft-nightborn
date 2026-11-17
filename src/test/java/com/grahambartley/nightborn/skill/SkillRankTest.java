package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SkillRankTest {

  @ParameterizedTest(name = "{0} -> cost {1}, bare hands {2}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"cost\": 2}; 2; 0",
        "{\"cost\": 1, \"bare_hand_damage\": 2.0}; 1; 2",
        "{\"cost\": 0}; 0; 0"
      })
  @DisplayName("a rank reads its cost and what it gives, with nothing given by default")
  void reads(final String json, final int cost, final double bareHands) {
    final SkillRank rank =
        SkillRank.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(new SkillRank(cost, java.util.List.of(), bareHands), rank);
    assertEquals(SkillRank.costing(cost).cost(), rank.cost());
  }

  @ParameterizedTest(name = "{0} is refused")
  @CsvSource(
      delimiter = ';',
      value = {
        "{}",
        "{\"cost\": -1}",
        "{\"cost\": 101}",
        "{\"cost\": 1, \"bare_hand_damage\": 21}"
      })
  @DisplayName("a rank with no cost or a value out of bounds is refused")
  void refuses(final String json) {
    assertEquals(
        true, SkillRank.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
