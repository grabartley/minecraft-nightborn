package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerSeekTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | 0",
        "{\"read_scene\":{\"range\":8,\"kill_radius\":4,\"fresh_ticks\":24000}} | 1",
        "{\"mark_quarry\":{\"range\":24,\"duration_ticks\":1200,\"damage_share\":0.15,\"colour\":11674146}} | 1",
        "{\"true_sight\":{\"range\":24,\"duration_ticks\":120,\"colour\":15129792}} | 1",
        "{\"revelation\":{\"range\":48,\"share_range\":16,\"duration_ticks\":400,\"colour\":13938487}} | 1"
      })
  @DisplayName("a power may read a scene, mark a quarry, see true or reveal")
  void reads(final String json, final int count) {
    final PowerSeek seek =
        PowerSeek.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(count, seek.count());
    assertEquals(count == 0, seek.equals(PowerSeek.NONE));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"true_sight\":{\"range\":24,\"duration_ticks\":120,\"colour\":1},\"revelation\":{\"range\":48,\"share_range\":16,\"duration_ticks\":400,\"colour\":1}}",
        "{\"read_scene\":{\"range\":8,\"kill_radius\":4,\"fresh_ticks\":24000},\"mark_quarry\":{\"range\":24,\"duration_ticks\":1200,\"colour\":1}}"
      })
  @DisplayName("a power does one of these only")
  void oneOnly(final String json) {
    assertTrue(
        PowerSeek.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
