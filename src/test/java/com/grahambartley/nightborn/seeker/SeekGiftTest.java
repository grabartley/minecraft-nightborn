package com.grahambartley.nightborn.seeker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class SeekGiftTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{}; 0; 0; false; 0; 0; 0",
        "{\"track_range\":32,\"track_ticks\":100}; 32; 100; false; 0; 0; 0",
        "{\"track_range\":32,\"track_ticks\":100,\"blood_trails\":true}; 32; 100; true; 0; 0; 0",
        "{\"sense_range\":48}; 0; 0; false; 48; 0; 0",
        "{\"listen_range\":12}; 0; 0; false; 0; 12; 0",
        "{\"allied_share\":0.1}; 0; 0; false; 0; 0; 0.1"
      })
  @DisplayName("a gift reads what it gives, with nothing given by default")
  void reads(
      final String json,
      final int range,
      final int ticks,
      final boolean trails,
      final float sense,
      final float listen,
      final double allied) {
    assertEquals(
        new SeekGift(range, ticks, trails, sense, listen, allied),
        SeekGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"track_range\":-1}",
        "{\"track_ticks\":9999}",
        "{\"sense_range\":65}",
        "{\"listen_range\":-2}",
        "{\"allied_share\":1.5}"
      })
  @DisplayName("a gift's values stay in bounds")
  void refuses(final String json) {
    assertEquals(
        true, SeekGift.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
