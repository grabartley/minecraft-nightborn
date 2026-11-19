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
class GuardPowerTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"guard_ticks\":10,\"reach\":2.5,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40} | 10 | 2.5 | 10 | 3 | 40",
        "{\"guard_ticks\":1,\"reach\":0.5,\"charge_ticks\":1,\"push\":0,\"stagger_ticks\":1} | 1 | 0.5 | 1 | 0 | 1"
      })
  @DisplayName(
      "a guard reads how long it is held, how near it meets a charge, the push and the stagger")
  void reads(
      final String json,
      final int guard,
      final float reach,
      final int charge,
      final float push,
      final int stagger) {
    assertEquals(
        new GuardPower(guard, reach, charge, push, stagger),
        GuardPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"guard_ticks\":0,\"reach\":2.5,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40}",
        "{\"guard_ticks\":10,\"reach\":9,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40}",
        "{\"guard_ticks\":10}"
      })
  @DisplayName("a guard out of range or missing a value is refused")
  void refuses(final String json) {
    assertTrue(GuardPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
