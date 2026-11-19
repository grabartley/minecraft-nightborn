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

  @ParameterizedTest(name = "{0}: re-arms {1}, weakens {2}, unwings {3}, pulses {4}")
  @CsvSource(
      delimiter = ';',
      value = {
        "{\"radius\":10,\"duration_ticks\":400,\"rearms_traps\":true,\"look\":\"nightborn:killing_ground\"}; true; false; false; false",
        "{\"radius\":5,\"duration_ticks\":600,\"weakens\":true,\"unwings\":true,\"look\":\"nightborn:consecrate\"}; false; true; true; true",
        "{\"radius\":5,\"duration_ticks\":600,\"unwings\":true,\"look\":\"nightborn:consecrate\"}; false; false; true; true",
        "{\"radius\":5,\"duration_ticks\":600,\"look\":\"nightborn:consecrate\"}; false; false; false; false"
      })
  @DisplayName("a marked ground may re-arm traps, weaken vampires or hold them out of the air")
  void effects(
      final String json,
      final boolean rearms,
      final boolean weakens,
      final boolean unwings,
      final boolean pulses) {
    final FieldPower power =
        FieldPower.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(rearms, power.rearmsTraps());
    assertEquals(weakens, power.weakens());
    assertEquals(unwings, power.unwings());
    assertEquals(pulses, power.pulses());
  }

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
