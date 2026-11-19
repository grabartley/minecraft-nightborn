package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KindlePowerTest {

  @ParameterizedTest(name = "now {0}, lasts {1} -> burns until {2}")
  @CsvSource({"0, 1800, 1800", "1000, 1800, 2800", "500, 20, 520"})
  @DisplayName("blood fire burns from the moment it is lit for its whole time")
  void flame(final long now, final int ticks, final long until) {
    final KindlePower power = new KindlePower(4.0F, ticks, 3.0F, 100);
    assertEquals(new Flame(until, 3.0F, 100), power.flame(now));
  }

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"health_cost\":4,\"duration_ticks\":1800,\"damage\":3,\"burn_ticks\":100}; false",
        "{\"duration_ticks\":1800,\"damage\":3,\"burn_ticks\":100}; true",
        "{\"health_cost\":4,\"duration_ticks\":1800,\"damage\":30,\"burn_ticks\":100}; true"
      })
  @DisplayName("blood fire needs its cost in health, its time, its damage and its burn")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, KindlePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
