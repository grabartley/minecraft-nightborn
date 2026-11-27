package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewWashTest {

  @Test
  @DisplayName("a wash reads its colour, strength, saturation, softness and drift")
  void reads() {
    assertEquals(
        new FormViewWash(0xC9CED3, 0.35F, 0.3F, 2.5F, 0.004F),
        FormViewWash.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"color\":\"#C9CED3\",\"amount\":0.35,\"saturation\":0.3,"
                        + "\"blur\":2.5,\"drift\":0.004}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"color\":\"#C9CED3\",\"amount\":1.5,\"saturation\":0.3,\"blur\":2.5,\"drift\":0.004}",
        "{\"color\":\"#C9CED3\",\"amount\":0.35,\"saturation\":0.3,\"blur\":20,\"drift\":0.004}",
        "{\"color\":\"#C9CED3\",\"amount\":0.35,\"saturation\":0.3,\"blur\":2.5,\"drift\":0.5}",
        "{\"amount\":0.35,\"saturation\":0.3,\"blur\":2.5,\"drift\":0.004}"
      })
  @DisplayName("a wash that would hide the way ahead, or is missing a part, is refused")
  void refusesBad(final String json) {
    assertFalse(
        FormViewWash.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
