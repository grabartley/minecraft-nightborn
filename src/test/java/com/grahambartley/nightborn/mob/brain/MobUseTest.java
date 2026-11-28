package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

  @ParameterizedTest(name = "aim [{0}] -> {1}")
  @CsvSource({"self, SELF", "target, TARGET", "spot, SPOT", "ally, ALLY", "enemy, TARGET"})
  @DisplayName("a use's aim is read from its data name, and an unknown one aims at the target")
  void aimed(final String aim, final Aim aimed) {
    assertEquals(aimed, new MobUse("fight", aim, List.of(), 1.0F).aimed());
  }

  private static Situation.Member ally(final int id, final float health, final boolean claimed) {
    return new Situation.Member(new UUID(0, id), 3.0, health, false, true, claimed);
  }

  private static final MobUse MEND =
      new MobUse(
          "fight",
          "ally",
          List.of(new Consideration("ally_health", Curve.FALLING, 0.0F, 1.0F)),
          2.0F);

  @ParameterizedTest(name = "allies with health [{0}], claimed [{1}]: helps {2} scoring {3}")
  @CsvSource({
    "0.8 0.2 0.5, '', 2, 1.6",
    "0.8 0.2 0.5, 2, 3, 1",
    "0.9, '', 1, 0.2",
    "0.9, 1, 0, 0",
    "'', '', 0, 0"
  })
  @DisplayName(
      "a use about one ally weighs every ally it may help and keeps the one it scores best,"
          + " passing over any another mob has claimed")
  void scoresTheBestAlly(
      final String healths, final String claimed, final int helped, final double score) {
    final List<Situation.Member> allies = new ArrayList<>();
    final String[] each = healths.isEmpty() ? new String[0] : healths.split(" ");
    for (int i = 0; i < each.length; i++) {
      allies.add(ally(i + 1, Float.parseFloat(each[i]), claimed.contains(String.valueOf(i + 1))));
    }
    final MobUse.Scoring scoring =
        MEND.scoring(
            new Situation(
                Intent.FIGHT,
                Situation.Self.of(1.0F, 20),
                null,
                Situation.Place.UNKNOWN,
                Situation.Group.of(allies, List.of()),
                null));
    assertEquals(score, scoring.score(), 1.0E-6);
    assertEquals(
        helped == 0 ? null : new UUID(0, helped),
        scoring.ally() == null ? null : scoring.ally().id());
  }

  @ParameterizedTest(name = "[{0}]: helps an ally {1}")
  @CsvSource({
    "ally_health own_health, true",
    "ally_distance, true",
    "foes_in_radius, false",
    "'', false"
  })
  @DisplayName("a use helps an ally only when it weighs a fact about one")
  void helpsAnAlly(final String facts, final boolean helps) {
    assertEquals(
        helps,
        new MobUse(
                "fight",
                "allies",
                facts.isEmpty()
                    ? List.of()
                    : Arrays.stream(facts.split(" "))
                        .map(fact -> new Consideration(fact, Curve.BETWEEN, 0.0F, 1.0F))
                        .toList(),
                1.0F)
            .helpsAnAlly());
  }
}
