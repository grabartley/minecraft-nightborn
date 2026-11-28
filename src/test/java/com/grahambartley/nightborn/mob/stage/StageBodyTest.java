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
        "{\"health\": 24, \"speed\": 0.2, \"damage\": 3.5} | true",
        "{\"health\": -19} | true",
        "{\"health\": -19.5} | false",
        "{\"health\": 1000.5} | false",
        "{\"speed\": -0.9} | true",
        "{\"speed\": -1.0} | false",
        "{\"damage\": 100} | true",
        "{\"damage\": 100.5} | false"
      })
  @DisplayName("a stage's body gives health and damage in points and speed as a share, in bounds")
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

  @ParameterizedTest(name = "{0} health under x{1} -> {2}")
  @CsvSource({"4, 1.0, 2", "12, 1.0, 6", "4, 0.0, 4", "4, -3.0, 4", "0, 1.0, 0"})
  @DisplayName("health is shared out so a multiplied body gains exactly the stage's health")
  void healthAllowsForAMultiplier(
      final double health, final double multiplier, final double shared) {
    assertEquals(
        new StageBody(shared, 0.2, 3.0),
        new StageBody(health, 0.2, 3.0).underHealthMultiplier(multiplier));
  }
}
