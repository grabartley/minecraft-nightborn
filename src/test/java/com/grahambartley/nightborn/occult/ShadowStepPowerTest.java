package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ShadowStepPowerTest {

  @Test
  @DisplayName("a shadow step reads how far it reaches and how far behind a target it lands")
  void parses() {
    assertEquals(
        new ShadowStepPower(16.0F, 1.5F),
        ShadowStepPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"range\":16,\"behind\":1.5}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"range\":1,\"behind\":1}",
        "{\"range\":64,\"behind\":1}",
        "{\"range\":16,\"behind\":9}",
        "{\"range\":16}"
      })
  @DisplayName("a shadow step out of bounds or missing a field is rejected")
  void rejects(final String json) {
    assertTrue(
        ShadowStepPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
