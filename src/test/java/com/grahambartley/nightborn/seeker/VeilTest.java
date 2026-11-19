package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VeilTest {

  @ParameterizedTest(name = "{0} -> {1}")
  @CsvSource({"shadow_veil, SHADOW_VEIL", "mist, MIST", "glamour, GLAMOUR", "vanish, VANISH"})
  @DisplayName("each way a vampire hides is named in data")
  void reads(final String name, final Veil veil) {
    assertEquals(
        veil,
        Veil.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("\"" + name + "\""))
            .getOrThrow());
    assertEquals(name, veil.asString());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"smoke", "SHADOW_VEIL", "shadow-veil"})
  @DisplayName("an unknown veil is refused")
  void refuses(final String name) {
    assertEquals(
        true,
        Veil.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("\"" + name + "\"")).isError());
  }
}
