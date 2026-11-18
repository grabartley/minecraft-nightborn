package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HexPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"range\":16,\"duration_ticks\":400,\"player_duration_ticks\":200,\"healing\":0.5}; false",
        "{\"range\":16,\"duration_ticks\":400,\"player_duration_ticks\":200,\"healing\":1.5}; true",
        "{\"range\":16,\"duration_ticks\":400,\"healing\":0.5}; true",
        "{\"range\":16,\"duration_ticks\":400,\"player_duration_ticks\":200,\"healing\":0.5,\"effects\":[{\"id\":\"minecraft:weakness\",\"duration\":400}]}; false"
      })
  @DisplayName("a hex needs a reach, times and a healing share no greater than whole")
  void reads(final String json, final boolean refused) {
    assertEquals(
        refused, HexPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isError());
  }

  @ParameterizedTest(name = "player {0}: cursed {1} ticks")
  @CsvSource({"true, 200", "false, 400"})
  @DisplayName("a player is cursed for less time than a creature")
  void lasts(final boolean player, final int ticks) {
    assertEquals(ticks, new HexPower(16.0F, 400, 200, 0.5F, List.of()).durationFor(player));
  }
}
