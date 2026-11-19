package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlessingPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":8,\"duration_ticks\":1200}; false",
        "{\"range\":8}; true",
        "{\"duration_ticks\":1200}; true",
        "{\"range\":8,\"duration_ticks\":5}; true"
      })
  @DisplayName("a blessing needs the reach to an ally and how long it lasts")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused,
        BlessingPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
