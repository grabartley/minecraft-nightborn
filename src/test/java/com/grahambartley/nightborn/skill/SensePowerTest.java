package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SensePowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":24,\"duration_ticks\":160}; false",
        "{\"range\":0,\"duration_ticks\":160}; true",
        "{\"range\":24}; true",
        "{\"range\":24,\"duration_ticks\":0}; true"
      })
  @DisplayName("a sense needs a range and a time")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, SensePower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
