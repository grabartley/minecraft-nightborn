package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
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
    assertEquals(new SkillRank(cost, List.of(), bareHands, List.of(), List.of()), rank);
    assertEquals(SkillRank.costing(cost).cost(), rank.cost());
  }

  @ParameterizedTest(name = "{0} is refused")
  @CsvSource(
      delimiter = ';',
      value = {
        "{}",
        "{\"cost\": -1}",
        "{\"cost\": 101}",
        "{\"cost\": 1, \"bare_hand_damage\": 21}",
        "{\"cost\": 3, \"power_gifts\": [{\"blood_cut\": 1}]}"
      })
  @DisplayName("a rank with no cost or a value out of bounds is refused")
  void refuses(final String json) {
    assertEquals(
        true, SkillRank.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0} -> {1} gifts")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"cost\": 3, \"power_gifts\": [{\"branch\": \"nightborn:beast\", \"blood_cut\": 1, \"cooldown_cut\": 0.2}]}; 1",
        "{\"cost\": 3}; 0"
      })
  @DisplayName("a rank may carry gifts for its bloodline's powers")
  void gifts(final String json, final int count) {
    assertEquals(
        count,
        SkillRank.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .powerGifts()
            .size());
  }

  @ParameterizedTest(name = "{0} -> {1} trap gifts")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"cost\": 1, \"trap_gifts\": [{\"arm_cut\": 0.25, \"seen_within\": 6}]}; 1",
        "{\"cost\": 2, \"trap_gifts\": [{\"power\": \"nightborn:trapper/snare\", \"extra_damage\": 2}, {\"power\": \"nightborn:trapper/net_shot\", \"extra_damage\": 2}]}; 2",
        "{\"cost\": 3}; 0"
      })
  @DisplayName("a rank may carry gifts for its path's traps")
  void trapGifts(final String json, final int count) {
    assertEquals(
        count,
        SkillRank.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .trapGifts()
            .size());
  }
}
