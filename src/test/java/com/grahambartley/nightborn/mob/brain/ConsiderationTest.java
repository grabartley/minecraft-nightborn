package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConsiderationTest {

  private static final Situation SITUATION =
      new Situation(
          Intent.FIGHT, Situation.Self.of(0.4F, 10), new Situation.Target(6.0, false, 1.0F));

  @ParameterizedTest(name = "{0} {1} {2}..{3} = {4}")
  @CsvSource({
    "target_distance, BETWEEN, 4, 8, 1",
    "target_distance, BETWEEN, 7, 8, 0",
    "own_health, FALLING, 0, 0.8, 0.5",
    "own_blood, RISING, 0, 20, 0.5",
    "target_in_sight, BETWEEN, 1, 1, 0",
    "unknown_fact, BETWEEN, 0, 100, 0"
  })
  @DisplayName("a consideration reads its fact through its curve, and an unknown fact rules out")
  void value(
      final String fact, final Curve curve, final float low, final float high, final double value) {
    assertEquals(value, new Consideration(fact, curve, low, high).value(SITUATION), 1.0E-6);
  }

  @Test
  @DisplayName("a consideration's curve and low end are optional")
  void parsesDefaults() {
    assertEquals(
        new Consideration("target_distance", Curve.BETWEEN, 0.0F, 6.0F),
        Consideration.CODEC
            .parse(
                JsonOps.INSTANCE,
                JsonParser.parseString("{\"fact\":\"target_distance\",\"high\":6}"))
            .getOrThrow());
  }

  private static final Situation.Member HURT =
      new Situation.Member(new UUID(0, 1), 3.0, 0.25F, true, false, true, false);

  private static final Situation IN_A_GROUP =
      new Situation(
          Intent.FIGHT,
          Situation.Self.of(1.0F, 10),
          null,
          Situation.Place.UNKNOWN,
          Situation.Group.of(List.of(HURT), List.of(2.0, 3.0, 9.0)),
          HURT);

  @ParameterizedTest(name = "{0} radius {1}, {2} {3}..{4} = {5}")
  @CsvSource({
    "foes_in_radius, 4, RISING, 1, 3, 0.5",
    "foes_in_radius, 10, RISING, 1, 3, 1",
    "foes_in_radius, 1, RISING, 1, 3, 0",
    "allies_in_radius, 16, FALLING, 0, 1, 0",
    "allies_in_radius, 2, FALLING, 0, 1, 1",
    "ally_health, 0, FALLING, 0, 0.5, 0.5",
    "ally_under_attack, 0, BETWEEN, 1, 1, 1",
    "ally_distance, 0, BETWEEN, 0, 2, 0"
  })
  @DisplayName("a consideration counts within its own radius and reads the ally being helped")
  void groupValue(
      final String fact,
      final float radius,
      final Curve curve,
      final float low,
      final float high,
      final double value) {
    assertEquals(
        value, new Consideration(fact, curve, low, high, radius).value(IN_A_GROUP), 1.0E-6);
  }

  @ParameterizedTest(name = "{0}: reads an ally {1}")
  @CsvSource({
    "ally_health, true",
    "ally_under_attack, true",
    "ally_distance, true",
    "allies_in_radius, false",
    "own_health, false",
    "moon_phase, false"
  })
  @DisplayName("only the facts about one ally make a use pick an ally to help")
  void readsAlly(final String fact, final boolean reads) {
    assertEquals(reads, new Consideration(fact, Curve.BETWEEN, 0.0F, 1.0F).readsAlly());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "'{\"fact\":\"foes_in_radius\",\"high\":3,\"radius\":4}', 4, true",
    "'{\"fact\":\"foes_in_radius\",\"high\":3,\"radius\":25}', 0, false"
  })
  @DisplayName("a radius is read from data, up to how far a mob looks around itself")
  void parsesRadius(final String json, final float radius, final boolean parses) {
    final var parsed = Consideration.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json));
    assertEquals(parses, parsed.isSuccess());
    if (parses) {
      assertEquals(radius, parsed.getOrThrow().radius(), 1.0E-6);
    }
  }
}
