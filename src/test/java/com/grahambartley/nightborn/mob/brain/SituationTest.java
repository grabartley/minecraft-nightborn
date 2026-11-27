package com.grahambartley.nightborn.mob.brain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
