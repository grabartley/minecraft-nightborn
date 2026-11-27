package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewTintTest {

  @Test
  @DisplayName("a tint reads its colour and how deep it lies at the middle and the edge")
  void reads() {
    assertEquals(
        new FormViewTint(0x9E1418, 0.2F, 0.6F),
        FormViewTint.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"color\":\"#9E1418\",\"centre\":0.2,\"edge\":0.6}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"color\":\"#9E1418\",\"centre\":1.2,\"edge\":0.6}",
        "{\"color\":\"#9E1418\",\"centre\":0.2,\"edge\":-0.1}",
        "{\"color\":\"9E1418\",\"centre\":0.2,\"edge\":0.6}",
        "{\"centre\":0.2,\"edge\":0.6}"
      })
  @DisplayName("a tint with a bad colour or amount is refused")
  void refusesBad(final String json) {
    assertFalse(
        FormViewTint.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
