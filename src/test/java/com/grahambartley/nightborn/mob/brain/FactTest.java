package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class FactTest {

  private static final Situation SEEN =
      new Situation(
          Intent.FIGHT,
          new Situation.Self(0.25F, 14, true, true, false, true),
          new Situation.Target(7.5, true, 0.6F, true, false, true, false, true, false, false),
          new Situation.Place(11.5, new Route(1.0, 1.8, 40.0), 72.0, 900.0, 4.0, 3.0));

  private static final Situation OTHER_SIGNS =
      new Situation(
          Intent.FIGHT,
          Situation.Self.of(1.0F, 0),
          new Situation.Target(
              3.0, false, 1.0F, false, true, false, true, false, true, true, true));

  @ParameterizedTest(name = "{0} = {1}")
  @CsvSource({
    "TARGET_DISTANCE, 7.5",
    "TARGET_IN_SIGHT, 1",
    "TARGET_HEALTH, 0.6",
    "OWN_HEALTH, 0.25",
    "OWN_BLOOD, 14",
    "IN_FORM, 1",
    "SUN_ON_ME, 1",
    "DOWNED, 0",
    "STUCK, 1",
    "TARGET_WINDING_UP, 1",
    "TARGET_RUNNING, 0",
    "TARGET_SLOWED, 1",
    "TARGET_AIRBORNE, 0",
    "TARGET_VAMPIRE, 1",
    "TARGET_HUNTER, 0",
    "TARGET_AWARE, 0",
    "TARGET_THREATENING, 0",
    "SPOT_DISTANCE, 11.5",
    "GAP_AHEAD, 1",
    "DETOUR, 1.8",
    "TRIP_LENGTH, 40",
    "HOME_DISTANCE, 72",
    "UNTIL_DAWN, 900",
    "LIGHT, 4",
    "ROOM_TO_FLY, 3"
  })
  @DisplayName("each fact reads its part of the situation")
  void reads(final Fact fact, final double value) {
    assertEquals(value, fact.read(SEEN), 1.0E-6);
  }

  @ParameterizedTest(name = "{0} = {1}")
  @CsvSource({
    "TARGET_WINDING_UP, 0",
    "TARGET_RUNNING, 1",
    "TARGET_SLOWED, 0",
    "TARGET_AIRBORNE, 1",
    "TARGET_VAMPIRE, 0",
    "TARGET_HUNTER, 1",
    "TARGET_AWARE, 1",
    "TARGET_THREATENING, 1"
  })
  @DisplayName("each sign of the target reads the other way on a target that shows the others")
  void readsOtherSigns(final Fact fact, final double value) {
    assertEquals(value, fact.read(OTHER_SIGNS), 1.0E-6);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Fact.class,
      names = {
        "TARGET_DISTANCE",
        "TARGET_IN_SIGHT",
        "TARGET_HEALTH",
        "TARGET_WINDING_UP",
        "TARGET_RUNNING",
        "TARGET_SLOWED",
        "TARGET_AIRBORNE",
        "TARGET_VAMPIRE",
        "TARGET_HUNTER",
        "TARGET_AWARE",
        "TARGET_THREATENING"
      })
  @DisplayName("a target fact is missing when the mob has no target")
  void targetFactsNeedATarget(final Fact fact) {
    assertTrue(
        Double.isNaN(fact.read(new Situation(Intent.REST, Situation.Self.of(1.0F, 0), null))));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Fact.class,
      names = {
        "SPOT_DISTANCE",
        "GAP_AHEAD",
        "DETOUR",
        "TRIP_LENGTH",
        "HOME_DISTANCE",
        "UNTIL_DAWN",
        "LIGHT",
        "ROOM_TO_FLY"
      })
  @DisplayName("a place fact is missing when the mob has not looked at its place")
  void placeFactsNeedALook(final Fact fact) {
    assertTrue(
        Double.isNaN(fact.read(new Situation(Intent.RETREAT, Situation.Self.of(1.0F, 0), null))));
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Fact.class)
  @DisplayName("each fact is found by its data name")
  void byKey(final Fact fact) {
    assertEquals(Optional.of(fact), Fact.byKey(fact.key()));
  }

  @ParameterizedTest(name = "name [{0}]")
  @CsvSource({"TARGET_DISTANCE", "distance", "''"})
  @DisplayName("a name that is not a fact finds nothing")
  void unknown(final String key) {
    assertEquals(Optional.empty(), Fact.byKey(key));
  }

  private static final Situation.Member WARD =
      new Situation.Member(new UUID(0, 9), 4.5, 0.35F, true, true, true, false);

  private static final Situation HELPING =
      new Situation(
          Intent.FIGHT,
          Situation.Self.of(1.0F, 0),
          null,
          Situation.Place.UNKNOWN,
          Situation.Group.of(
              List.of(
                  WARD,
                  new Situation.Member(new UUID(0, 8), 10.0, 1.0F, false, false, true, false)),
              List.of(2.0, 6.0, 7.0)),
          WARD);

  @ParameterizedTest(name = "{0} within {1} = {2}")
  @CsvSource({
    "ALLY_DISTANCE, 0, 4.5",
    "ALLY_HEALTH, 0, 0.35",
    "ALLY_UNDER_ATTACK, 0, 1",
    "ALLY_HEALING, 0, 1",
    "ALLIES_IN_RADIUS, 5, 1",
    "ALLIES_IN_RADIUS, 12, 2",
    "FOES_IN_RADIUS, 6, 2",
    "FOES_IN_RADIUS, 1, 0"
  })
  @DisplayName("each group fact reads the ally being helped or counts within its radius")
  void readsTheGroup(final Fact fact, final double radius, final double value) {
    assertEquals(value, fact.read(HELPING, radius), 1.0E-6);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Fact.class,
      names = {
        "ALLY_DISTANCE",
        "ALLY_HEALTH",
        "ALLY_UNDER_ATTACK",
        "ALLY_HEALING",
        "ALLIES_IN_RADIUS",
        "FOES_IN_RADIUS"
      })
  @DisplayName("a group fact is missing when no group was read and no ally is being helped")
  void groupFactsNeedAGroup(final Fact fact) {
    assertTrue(
        Double.isNaN(fact.read(new Situation(Intent.FIGHT, Situation.Self.of(1.0F, 0), null), 8)));
  }
}
