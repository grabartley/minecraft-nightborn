package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MassGazePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":12,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}; false",
        "{\"radius\":12,\"duration_ticks\":200,\"player_duration_ticks\":60}; true",
        "{\"radius\":0,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}; true",
        "{\"radius\":12,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":9}; true"
      })
  @DisplayName("a mass gaze needs a radius, times and a slowness")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused,
        MassGazePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "player {0}: held {1} ticks")
  @CsvSource({"true, 60", "false, 200"})
  @DisplayName("a player is held for less time than a creature")
  void holds(final boolean player, final int ticks) {
    assertEquals(ticks, new MassGazePower(12.0F, 200, 60, 2).durationFor(player));
  }
}
