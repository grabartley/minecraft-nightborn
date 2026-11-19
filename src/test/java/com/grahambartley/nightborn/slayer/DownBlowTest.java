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
class DownBlowTest {

  @ParameterizedTest(name = "{0}: below {1}, up to {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"below\":0.4,\"up_to\":\"nightborn:elder\"} | 0.4 | nightborn:elder",
        "{\"below\":0.3,\"up_to\":\"nightborn:adult\"} | 0.3 | nightborn:adult"
      })
  @DisplayName("a downing blow reads the health share and the oldest stage it can fell")
  void reads(final String json, final float below, final String upTo) {
    final DownBlow blow =
        DownBlow.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(below, blow.below());
    assertEquals(upTo, blow.upTo().toString());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"{\"below\":1.5,\"up_to\":\"nightborn:elder\"}", "{\"below\":0.3}", "{}"})
  @DisplayName("a downing blow out of range or missing its stage is refused")
  void refuses(final String json) {
    assertTrue(DownBlow.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
