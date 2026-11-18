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
class PowerMovesTest {

  @ParameterizedTest(name = "{0}: {1} moves")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | 0",
        "{\"leap\":{\"forward\":1.4,\"up\":0.55}} | 1",
        "{\"mist\":{\"duration_ticks\":160,\"harm\":2},\"pounce\":{\"range\":10,\"pin_ticks\":40,\"player_pin_ticks\":20}} | 2"
      })
  @DisplayName("the moves count every way a power shifts or moves its caster")
  void counts(final String json, final int count) {
    assertEquals(
        count,
        PowerMoves.CODEC
            .codec()
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .count());
  }
}
