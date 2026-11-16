package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FogRulesTest {

  @ParameterizedTest(name = "now {0}, started {1} -> {2}")
  @CsvSource({"100, 100, true", "105, 100, false", "110, 100, true", "120, 100, true"})
  @DisplayName("a fog touches those inside it twice a second, from the moment it rises")
  void pulses(final long now, final long started, final boolean expected) {
    assertEquals(expected, FogRules.pulses(now, started));
  }

  @ParameterizedTest(name = "now {0}, ends {1} -> {2}")
  @CsvSource({"99, 100, true", "100, 100, false", "200, 100, false"})
  @DisplayName("a fog lifts when its time is up")
  void lasts(final long now, final long ends, final boolean expected) {
    assertEquals(expected, FogRules.lasts(now, ends));
  }

  @ParameterizedTest(name = "{0} squared within {1} -> {2}")
  @CsvSource({"15.9, 4.0, true", "16.0, 4.0, true", "16.1, 4.0, false"})
  @DisplayName("a fog covers a round patch around where it was breathed")
  void covers(final double distanceSquared, final float radius, final boolean expected) {
    assertEquals(expected, FogRules.covers(distanceSquared, radius));
  }

  @ParameterizedTest(name = "alive {0}, caster {1}, serves {2}, spectating {3} -> {4}")
  @CsvSource({
    "true, false, false, false, true",
    "false, false, false, false, false",
    "true, true, false, false, false",
    "true, false, true, false, false",
    "true, false, false, true, false"
  })
  @DisplayName("a fog spares its caster, those who serve them and spectators")
  void touches(
      final boolean alive,
      final boolean caster,
      final boolean serves,
      final boolean spectating,
      final boolean expected) {
    assertEquals(expected, FogRules.touches(alive, caster, serves, spectating));
  }
}
