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
class TrapPowerTest {

  static final String SPRING =
      "\"spring\":{\"sound\":\"minecraft:block.iron_trapdoor.close\",\"particle\":{\"type\":\"minecraft:crit\"}}";

  static TrapPower read(final String body) {
    return TrapPower.CODEC
        .parse(JsonOps.INSTANCE, JsonParser.parseString("{" + body + "," + SPRING + "}"))
        .getOrThrow();
  }

  @ParameterizedTest(name = "{0}: refused {1}")
  @CsvSource(
      delimiter = ';',
      value = {
        "\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\"; false",
        "\"range\":4,\"duration_ticks\":6000,\"limit\":1,\"seen_within\":6,\"stepped\":false,\"trigger_radius\":3,\"damage\":6,\"damage_type\":\"nightborn:holy_light\",\"look\":\"nightborn:flash_trap\"; false",
        "\"range\":0,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\"; true",
        "\"range\":4,\"duration_ticks\":6000,\"limit\":0,\"seen_within\":6,\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\"; true",
        "\"range\":4,\"duration_ticks\":10,\"limit\":2,\"seen_within\":6,\"stepped\":true,\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\"; true",
        "\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,\"look\":\"nightborn:snare\"; true"
      })
  @DisplayName("a trap needs a reach, a life, a limit, a damage type and a look")
  void reads(final String body, final boolean refused) {
    assertEquals(
        refused,
        TrapPower.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{" + body + "," + SPRING + "}"))
            .isError());
  }

  @ParameterizedTest(name = "pins {0} a mob, {1} a player")
  @CsvSource({"60, 40", "0, 0", "100, 20"})
  @DisplayName("a trap pins a player for its own time and anything else for the mob time")
  void pins(final int mob, final int player) {
    final TrapPower trap =
        read(
            "\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,"
                + "\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\","
                + "\"pin_ticks\":"
                + mob
                + ",\"player_pin_ticks\":"
                + player);
    assertEquals(mob, trap.pinTicks(false));
    assertEquals(player, trap.pinTicks(true));
    assertEquals(0.5F, trap.size());
    assertEquals(true, trap.catchesBatsAndMist());
  }
}
