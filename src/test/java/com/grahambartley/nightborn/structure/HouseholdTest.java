package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HouseholdTest {

  private static boolean loads(final String resident, final String residents) {
    final String json = "{\"resident\": \"" + resident + "\", \"residents\": " + residents + "}";
    return Household.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess();
  }

  @ParameterizedTest(name = "{0} x {1}: {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "minecraft:zombie | 3 | true",
        "minecraft:zombie | 0 | true",
        "minecraft:zombie | 16 | true",
        "minecraft:zombie | 17 | false",
        "minecraft:zombie | -1 | false",
        "minecraft:bat | {\"type\": \"minecraft:uniform\", \"min_inclusive\": 2, \"max_inclusive\": 4} | true",
        "minecraft:zombie | {\"type\": \"minecraft:uniform\", \"min_inclusive\": 2, \"max_inclusive\": 20} | false",
        "nightborn:no_such_thing | 3 | false",
        "Not An Id | 3 | false"
      })
  @DisplayName("a household names a real entity type and between 0 and 16 residents")
  void validates(final String resident, final String residents, final boolean loads) {
    assertEquals(loads, loads(resident, residents));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | true",
        "{\"nightborn:ancient\": 3, \"nightborn:elder\": 7} | true",
        "{\"nightborn:ancient\": 1001} | false",
        "{\"nightborn:ancient\": -1} | false"
      })
  @DisplayName("a household may set its own odds for each vampire stage")
  void stageOdds(final String stages, final boolean loads) {
    final String json =
        "{\"resident\": \"minecraft:zombie\", \"residents\": 2, \"stages\": " + stages + "}";
    assertEquals(
        loads, Household.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
