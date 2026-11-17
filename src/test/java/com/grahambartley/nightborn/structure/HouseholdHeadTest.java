package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class HouseholdHeadTest {

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"resident\": \"nightborn:vampire_lord\"} | nightborn:vampire_lord | 0",
        "{\"resident\": \"nightborn:vampire\", \"stages\": {\"nightborn:lord\": 1}} | nightborn:vampire | 1"
      })
  @DisplayName("a head reads the entity type that leads the household and its stage odds")
  void parses(final String json, final String resident, final int lordOdds) {
    final HouseholdHead head =
        HouseholdHead.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(resident, head.resident().getKey().orElseThrow().getValue().toString());
    assertEquals(
        lordOdds, head.stages().getOrDefault(Identifier.of("nightborn", "lord"), 0).intValue());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{}",
        "{\"resident\": \"nightborn:no_such_thing\"}",
        "{\"resident\": \"nightborn:vampire_lord\", \"stages\": {\"nightborn:lord\": -1}}"
      })
  @DisplayName("a head with no resident, an unknown resident or bad odds is rejected")
  void rejects(final String json) {
    assertEquals(
        false,
        HouseholdHead.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0} stages kept")
  @CsvSource({"0", "1", "3"})
  @DisplayName("the stage odds cannot be changed after reading")
  void keepsOdds(final int count) {
    final Map<Identifier, Integer> odds = new java.util.HashMap<>();
    for (int i = 0; i < count; i++) {
      odds.put(Identifier.of("nightborn", "stage" + i), i);
    }
    final HouseholdHead head =
        new HouseholdHead(
            net.minecraft.registry.Registries.ENTITY_TYPE.getEntry(
                com.grahambartley.nightborn.mob.NightbornMobs.VAMPIRE_LORD),
            odds,
            HeadSeat.LOWEST);
    odds.clear();
    assertEquals(count, head.stages().size());
  }

  @ParameterizedTest(name = "{0} seats its head {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"resident\": \"nightborn:vampire_lord\"} | LOWEST",
        "{\"resident\": \"nightborn:eldest\", \"seat\": \"highest\"} | HIGHEST",
        "{\"resident\": \"nightborn:eldest\", \"seat\": \"lowest\"} | LOWEST"
      })
  @DisplayName("a head takes the lowest floor unless its household seats it on the highest")
  void seats(final String json, final HeadSeat seat) {
    assertEquals(
        seat,
        HouseholdHead.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString(json))
            .getOrThrow()
            .seat());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{\"resident\": \"nightborn:eldest\", \"seat\": \"middle\"}"})
  @DisplayName("a seat that is not a floor is rejected")
  void rejectsUnknownSeat(final String json) {
    assertEquals(
        false,
        HouseholdHead.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
