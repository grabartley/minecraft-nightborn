package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RebukePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":8,\"arc_degrees\":90,\"push\":1.5,\"silence_ticks\":40}; false",
        "{\"range\":8,\"arc_degrees\":90,\"push\":1.5}; true",
        "{\"range\":0,\"arc_degrees\":90,\"push\":1.5,\"silence_ticks\":40}; true",
        "{\"range\":8,\"arc_degrees\":400,\"push\":1.5,\"silence_ticks\":40}; true"
      })
  @DisplayName("a rebuke needs its reach, arc, push and how long it silences")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, RebukePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
