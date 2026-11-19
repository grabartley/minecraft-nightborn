package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
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

  @ParameterizedTest(
      name = "alive {0}, caster {1}, serves {2}, spectating {3}, sees through {4} -> {5}")
  @CsvSource({
    "true, false, false, false, false, true",
    "false, false, false, false, false, false",
    "true, true, false, false, false, false",
    "true, false, true, false, false, false",
    "true, false, false, true, false, false",
    "true, false, false, false, true, false"
  })
  @DisplayName(
      "a fog spares its caster, those who serve them, spectators and those who see through fog")
  void touches(
      final boolean alive,
      final boolean caster,
      final boolean serves,
      final boolean spectating,
      final boolean seesThrough,
      final boolean expected) {
    assertEquals(expected, FogRules.touches(alive, caster, serves, spectating, seesThrough));
  }

  @ParameterizedTest(name = "{0}: blinds {1}")
  @CsvSource({"blindness, true", "darkness, true", "slowness, false", "weakness, false"})
  @DisplayName("only the effects that take sight count as blinding")
  void blinds(final String effect, final boolean blinds) {
    assertEquals(
        blinds,
        FogRules.blinds(
            Registries.STATUS_EFFECT.getEntry(Identifier.ofVanilla(effect)).orElseThrow()));
  }

  @ParameterizedTest(name = "blinding {0}, clear sight {1}: reaches {2}")
  @CsvSource({"true, true, false", "true, false, true", "false, true, true", "false, false, true"})
  @DisplayName("clear sight keeps the fog from blinding, while its slowness still lands")
  void reaches(final boolean blinding, final boolean clearSight, final boolean reaches) {
    assertEquals(reaches, FogRules.reaches(blinding, clearSight));
  }
}
