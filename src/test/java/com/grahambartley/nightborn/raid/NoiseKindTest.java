package com.grahambartley.nightborn.raid;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NoiseKindTest {

  @ParameterizedTest(name = "loudness {0}, reach {1}: loads {2}")
  @CsvSource({
    "0, 16, true",
    "0.5, 1, true",
    "1000, 64, true",
    "-0.5, 16, false",
    "1001, 16, false",
    "2, 0, false",
    "2, 65, false"
  })
  @DisplayName("a sound is between silent and 1000 loud and carries 1 to 64 blocks")
  void bounds(final String loudness, final int reach, final boolean loads) {
    final String json = "{\"loudness\": " + loudness + ", \"reach\": " + reach + "}";
    assertEquals(
        loads, NoiseKind.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "loudness {0}: silent {1}")
  @CsvSource({"0, true", "0.5, false", "6, false"})
  @DisplayName("a sound with no loudness is silent and is never carried")
  void silent(final float loudness, final boolean silent) {
    assertEquals(silent, new NoiseKind(loudness, 16).silent());
  }
}
