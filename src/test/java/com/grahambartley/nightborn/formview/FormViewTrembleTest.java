package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormViewTrembleTest {

  @Test
  @DisplayName("a tremble reads its size, where it starts and how fast it moves")
  void reads() {
    assertEquals(
        new FormViewTremble(0.006F, 0.45F, 9.0F),
        FormViewTremble.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"amount\":0.006,\"inner\":0.45,\"speed\":9}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"amount\":0.2,\"inner\":0.45,\"speed\":9}",
        "{\"amount\":0.006,\"inner\":1.5,\"speed\":9}",
        "{\"amount\":0.006,\"inner\":0.45,\"speed\":-1}",
        "{\"amount\":0.006,\"inner\":0.45}"
      })
  @DisplayName("a tremble big enough to shake the camera, or missing a number, is refused")
  void refusesBad(final String json) {
    assertFalse(
        FormViewTremble.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
