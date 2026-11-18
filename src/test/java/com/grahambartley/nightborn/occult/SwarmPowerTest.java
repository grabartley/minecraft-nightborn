package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SwarmPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":12,\"duration_ticks\":300,\"damage\":1,\"every_ticks\":20,\"recent_ticks\":200}; false",
        "{\"radius\":12,\"duration_ticks\":300,\"damage\":1,\"every_ticks\":0,\"recent_ticks\":200}; true",
        "{\"radius\":12,\"duration_ticks\":300,\"every_ticks\":20,\"recent_ticks\":200}; true"
      })
  @DisplayName("a swarm needs a radius, a time, damage, a beat and a memory")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, SwarmPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
