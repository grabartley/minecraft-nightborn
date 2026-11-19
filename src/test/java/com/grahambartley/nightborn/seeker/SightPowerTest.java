package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SightPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":24,\"duration_ticks\":120,\"colour\":15129792}; false",
        "{\"range\":24,\"duration_ticks\":120}; true",
        "{\"range\":0,\"duration_ticks\":120,\"colour\":1}; true",
        "{\"range\":24,\"duration_ticks\":10,\"colour\":1}; true"
      })
  @DisplayName("true sight reads its reach, time and colour")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, SightPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
