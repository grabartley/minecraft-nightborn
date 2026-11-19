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
class PowerTrapsTest {

  static final String SNARE =
      "\"trap\":{\"range\":4,\"duration_ticks\":6000,\"limit\":2,\"seen_within\":6,\"stepped\":true,"
          + "\"damage_type\":\"nightborn:silver\",\"look\":\"nightborn:snare\",\"spring\":"
          + "{\"sound\":\"minecraft:block.iron_trapdoor.close\",\"particle\":{\"type\":\"minecraft:crit\"}}}";
  static final String BAIT =
      "\"bait\":{\"range\":3,\"duration_ticks\":1200,\"lure_range\":24,\"scent_range\":8,"
          + "\"look\":\"nightborn:bait\"}";
  static final String NET =
      "\"net\":{\"range\":24,\"speed\":2,\"ground_ticks\":100,\"damage_type\":\"nightborn:silver\"}";
  static final String FIELD =
      "\"field\":{\"radius\":10,\"duration_ticks\":400,\"look\":\"nightborn:killing_ground\"}";

  @ParameterizedTest(name = "{0}: places {1} within {2}")
  @CsvSource({
    "snare, true, 4",
    "bait, true, 3",
    "net, false, 0",
    "field, false, 0",
    "none, false, 0"
  })
  @DisplayName(
      "a trap or bait is placed on the ground within its reach, and a net or ground is not")
  void places(final String kind, final boolean places, final float range) {
    final String body =
        switch (kind) {
          case "snare" -> SNARE;
          case "bait" -> BAIT;
          case "net" -> NET;
          case "field" -> FIELD;
          default -> "";
        };
    final PowerTraps traps =
        SkillPower.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString(
                    "{\"blood\":0,\"cooldown_ticks\":400"
                        + (body.isEmpty() ? "" : "," + body)
                        + "}"))
            .getOrThrow()
            .traps();
    assertEquals(places, traps.places());
    assertEquals(range, traps.placeRange().orElse(0.0F));
    assertEquals(kind.equals("none"), traps.equals(PowerTraps.NONE));
  }
}
