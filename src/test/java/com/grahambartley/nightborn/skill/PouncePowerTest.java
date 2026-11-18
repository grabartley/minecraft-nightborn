package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PouncePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":10,\"pin_ticks\":40,\"player_pin_ticks\":20}; false",
        "{\"range\":1,\"pin_ticks\":40,\"player_pin_ticks\":20}; true",
        "{\"range\":10,\"pin_ticks\":0,\"player_pin_ticks\":20}; true",
        "{\"range\":10,\"pin_ticks\":40}; true"
      })
  @DisplayName("a pounce needs a reach and how long it pins a creature and a player")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, PouncePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "player {0}: pinned {1} ticks")
  @CsvSource({"true, 20", "false, 40"})
  @DisplayName("a player is pinned for less time than a creature")
  void pins(final boolean player, final int ticks) {
    assertEquals(ticks, new PouncePower(10.0F, 40, 20).pinFor(player));
  }
}
