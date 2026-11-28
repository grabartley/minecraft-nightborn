package com.grahambartley.nightborn.rank;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RankGiftsTest {

  @ParameterizedTest(name = "hunter {0}, rank {1} -> {2} health, {3} damage")
  @CsvSource({
    "true, 0, 0.0, 0.0",
    "true, 1, 4.0, 0.5",
    "true, 2, 8.0, 1.0",
    "true, 3, 12.0, 1.5",
    "true, 9, 12.0, 1.5",
    "true, -1, 0.0, 0.0",
    "false, 3, 0.0, 0.0"
  })
  @DisplayName("a hunter has the gifts its rank's data gives, and no one else has any")
  void ofTheRank(final boolean hunter, final int rank, final double health, final float damage) {
    final RankGifts gifts = RankGifts.of(RankFixtures.LADDER, hunter, rank);
    assertEquals(health, gifts.health());
    assertEquals(damage, gifts.against(true));
  }

  @ParameterizedTest(name = "rank {0}")
  @CsvSource({"0", "1", "3"})
  @DisplayName("a rank adds nothing against anyone but a vampire")
  void nothingAgainstOthers(final int rank) {
    assertEquals(0.0F, RankGifts.of(RankFixtures.LADDER, true, rank).against(false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"health\": 12, \"vampire_damage\": 1.5} | true",
        "{\"health\": -1} | false",
        "{\"vampire_damage\": -0.5} | false",
        "{\"health\": 1000.5} | false"
      })
  @DisplayName("a rank's gifts only add, within bounds")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, RankGifts.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
