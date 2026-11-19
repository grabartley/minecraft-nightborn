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
class PowerFightTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | 0",
        "{\"riposte\":{\"guard_ticks\":10,\"reach\":2.5,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40}} | 1",
        "{\"stake_throw\":{\"range\":20,\"speed\":2,\"damage\":4,\"stagger_ticks\":20,\"damage_type\":\"nightborn:thrown_stake\"}} | 1",
        "{\"quick_shot\":{\"speed\":3.15,\"divergence\":1}} | 1"
      })
  @DisplayName("a power may guard, throw a stake or fire a crossbow")
  void reads(final String json, final int count) {
    final PowerFight fight =
        PowerFight.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(count, fight.count());
    assertEquals(count == 0, fight.equals(PowerFight.NONE));
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"riposte\":{\"guard_ticks\":10,\"reach\":2.5,\"charge_ticks\":10,\"push\":3,\"stagger_ticks\":40},\"quick_shot\":{\"speed\":3.15,\"divergence\":1}}"
      })
  @DisplayName("a power does one of these only")
  void oneOnly(final String json) {
    assertTrue(
        PowerFight.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
