package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScenePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":8,\"kill_radius\":4,\"fresh_ticks\":24000}; false",
        "{\"range\":8,\"kill_radius\":4}; true",
        "{\"range\":0,\"kill_radius\":4,\"fresh_ticks\":24000}; true",
        "{\"range\":8,\"kill_radius\":4,\"fresh_ticks\":72001}; true"
      })
  @DisplayName("a scene is read from close by, with a kill's radius and how long signs stay fresh")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, ScenePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
