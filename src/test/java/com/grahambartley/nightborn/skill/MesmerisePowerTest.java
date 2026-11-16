package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MesmerisePowerTest {

  @Test
  @DisplayName("a mesmerise power reads its range, stare, hold times and slowness")
  void parses() {
    final MesmerisePower power =
        MesmerisePower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"range\":12,\"stare_ticks\":20,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}"))
            .getOrThrow();
    assertEquals(new MesmerisePower(12.0F, 20, 200, 60, 2), power);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"range\":0,\"stare_ticks\":20,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}",
        "{\"range\":12,\"stare_ticks\":0,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}",
        "{\"range\":12,\"stare_ticks\":20,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":9}",
        "{\"range\":12,\"stare_ticks\":20,\"duration_ticks\":200}"
      })
  @DisplayName("a mesmerise power out of bounds or missing a field is rejected")
  void rejects(final String json) {
    assertTrue(
        MesmerisePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "player {0} -> {1} ticks")
  @CsvSource({"true, 60", "false, 200"})
  @DisplayName("a player is held for a shorter time than a mob")
  void durationFor(final boolean player, final int expected) {
    assertEquals(expected, new MesmerisePower(12.0F, 20, 200, 60, 2).durationFor(player));
  }
}
