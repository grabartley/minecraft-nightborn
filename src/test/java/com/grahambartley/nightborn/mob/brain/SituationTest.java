package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SituationTest {

  @Test
  @DisplayName("a situation with no place given knows no route")
  void noPlace() {
    final Situation situation = new Situation(Intent.FIGHT, Situation.Self.of(1.0F, 0), null);
    assertEquals(Situation.Place.UNKNOWN, situation.place());
    assertTrue(Double.isNaN(situation.place().spotDistance()));
  }

  @ParameterizedTest(name = "distance {0}, sight {1}, health {2}")
  @CsvSource({"3, true, 1", "12.5, false, 0.2"})
  @DisplayName("a target read without its signs shows none of them")
  void targetWithoutSigns(final double distance, final boolean sight, final float health) {
    assertEquals(
        new Situation.Target(distance, sight, health, false, false, false, false, false, false),
        new Situation.Target(distance, sight, health));
  }

  private static Situation.Member member(
      final double distance, final boolean fighting, final boolean claimed) {
    return new Situation.Member(UUID.randomUUID(), distance, 0.5F, false, false, fighting, claimed);
  }

  @ParameterizedTest(name = "radius {0}: allies {1}, foes {2}")
  @CsvSource({"2, 1, 0", "5, 1, 1", "12, 2, 2", "0.5, 0, 0"})
  @DisplayName(
      "a group counts the allies in the fight and the foes within a radius of the mob, and not an"
          + " ally standing idle")
  void countsWithinRadius(final double radius, final double allies, final double foes) {
    final Situation.Group group =
        Situation.Group.of(
            List.of(member(1.5, true, false), member(5.0, false, false), member(9.0, true, false)),
            List.of(4.0, 12.0));
    assertEquals(allies, group.alliesWithin(radius));
    assertEquals(foes, group.foesWithin(radius));
  }

  @Test
  @DisplayName("a group that was never read counts nothing, so no use that counts can pass")
  void unknownGroupCountsNothing() {
    assertTrue(Double.isNaN(Situation.Group.UNKNOWN.alliesWithin(10)));
    assertTrue(Double.isNaN(Situation.Group.UNKNOWN.foesWithin(10)));
  }

  @ParameterizedTest(name = "first fighting {0}, second fighting {1}: an ally fights {2}")
  @CsvSource({"true, false, true", "false, true, true", "false, false, false"})
  @DisplayName("a group knows whether any ally near the mob is in a fight")
  void allyFighting(final boolean first, final boolean second, final boolean fighting) {
    assertEquals(
        fighting,
        Situation.Group.of(List.of(member(3, first, false), member(4, second, false)), List.of())
            .allyFighting());
  }

  @Test
  @DisplayName("an ally another mob has claimed cannot be helped, and helping one focuses on it")
  void helpableSkipsClaimed() {
    final Situation.Member free = member(3, true, false);
    final Situation.Group group =
        Situation.Group.of(List.of(member(2, true, true), free), List.of());
    assertEquals(List.of(free), group.helpable());
    final Situation situation =
        new Situation(
                Intent.FIGHT,
                Situation.Self.of(1.0F, 0),
                null,
                Situation.Place.UNKNOWN,
                group,
                null)
            .helping(free);
    assertEquals(free, situation.ally());
    assertEquals(group, situation.group());
  }
}
