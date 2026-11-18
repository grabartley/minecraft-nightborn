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
class PowerMindsTest {

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"glamour\":{\"duration_ticks\":600}} | glamour",
        "{\"command\":{\"range\":12,\"duration_ticks\":600}} | command",
        "{\"mass_gaze\":{\"radius\":12,\"duration_ticks\":200,\"player_duration_ticks\":60,\"player_slowness\":2}} | mass_gaze",
        "{} | none"
      })
  @DisplayName(
      "the group reads each of its blocks from the power's own fields, and none when absent")
  void reads(final String json, final String block) {
    final PowerMinds group =
        PowerMinds.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(block.equals("glamour"), group.glamour().isPresent());
    assertEquals(block.equals("command"), group.command().isPresent());
    assertEquals(block.equals("mass_gaze"), group.massGaze().isPresent());
  }
}
