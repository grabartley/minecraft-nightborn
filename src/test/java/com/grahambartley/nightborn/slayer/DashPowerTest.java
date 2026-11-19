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
class DashPowerTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"distance\":4,\"untouchable_ticks\":6,\"dodge_ticks\":5,\"stagger_ticks\":30} | 4 | 6 | 5 | 30",
        "{\"distance\":2.5,\"untouchable_ticks\":0,\"dodge_ticks\":0,\"stagger_ticks\":1} | 2.5 | 0 | 0 | 1"
      })
  @DisplayName("a dash reads its distance, its untouchable time, its dodge window and its stagger")
  void reads(
      final String json,
      final float distance,
      final int untouchable,
      final int dodge,
      final int stagger) {
    assertEquals(
        new DashPower(distance, untouchable, dodge, stagger),
        DashPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{\"distance\":9,\"untouchable_ticks\":6,\"dodge_ticks\":5,\"stagger_ticks\":30}",
        "{\"distance\":4,\"untouchable_ticks\":6,\"dodge_ticks\":5,\"stagger_ticks\":0}",
        "{\"distance\":4}"
      })
  @DisplayName("a dash out of range or missing a value is refused")
  void refuses(final String json) {
    assertTrue(DashPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }
}
