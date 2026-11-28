package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

  @ParameterizedTest(name = "spot {0}")
  @CsvSource({"NaN", "0", "11.5"})
  @DisplayName("a place given only the spot ahead knows nothing else of where the mob is")
  void spotOnly(final double spot) {
    final Situation.Place place = new Situation.Place(spot);
    assertEquals(spot, place.spotDistance(), 1.0E-9);
    assertEquals(Route.UNKNOWN, place.route());
    assertTrue(Double.isNaN(place.homeDistance()));
    assertTrue(Double.isNaN(place.untilDawn()));
    assertTrue(Double.isNaN(place.light()));
    assertTrue(Double.isNaN(place.roomToFly()));
  }

  @ParameterizedTest(name = "distance {0}, vampire {1}, hunter {2}")
  @CsvSource({"3, true, false", "20, false, true"})
  @DisplayName("a target read with its signs but no word on awareness counts as aware of the mob")
  void awareUnlessSaid(final double distance, final boolean vampire, final boolean hunter) {
    assertTrue(
        new Situation.Target(distance, true, 1.0F, false, false, false, false, vampire, hunter)
            .aware());
  }

  @ParameterizedTest(name = "distance {0}, sight {1}, health {2}")
  @CsvSource({"3, true, 1", "12.5, false, 0.2"})
  @DisplayName("a target read without its signs shows none of them")
  void targetWithoutSigns(final double distance, final boolean sight, final float health) {
    assertEquals(
        new Situation.Target(distance, sight, health, false, false, false, false, false, false),
        new Situation.Target(distance, sight, health));
  }

  @ParameterizedTest(name = "aware {0}")
  @CsvSource({"true", "false"})
  @DisplayName("a target read without the threat test is not counted as threatening")
  void targetNotThreateningByDefault(final boolean aware) {
    assertFalse(
        new Situation.Target(3.0, true, 1.0F, true, false, false, false, false, false, aware)
            .threatening());
  }

  private static Situation.Member member(
      final double distance, final boolean fighting, final boolean claimed) {
    return new Situation.Member(UUID.randomUUID(), distance, 0.5F, false, false, fighting, claimed);
  }

  @ParameterizedTest(name = "radius {0}: allies {1}, foes {2}, idle allies {3}")
  @CsvSource({"2, 1, 0, 0", "5, 1, 1, 1", "12, 2, 2, 1", "0.5, 0, 0, 0"})
  @DisplayName(
      "a group counts the allies in the fight, the foes, and apart from them the allies standing"
          + " idle that a call would bring, within a radius of the mob")
  void countsWithinRadius(
      final double radius, final double allies, final double foes, final double idle) {
    final Situation.Group group =
        Situation.Group.of(
            List.of(member(1.5, true, false), member(5.0, false, false), member(9.0, true, false)),
            List.of(4.0, 12.0));
    assertEquals(allies, group.alliesWithin(radius));
    assertEquals(foes, group.foesWithin(radius));
    assertEquals(idle, group.idleAlliesWithin(radius));
  }

  @Test
  @DisplayName("a group that was never read counts nothing, so no use that counts can pass")
  void unknownGroupCountsNothing() {
    assertTrue(Double.isNaN(Situation.Group.UNKNOWN.alliesWithin(10)));
    assertTrue(Double.isNaN(Situation.Group.UNKNOWN.foesWithin(10)));
    assertTrue(Double.isNaN(Situation.Group.UNKNOWN.idleAlliesWithin(10)));
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
