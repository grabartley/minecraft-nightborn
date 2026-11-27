package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageStuckTest {

  @Test
  @DisplayName("a stage with no stuck habits waits two seconds and tries three times")
  void defaults() {
    assertEquals(
        StageStuck.PLAIN,
        StageStuck.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"ticks\": 1} | true",
        "{\"ticks\": 0} | false",
        "{\"ticks\": 201} | false",
        "{\"progress\": 0.01} | false",
        "{\"progress\": 1.0} | true",
        "{\"travel\": 0.1} | false",
        "{\"reach\": -1.0} | false",
        "{\"reach\": 0.0} | true",
        "{\"hold_waits\": 0} | false",
        "{\"step_back_waits\": 21} | false",
        "{\"tries\": 1} | true",
        "{\"forgo_waits\": 101} | false",
        "{\"step_back_range\": 33} | false"
      })
  @DisplayName("every stuck habit stays within sensible bounds")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageStuck.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"ticks\": 15, \"tries\": 2} | 15 | 2 | 0.5",
        "{\"progress\": 1.0} | 40 | 3 | 1.0"
      })
  @DisplayName("a stage reads its own stuck habits and keeps the rest from the defaults")
  void reads(final String json, final int ticks, final int tries, final double progress) {
    final StageStuck stuck =
        StageStuck.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(ticks, stuck.ticks());
    assertEquals(tries, stuck.tries());
    assertEquals(progress, stuck.progress());
  }
}
