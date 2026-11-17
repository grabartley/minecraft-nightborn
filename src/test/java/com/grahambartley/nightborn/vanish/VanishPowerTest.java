package com.grahambartley.nightborn.vanish;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VanishPowerTest {

  @Test
  @DisplayName("a vanish reads how far it goes and how long the vampire is gone")
  void parses() {
    assertEquals(
        new VanishPower(4.0F, 5),
        VanishPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"distance\":4,\"gone_ticks\":5}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"distance\":1.9,\"gone_ticks\":5}",
        "{\"distance\":8.1,\"gone_ticks\":5}",
        "{\"distance\":4,\"gone_ticks\":0}",
        "{\"distance\":4,\"gone_ticks\":21}",
        "{\"distance\":4}",
        "{\"gone_ticks\":5}"
      })
  @DisplayName("a vanish out of bounds or missing a field is rejected")
  void rejects(final String json) {
    assertTrue(VanishPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
