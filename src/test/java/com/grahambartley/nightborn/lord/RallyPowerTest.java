package com.grahambartley.nightborn.lord;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RallyPowerTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"range\":32} | true",
        "{\"range\":1} | true",
        "{\"range\":0} | false",
        "{\"range\":49} | false",
        "{} | false"
      })
  @DisplayName("a rally reads how far the call carries, from 1 to 48 blocks")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, RallyPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
