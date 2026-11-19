package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class QuickShotPowerTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"speed\":3.15,\"divergence\":1} | 3.15 | 1",
        "{\"speed\":1.6,\"divergence\":0} | 1.6 | 0"
      })
  @DisplayName("a quick shot reads how fast and how true the bolt flies")
  void reads(final String json, final float speed, final float divergence) {
    assertEquals(
        new QuickShotPower(speed, divergence),
        QuickShotPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{\"speed\":0,\"divergence\":1}", "{\"speed\":3}", "{}"})
  @DisplayName("a quick shot out of range or missing a value is refused")
  void refuses(final String json) {
    assertTrue(
        QuickShotPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
