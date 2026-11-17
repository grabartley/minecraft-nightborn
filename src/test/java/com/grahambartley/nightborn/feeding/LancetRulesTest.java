package com.grahambartley.nightborn.feeding;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class LancetRulesTest {

  @ParameterizedTest(name = "hurt {0}, bottle {1}: draws {2}")
  @CsvSource({"4, 4, 4", "2, 4, 4", "6, 4, 6", "1, 1, 1", "20, 1, 20"})
  @DisplayName("a lancet never draws less than a bottle's worth, so it never makes blood")
  void draw(final int hurt, final int bottle, final float draw) {
    assertEquals(draw, LancetRules.draw(hurt, bottle));
  }

  @ParameterizedTest(
      name = "warm {0}, vampire {1}, player {2}, alive {3}, health {4}, bottle {5} -> {6}")
  @CsvSource({
    "true, false, false, true, 10, true, BLEEDS",
    "true, false, false, true, 5, true, BLEEDS",
    "true, false, false, true, 4.9, true, TOO_WEAK",
    "true, false, false, true, 1, true, TOO_WEAK",
    "true, false, false, true, 10, false, NO_BOTTLE",
    "true, false, false, true, 2, false, NO_BOTTLE",
    "true, false, true, true, 20, true, A_PLAYER",
    "true, true, true, true, 20, true, A_PLAYER",
    "true, true, false, true, 20, true, NO_WARM_BLOOD",
    "false, false, false, true, 20, true, NO_WARM_BLOOD",
    "true, false, false, false, 0, true, NO_WARM_BLOOD"
  })
  @DisplayName("a lancet bleeds a living creature with warm blood, never another player")
  void onCreature(
      final boolean warmBlood,
      final boolean vampire,
      final boolean player,
      final boolean alive,
      final float health,
      final boolean emptyBottle,
      final LancetRules expected) {
    assertEquals(
        expected,
        LancetRules.onCreature(
            new LancetRules.Creature(warmBlood, vampire, player, alive, health),
            emptyBottle,
            4.0F));
  }

  @ParameterizedTest(name = "sneaking {0}, vampire {1}, bottle {2}, health {3} -> {4}")
  @CsvSource({
    "true, false, true, 20, BLEEDS",
    "true, false, true, 5, BLEEDS",
    "true, false, true, 4, TOO_WEAK",
    "true, false, false, 20, NO_BOTTLE",
    "false, false, true, 20, PASS",
    "true, true, true, 20, PASS",
    "false, true, true, 20, PASS"
  })
  @DisplayName("a sneaking human bleeds themself, and nobody else does")
  void onSelf(
      final boolean sneaking,
      final boolean vampire,
      final boolean emptyBottle,
      final float health,
      final LancetRules expected) {
    assertEquals(expected, LancetRules.onSelf(sneaking, vampire, emptyBottle, health, 4.0F));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(LancetRules.class)
  @DisplayName("every refusal says why, and success or a pass says nothing")
  void refusals(final LancetRules rule) {
    assertEquals(
        rule != LancetRules.BLEEDS && rule != LancetRules.PASS, rule.refusal().isPresent());
  }
}
