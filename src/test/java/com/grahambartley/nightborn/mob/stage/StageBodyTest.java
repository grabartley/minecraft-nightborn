package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageBodyTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"health\": 1.5, \"speed\": 0.2, \"damage\": 0.9} | true",
        "{\"health\": -0.9} | true",
        "{\"health\": -1.0} | false",
        "{\"damage\": 10.1} | false"
      })
  @DisplayName("each part of a stage's body scales between -0.9 and 10 times the base")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageBody.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{}", "{\"health\": 0, \"speed\": 0, \"damage\": 0}"})
  @DisplayName("a body that names nothing leaves the vampire as it is")
  void defaultsToSame(final String json) {
    assertEquals(
        StageBody.SAME,
        StageBody.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }
}
