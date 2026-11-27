package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MobUseTest {

  private static Situation situation(final Intent intent, final float health) {
    return new Situation(
        intent, Situation.Self.of(health, 20), new Situation.Target(5.0, true, 1.0F));
  }

  private static MobUse use(final String intent, final String aim, final float weight) {
    return new MobUse(
        intent,
        aim,
        List.of(
            new Consideration("target_distance", Curve.BETWEEN, 0.0F, 10.0F),
            new Consideration("own_health", Curve.FALLING, 0.0F, 1.0F)),
        weight);
  }

  @ParameterizedTest(name = "{0} {1} weight {2}, intent {3}, health {4}: {5}")
  @CsvSource({
    "fight, target, 1, FIGHT, 0.5, 0.5",
    "fight, target, 4, FIGHT, 0.25, 3",
    "fight, self, 2, FIGHT, 0, 2",
    "fight, target, 1, FIGHT, 1, 0",
    "fight, target, 1, RETREAT, 0.5, 0",
    "retreat, self, 1, RETREAT, 0.5, 0.5",
    "fight, nowhere, 1, FIGHT, 0.5, 0",
    "charge, target, 1, FIGHT, 0.5, 0",
    "fight, target, 0, FIGHT, 0.5, 0"
  })
  @DisplayName(
      "a use scores its weight times each consideration, only for its own intent and a known aim,"
          + " and any zero rules it out")
  void score(
      final String intent,
      final String aim,
      final float weight,
      final Intent now,
      final float health,
      final double score) {
    assertEquals(score, use(intent, aim, weight).score(situation(now, health)), 1.0E-6);
  }

  @Test
  @DisplayName("a use with no considerations scores its weight")
  void weightAlone() {
    assertEquals(
        2.5,
        new MobUse("fight", "self", List.of(), 2.5F).score(situation(Intent.FIGHT, 1.0F)),
        1.0E-6);
  }

  @Test
  @DisplayName("a use aims at its target and weighs 1 unless it says otherwise")
  void parsesDefaults() {
    assertEquals(
        new MobUse("travel", "target", List.of(), 1.0F),
        MobUse.CODEC
            .parse(JsonOps.INSTANCE, JsonParser.parseString("{\"intent\":\"travel\"}"))
            .getOrThrow());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(
      strings = {
        "{}",
        "{\"intent\":\"fight\",\"weight\":-1}",
        "{\"intent\":\"fight\",\"weight\":101}"
      })
  @DisplayName("a use with no intent or a weight out of bounds is rejected")
  void rejects(final String json) {
    assertFalse(MobUse.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).isSuccess());
  }
}
