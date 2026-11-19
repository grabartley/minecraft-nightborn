package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarkPowerTest {

  @ParameterizedTest(name = "{0} -> share {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":24,\"duration_ticks\":1200,\"damage_share\":0.15,\"colour\":11674146}; 0.15",
        "{\"range\":24,\"duration_ticks\":1200,\"colour\":11674146}; 0"
      })
  @DisplayName("a mark reads its reach, time, colour and the harm it adds, none by default")
  void reads(final String json, final double share) {
    assertEquals(
        share,
        MarkPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .damageShare());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":24,\"duration_ticks\":1200}",
        "{\"range\":24,\"duration_ticks\":1200,\"damage_share\":2,\"colour\":1}",
        "{\"range\":24,\"duration_ticks\":1200,\"colour\":16777216}",
        "{\"range\":24,\"duration_ticks\":1200,\"colour\":1,\"hidden_by\":[\"smoke\"]}"
      })
  @DisplayName("a mark with no colour or a value out of bounds is refused")
  void refuses(final String json) {
    assertEquals(
        true, MarkPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
