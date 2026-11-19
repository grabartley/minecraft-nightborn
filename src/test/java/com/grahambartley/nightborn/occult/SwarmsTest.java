package com.grahambartley.nightborn.occult;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SwarmsTest {

  @ParameterizedTest(name = "hunter {0}, fought {1}, reachable {2}: bitten {3}")
  @CsvSource({
    "true, false, true, true",
    "false, true, true, true",
    "false, false, true, false",
    "true, true, false, false"
  })
  @DisplayName("the swarm bites hunters and anyone the vampire has fought, where it may")
  void bites(final boolean hunter, final boolean fought, final boolean reach, final boolean bit) {
    assertEquals(bit, Swarms.bites(hunter, fought, reach));
  }

  @ParameterizedTest(name = "caster alive {0}, time left {1}: ends {2}")
  @CsvSource({"true, true, false", "false, true, true", "true, false, true", "false, false, true"})
  @DisplayName("the swarm ends when its caster dies or its time is up")
  void ends(final boolean alive, final boolean lasts, final boolean ends) {
    assertEquals(ends, Swarms.ends(alive, lasts));
  }
}
