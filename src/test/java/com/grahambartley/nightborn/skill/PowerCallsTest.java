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
class PowerCallsTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"fog\":{\"radius\":4,\"duration_ticks\":160,\"effects\":[]}} | fog",
        "{\"summon\":{\"entity\":\"minecraft:bat\",\"count\":2}} | summon",
        "{\"circle\":{\"radius\":4,\"duration_ticks\":600}} | circle",
        "{} | none"
      })
  @DisplayName(
      "the group reads each of its blocks from the power's own fields, and none when absent")
  void reads(final String json, final String block) {
    final PowerCalls group =
        PowerCalls.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(block.equals("fog"), group.fog().isPresent());
    assertEquals(block.equals("summon"), group.summon().isPresent());
    assertEquals(block.equals("circle"), group.circle().isPresent());
  }
}
