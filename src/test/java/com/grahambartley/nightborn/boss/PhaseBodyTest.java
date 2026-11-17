package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PhaseBodyTest {

  private static final Identifier ARMOR = Identifier.ofVanilla("generic.armor");

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"attributes\":{\"minecraft:generic.armor\":12,\"minecraft:generic.scale\":0.35},\"max_hit\":10,\"glowing\":true} | true",
        "{\"max_hit\":-1} | false",
        "{\"attributes\":{\"minecraft:generic.armor\":65}} | false",
        "{\"glowing\":\"yes\"} | false"
      })
  @DisplayName("a body is a set of attribute bonuses, a hardest hit and whether the eyes blaze")
  void parses(final String json, final boolean loads) {
    assertEquals(
        loads, PhaseBody.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "a blow of {0} against a hardest hit of {1}, uncapped {2}: {3}")
  @CsvSource({
    "8, 10, false, 8",
    "10, 10, false, 10",
    "400, 10, false, 10",
    "400, 0, false, 400",
    "400, 10, true, 400"
  })
  @DisplayName("no single blow lands harder than the phase allows, unless it is uncapped")
  void limits(final float amount, final float maxHit, final boolean uncapped, final float dealt) {
    assertEquals(dealt, new PhaseBody(Map.of(), maxHit, false).limit(amount, uncapped));
  }

  @ParameterizedTest(name = "from {0} to {1} at {2}: {3}")
  @CsvSource({
    "0, 0.35, 0.0, 0.0",
    "0, 0.35, 0.5, 0.175",
    "0, 0.35, 1.0, 0.35",
    "0.12, 0.35, 2.0, 0.35",
    "0.12, 0.35, -1.0, 0.12",
    "8, 8, 0.4, 8"
  })
  @DisplayName("a bonus grows evenly from the last phase's to this one's while the boss changes")
  void blends(final double from, final double to, final double progress, final double value) {
    assertEquals(value, PhaseBody.blend(from, to, progress), 1.0E-9);
  }

  @ParameterizedTest(name = "{0} gives {1}")
  @CsvSource({"minecraft:generic.armor, 12.0", "minecraft:generic.scale, 0.0"})
  @DisplayName("an attribute the body does not name gets no bonus")
  void bonus(final String attribute, final double bonus) {
    assertEquals(
        bonus, new PhaseBody(Map.of(ARMOR, 12.0), 0.0F, false).bonus(Identifier.of(attribute)));
  }
}
