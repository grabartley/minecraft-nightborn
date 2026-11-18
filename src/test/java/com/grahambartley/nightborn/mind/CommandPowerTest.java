package com.grahambartley.nightborn.mind;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CommandPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":12,\"duration_ticks\":600}; false",
        "{\"range\":0,\"duration_ticks\":600}; true",
        "{\"duration_ticks\":600}; true",
        "{\"range\":12,\"duration_ticks\":0}; true"
      })
  @DisplayName("a command needs a reach and a time")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused,
        CommandPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
