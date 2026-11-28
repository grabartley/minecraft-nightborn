package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SharpnessTest {

  private static final Set<Fact> NEEDED =
      EnumSet.of(
          Fact.TARGET_DISTANCE,
          Fact.OWN_HEALTH,
          Fact.SUN_ON_ME,
          Fact.ALLY_HEALTH,
          Fact.FOES_IN_RADIUS,
          Fact.LIGHT,
          Fact.GAP_AHEAD);

  private static Sharpness reading(final String reads) {
    final Set<Sharpness.Reads> groups = EnumSet.noneOf(Sharpness.Reads.class);
    if (!reads.isBlank()) {
      Arrays.stream(reads.split(";")).map(Sharpness.Reads::valueOf).forEach(groups::add);
    }
    return new Sharpness(0.0F, groups, 1.0F);
  }

  private static Set<Fact> facts(final String keys) {
    return Arrays.stream(keys.split(";")).map(Fact::valueOf).collect(Collectors.toSet());
  }

  @ParameterizedTest(name = "reads [{0}]: sees {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "| TARGET_DISTANCE;OWN_HEALTH;SUN_ON_ME",
        "GROUP | TARGET_DISTANCE;OWN_HEALTH;SUN_ON_ME;ALLY_HEALTH;FOES_IN_RADIUS",
        "PLACE | TARGET_DISTANCE;OWN_HEALTH;SUN_ON_ME;LIGHT;GAP_AHEAD",
        "GROUP;PLACE | TARGET_DISTANCE;OWN_HEALTH;SUN_ON_ME;ALLY_HEALTH;FOES_IN_RADIUS;LIGHT;GAP_AHEAD"
      })
  @DisplayName("a mob always reads itself and its target, and the group and place only if sharp")
  void visible(final String reads, final String seen) {
    assertEquals(facts(seen), reading(reads == null ? "" : reads).visible(NEEDED));
  }

  @Test
  @DisplayName("a mob that needs nothing sees nothing, however sharp")
  void nothingNeeded() {
    assertEquals(Set.of(), Sharpness.KEEN.visible(EnumSet.noneOf(Fact.class)));
  }

  @ParameterizedTest(name = "blur {0}, score {1}, roll {2}: {3}")
  @CsvSource({
    "0, 1, 0, 1",
    "0, 1, 1, 1",
    "0.5, 1, 0.5, 1",
    "0.5, 1, 0, 0.5",
    "0.5, 1, 1, 1.5",
    "0.5, 2, 0.25, 1.5",
    "0.9, 1, 0, 0.1"
  })
  @DisplayName("blur swings a score up or down by its share, and a sharp mob's score holds")
  void blurred(final float blur, final double score, final double roll, final double blurred) {
    assertEquals(
        blurred,
        new Sharpness(blur, EnumSet.allOf(Sharpness.Reads.class), 1.0F).blurred(score, roll),
        1.0E-6);
  }

  @Test
  @DisplayName("a mob with no sharpness in its data thinks as keenly as before")
  void defaults() {
    assertEquals(
        Sharpness.KEEN,
        Sharpness.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow());
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{\"blur\": 0.9} | true",
        "{\"blur\": 0.95} | false",
        "{\"blur\": -0.1} | false",
        "{\"foresight\": 2} | true",
        "{\"foresight\": 2.5} | false",
        "{\"reads\": []} | true",
        "{\"reads\": [\"group\", \"place\"]} | true",
        "{\"reads\": [\"tactics\"]} | true",
        "{\"chains\": true} | true",
        "{\"reads\": [\"moon\"]} | false"
      })
  @DisplayName(
      "blur, foresight and the groups a mob reads are refused when out of range or unknown")
  void bounds(final String json, final boolean loads) {
    assertEquals(
        loads, Sharpness.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource(
      delimiter = '|',
      value = {"{\"reads\": []} | ''", "{\"reads\": [\"place\"]} | PLACE"})
  @DisplayName("the groups a mob reads come from its data")
  void reads(final String json, final String reads) {
    assertEquals(
        reading(reads).reads(),
        Sharpness.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow().reads());
  }

  @ParameterizedTest(name = "{0}: tactics {1}, chains {2}")
  @CsvSource(
      delimiter = '|',
      value = {
        "{} | false | false",
        "{\"reads\": [\"group\", \"place\"]} | false | false",
        "{\"reads\": [\"group\", \"place\", \"tactics\"], \"chains\": true} | true | true",
        "{\"chains\": true} | false | true"
      })
  @DisplayName(
      "only a mind whose data says so reads the tactics of a fight or chains one power into the"
          + " next")
  void tacticsAndChainsAreChosen(final String json, final boolean tactics, final boolean chains) {
    final Sharpness sharpness =
        Sharpness.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
    assertEquals(tactics, sharpness.reads(Sharpness.Reads.TACTICS));
    assertEquals(chains, sharpness.chains());
    assertEquals(
        tactics,
        sharpness
            .visible(EnumSet.of(Fact.TARGET_HEALING, Fact.OWN_HEALTH))
            .contains(Fact.TARGET_HEALING));
  }
}
