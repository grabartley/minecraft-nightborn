package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RevealPowerTest {

  @ParameterizedTest(name = "{0} -> {1} bars")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":48,\"share_range\":16,\"duration_ticks\":400,\"colour\":1}; 0",
        "{\"range\":48,\"share_range\":16,\"duration_ticks\":400,\"colour\":1,\"bars\":[\"nightborn:occult/shadow_veil\",\"nightborn:occult/mist_form\",\"nightborn:noble/glamour\"]}; 3"
      })
  @DisplayName("a revelation reads the powers it bars, none by default")
  void reads(final String json, final int bars) {
    assertEquals(
        bars,
        RevealPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .bars()
            .size());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":48,\"duration_ticks\":400,\"colour\":1}",
        "{\"range\":200,\"share_range\":16,\"duration_ticks\":400,\"colour\":1}",
        "{\"range\":48,\"share_range\":65,\"duration_ticks\":400,\"colour\":1}"
      })
  @DisplayName("a revelation with no share range or a value out of bounds is refused")
  void refuses(final String json) {
    assertEquals(
        true, RevealPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
