package com.grahambartley.nightborn.mob.stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StageCombatTest {

  @Test
  @DisplayName("a stage with no fighting habits fights like a plain vampire")
  void defaults() {
    assertEquals(
        StageCombat.PLAIN,
        StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blood_floor\": 20} | true",
        "{\"blood_floor\": 21} | false",
        "{\"think_ticks\": 0} | false",
        "{\"think_ticks\": 200} | true",
        "{\"leap_chance\": 101} | false",
        "{\"dash_chance\": -1} | false",
        "{\"dodge_chance\": 100} | true",
        "{\"retreat_health\": 101} | false"
      })
  @DisplayName("blood floors stay within a blood bar and chances within 0 to 100")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
