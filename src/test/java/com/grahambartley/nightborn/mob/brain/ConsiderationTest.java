package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
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
}
