package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.DataResult;
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
        "minecraft:zombie | 20 | true",
        "minecraft:zombie | 32 | true",
        "minecraft:zombie | 33 | false",
        "minecraft:zombie | -1 | false",
        "minecraft:bat | {\"type\": \"minecraft:uniform\", \"min_inclusive\": 2, \"max_inclusive\": 4} | true",
        "minecraft:zombie | {\"type\": \"minecraft:uniform\", \"min_inclusive\": 2, \"max_inclusive\": 40} | false",
        "nightborn:no_such_thing | 3 | false",
        "Not An Id | 3 | false"
      })
  @DisplayName("a household names a real entity type and between 0 and 32 residents")
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

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"resident\": \"nightborn:vampire_lord\"} | true",
        "{\"resident\": \"nightborn:vampire_lord\", \"stages\": {\"nightborn:lord\": 1}} | true",
        "{\"stages\": {\"nightborn:lord\": 1}} | false",
        "{\"resident\": \"nightborn:no_such_thing\"} | false"
      })
  @DisplayName("a household may have one head, a real entity type with its own stage odds")
  void head(final String head, final boolean loads) {
    final String json =
        "{\"resident\": \"minecraft:zombie\", \"residents\": 2, \"head\": " + head + "}";
    assertEquals(
        loads, Household.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  private static DataResult<Household> ceilinged(final String ceiling) {
    return Household.CODEC.parse(
        JsonOps.INSTANCE,
        JsonParser.parseString(
            "{\"resident\": \"minecraft:zombie\", \"residents\": 3" + ceiling + "}"));
  }

  @ParameterizedTest(name = "ceiling {0}, floor at {1}, heart top {2}: looks up to {3}")
  @CsvSource({"6, 68, 190, 76", "6, 186, 190, 190", "64, 60, 100, 100", "1, 10, 90, 13"})
  @DisplayName("a household with a ceiling counts a roof only that far over a resident's head")
  void shelterTop(final int ceiling, final int floor, final int heartTop, final int top) {
    assertEquals(
        top, ceilinged(", \"ceiling\": " + ceiling).getOrThrow().shelterTop(floor, heartTop));
  }

  @ParameterizedTest(name = "floor at {0}, heart top {1}")
  @CsvSource({"68, 190", "10, 40"})
  @DisplayName("a household with no ceiling counts any roof in its heart")
  void noCeiling(final int floor, final int heartTop) {
    assertEquals(heartTop, ceilinged("").getOrThrow().shelterTop(floor, heartTop));
  }

  @ParameterizedTest(name = "ceiling {0} loads: {1}")
  @CsvSource({"1, true", "64, true", "0, false", "65, false", "-3, false"})
  @DisplayName("a ceiling is between 1 and 64 blocks")
  void ceilingBounds(final int ceiling, final boolean loads) {
    assertEquals(loads, ceilinged(", \"ceiling\": " + ceiling).isSuccess());
  }
}
