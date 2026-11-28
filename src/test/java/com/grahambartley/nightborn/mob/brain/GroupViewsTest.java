package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroupViewsTest {

  @ParameterizedTest(name = "attacker {0}, age {1}, last hit {2}, targeted {3}: under attack {4}")
  @CsvSource({
    "true, 200, 180, false, true",
    "true, 200, 140, false, true",
    "true, 200, 139, false, false",
    "false, 200, 199, false, false",
    "false, 200, 0, true, true",
    "true, 500, 0, true, true"
  })
  @DisplayName(
      "an ally is under attack while a foe has it as its target or for three seconds after it is"
          + " struck")
  void underAttack(
      final boolean attacker,
      final int age,
      final int lastHit,
      final boolean targeted,
      final boolean expected) {
    assertEquals(expected, GroupViews.underAttack(attacker, age, lastHit, targeted));
  }

  @ParameterizedTest(name = "has a target {0}, under attack {1}: fighting {2}")
  @CsvSource({"true, false, true", "false, true, true", "true, true, true", "false, false, false"})
  @DisplayName("an ally is in a fight while it has a target of its own or is under attack")
  void fighting(final boolean target, final boolean underAttack, final boolean fighting) {
    assertEquals(fighting, GroupViews.fighting(target, underAttack));
  }
}
