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
class StakeThrowPowerTest {

  @ParameterizedTest(name = "{0}: flies {5} ticks")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"range\":20,\"speed\":2,\"damage\":4,\"stagger_ticks\":20,\"damage_type\":\"nightborn:thrown_stake\"} | 20 | 2 | 4 | 20 | 10",
        "{\"range\":10,\"speed\":3,\"damage\":0,\"stagger_ticks\":0,\"damage_type\":\"nightborn:thrown_stake\"} | 10 | 3 | 0 | 0 | 4"
      })
  @DisplayName(
      "a stake throw reads its reach, speed, damage and stagger, and flies until its reach")
  void reads(
      final String json,
      final float range,
      final float speed,
      final float damage,
      final int stagger,
      final int flight) {
    final StakeThrowPower power =
        StakeThrowPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(range, power.range());
    assertEquals(speed, power.speed());
    assertEquals(damage, power.damage());
    assertEquals(stagger, power.staggerTicks());
    assertEquals("nightborn:thrown_stake", power.damageType().getValue().toString());
    assertEquals(flight, power.flightTicks());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"range\":20,\"speed\":2,\"damage\":4,\"stagger_ticks\":20}",
        "{\"range\":0,\"speed\":2,\"damage\":4,\"stagger_ticks\":20,\"damage_type\":\"nightborn:thrown_stake\"}",
        "{\"range\":20,\"speed\":9,\"damage\":4,\"stagger_ticks\":20,\"damage_type\":\"nightborn:thrown_stake\"}"
      })
  @DisplayName("a stake throw out of range or without its damage type is refused")
  void refuses(final String json) {
    assertTrue(
        StakeThrowPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
