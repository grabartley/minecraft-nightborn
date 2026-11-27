package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class FactTest {

  private static final Situation SEEN =
      new Situation(
          Intent.FIGHT,
          new Situation.Self(0.25F, 14, true, true, false),
          new Situation.Target(7.5, true, 0.6F));

  @ParameterizedTest(name = "{0} = {1}")
  @CsvSource({
    "TARGET_DISTANCE, 7.5",
    "TARGET_IN_SIGHT, 1",
    "TARGET_HEALTH, 0.6",
    "OWN_HEALTH, 0.25",
    "OWN_BLOOD, 14",
    "IN_FORM, 1",
    "SUN_ON_ME, 1",
    "DOWNED, 0"
  })
  @DisplayName("each fact reads its part of the situation")
  void reads(final Fact fact, final double value) {
    assertEquals(value, fact.read(SEEN), 1.0E-6);
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(
      value = Fact.class,
      names = {"TARGET_DISTANCE", "TARGET_IN_SIGHT", "TARGET_HEALTH"})
  @DisplayName("a target fact is missing when the mob has no target")
  void targetFactsNeedATarget(final Fact fact) {
    assertTrue(
        Double.isNaN(fact.read(new Situation(Intent.REST, Situation.Self.of(1.0F, 0), null))));
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
}
