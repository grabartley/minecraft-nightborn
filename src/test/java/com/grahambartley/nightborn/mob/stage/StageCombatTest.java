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
        "{\"power_dodge_chance\": 100} | true",
        "{\"power_dodge_chance\": 101} | false",
        "{\"power_dodge_chance\": -1} | false",
        "{\"retreat_health\": 101} | false",
        "{\"stuck_ticks\": 0} | false",
        "{\"stuck_ticks\": 1} | true",
        "{\"stuck_ticks\": 200} | true",
        "{\"stuck_ticks\": 201} | false"
      })
  @DisplayName(
      "blood floors stay within a blood bar, chances within 0 to 100 and waits within ten"
          + " seconds")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, StageCombat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"0", "35", "100"})
  @DisplayName("a stage reads how often it dodges with a power")
  void powerDodgeChance(final int chance) {
    assertEquals(
        chance,
        StageCombat.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"power_dodge_chance\": " + chance + "}"))
            .getOrThrow()
            .powerDodgeChance());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"1", "25", "200"})
  @DisplayName("a stage reads how long it stays stuck before it tries something else")
  void stuckTicks(final int ticks) {
    assertEquals(
        ticks,
        StageCombat.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"stuck_ticks\": " + ticks + "}"))
            .getOrThrow()
            .stuckTicks());
  }
}
