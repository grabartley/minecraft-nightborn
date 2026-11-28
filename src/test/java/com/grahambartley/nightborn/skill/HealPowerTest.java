package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HealPowerTest {

  @ParameterizedTest(name = "{0} health over {1} ticks: {2} a tick")
  @CsvSource({"6, 60, 0.1", "6, 1, 6", "0, 40, 0"})
  @DisplayName("a heal is spread evenly over its ticks")
  void spreads(final float health, final int ticks, final float perTick) {
    assertEquals(new Mend(perTick, ticks), new HealPower(health, ticks, false).mend());
  }

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"health\": 6.0, \"ticks\": 60}; false",
        "{\"health\": 6.0, \"ticks\": 0}; true",
        "{\"health\": -1.0, \"ticks\": 60}; true",
        "{\"health\": 6.0}; true"
      })
  @DisplayName("a heal needs an amount and a time of at least one tick")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, HealPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "{0}: closes wounds {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"health\": 6.0, \"ticks\": 60}; false",
        "{\"health\": 6.0, \"ticks\": 60, \"closes_wounds\": true}; true",
        "{\"health\": 6.0, \"ticks\": 60, \"closes_wounds\": false}; false"
      })
  @DisplayName("a heal closes open wounds only when its data says so")
  void closesWounds(final String json, final boolean closes) {
    assertEquals(
        closes,
        HealPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .closesWounds());
  }
}
