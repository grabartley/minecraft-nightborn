package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FamiliarPowerTest {

  static final String VALID =
      "{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9}";

  @Test
  @DisplayName("a familiar power reads its lifetime, body, scouting and carrying")
  void parses() {
    assertEquals(
        new FamiliarPower(1200, 10.0F, 3.0F, 24.0F, 200, 8.0F, 200, 9),
        FamiliarPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(VALID)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"lifetime_ticks\":0,\"health\":10,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9}",
        "{\"lifetime_ticks\":1200,\"health\":0,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9}",
        "{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3,\"scout_range\":100,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":9}",
        "{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3,\"scout_range\":24,\"scout_ticks\":200,\"mark_radius\":8,\"mark_ticks\":200,\"carry_slots\":28}",
        "{\"lifetime_ticks\":1200,\"health\":10,\"damage\":3}"
      })
  @DisplayName("a familiar power out of bounds or missing a field is rejected")
  void rejects(final String json) {
    assertTrue(FamiliarPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
