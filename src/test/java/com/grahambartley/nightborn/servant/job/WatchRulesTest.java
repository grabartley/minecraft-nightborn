package com.grahambartley.nightborn.servant.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WatchRulesTest {

  @ParameterizedTest(name = "sees {0}, saw before {1} -> warns {2}")
  @CsvSource({
    "true, false, true",
    "true, true, false",
    "false, true, false",
    "false, false, false"
  })
  @DisplayName("a watch warns once as hunters come, not on every look")
  void warns(final boolean sees, final boolean sawBefore, final boolean warns) {
    assertEquals(warns, WatchRules.warns(sees, sawBefore));
  }

  @ParameterizedTest(name = "monster {0}, hunter {1}, spared {2} -> {3}")
  @CsvSource({
    "true, false, false, true",
    "true, false, true, false",
    "false, true, false, true",
    "false, false, false, false",
    "false, false, true, false"
  })
  @DisplayName("a watch drives off hunters and monsters, but leaves creepers and vampires be")
  void intrudes(
      final boolean monster, final boolean hunter, final boolean spared, final boolean intrudes) {
    assertEquals(intrudes, WatchRules.intrudes(monster, hunter, spared));
  }

  @ParameterizedTest(name = "{0} squared away, range {1} -> {2}")
  @CsvSource({"0, 16, true", "256, 16, true", "257, 16, false"})
  @DisplayName("a watch looks only as far as its range")
  void watched(final double distanceSquared, final int range, final boolean watched) {
    assertEquals(watched, WatchRules.watched(distanceSquared, range));
  }

  @ParameterizedTest(name = "creeper {0}, vampire {1} -> {2}")
  @CsvSource({"true, false, true", "false, true, true", "false, false, false"})
  @DisplayName("a guard leaves creepers and vampires be, on watch and at the coffin alike")
  void spared(final boolean creeper, final boolean vampire, final boolean spared) {
    assertEquals(spared, WatchRules.spared(creeper, vampire));
  }
}
