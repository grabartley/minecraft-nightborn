package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrackPowerTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"range\":64,\"duration_ticks\":200}|64|200",
        "{\"range\":1,\"duration_ticks\":1}|1|1",
        "{\"range\":256,\"duration_ticks\":6000}|256|6000",
        "{\"range\":0,\"duration_ticks\":200}|-1|-1",
        "{\"range\":257,\"duration_ticks\":200}|-1|-1",
        "{\"range\":64,\"duration_ticks\":0}|-1|-1",
        "{\"range\":64}|-1|-1"
      })
  @DisplayName("a tracking power reads its range and time, and rejects values out of bounds")
  void parses(final String json, final int range, final int ticks) {
    final Optional<TrackPower> power =
        TrackPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).result();
    assertEquals(range < 0 ? Optional.empty() : Optional.of(new TrackPower(range, ticks)), power);
  }
}
