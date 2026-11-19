package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class FieldPowerTest {

  @ParameterizedTest(name = "{0}: refused {1}, bars {2}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":10,\"duration_ticks\":400,\"bars\":[\"nightborn:core/vanish\",\"nightborn:beast/bat_form\"],\"look\":\"nightborn:killing_ground\"}; false; 2",
        "{\"radius\":10,\"duration_ticks\":400,\"look\":\"nightborn:killing_ground\"}; false; 0",
        "{\"radius\":0,\"duration_ticks\":400,\"look\":\"nightborn:killing_ground\"}; true; 0",
        "{\"radius\":10,\"duration_ticks\":10,\"look\":\"nightborn:killing_ground\"}; true; 0",
        "{\"radius\":10,\"duration_ticks\":400}; true; 0"
      })
  @DisplayName("a marked ground needs a radius, a life and a look, and may bar powers")
  void reads(final String json, final boolean refused, final int bars) {
    final var read = FieldPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    assertEquals(refused, read.isError());
    if (!refused) {
      assertEquals(bars, read.getOrThrow().bars().size());
    }
  }
}
