package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class PowerSelfTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"sense\":{\"range\":24,\"duration_ticks\":160}} | sense",
        "{\"frees\":{\"skills\":[],\"duration_ticks\":20}} | frees",
        "{\"heal\":{\"health\":6,\"ticks\":60}} | heal",
        "{} | none"
      })
  @DisplayName(
      "the group reads each of its blocks from the power's own fields, and none when absent")
  void reads(final String json, final String block) {
    final PowerSelf group =
        PowerSelf.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(block.equals("sense"), group.sense().isPresent());
    assertEquals(block.equals("frees"), group.frees().isPresent());
    assertEquals(block.equals("heal"), group.heal().isPresent());
  }
}
