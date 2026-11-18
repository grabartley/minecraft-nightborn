package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MistPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"duration_ticks\":160,\"harm\":2}; false",
        "{\"duration_ticks\":160,\"harm\":0.5}; true",
        "{\"harm\":2}; true"
      })
  @DisplayName("mist needs a time and harm of at least whole")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, MistPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
