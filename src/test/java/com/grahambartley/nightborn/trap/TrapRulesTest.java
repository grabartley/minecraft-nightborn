package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrapRulesTest {

  @ParameterizedTest(
      name =
          "vampire {0}, bat {1}, mist {2}, catches those {3}, reaches {4}, player {5},"
              + " spared {6}, stepped {7} -> {8}")
  @CsvSource({
    "true, false, false, false, true, false, false, true, CATCH",
    "true, true, false, false, true, false, false, true, NOTHING",
    "true, false, true, false, true, false, false, true, NOTHING",
    "true, true, false, true, true, false, false, false, CATCH",
    "true, false, false, true, false, true, false, false, NOTHING",
    "true, false, false, false, false, true, false, true, NOTHING",
    "false, false, false, false, false, true, false, true, SPRING",
    "false, false, false, false, false, true, true, true, NOTHING",
    "false, false, false, true, false, true, false, false, NOTHING",
    "false, false, false, false, false, false, false, true, NOTHING"
  })
  @DisplayName(
      "a trap catches a vampire it may reach, a snare misses bats and mist, and a player's foot"
          + " springs a snare unless they are spared")
  void meets(
      final boolean vampire,
      final boolean bat,
      final boolean mist,
      final boolean catchesBatsAndMist,
      final boolean reaches,
      final boolean player,
      final boolean spared,
      final boolean stepped,
      final TrapRules.Outcome expected) {
    assertEquals(
        expected,
        TrapRules.meets(vampire, bat, mist, catchesBatsAndMist, reaches, player, spared, stepped));
  }

  @ParameterizedTest(name = "stepped {0}, radius {1}, distance² {2}, on block {3} -> {4}")
  @CsvSource({
    "true, 0, 0.1, true, true",
    "true, 0, 0.1, false, false",
    "false, 3, 9.0, false, true",
    "false, 3, 9.5, true, false"
  })
  @DisplayName("a snare is met underfoot and a flash trap anywhere within its radius")
  void within(
      final boolean stepped,
      final float radius,
      final double distance,
      final boolean onBlock,
      final boolean expected) {
    assertEquals(expected, TrapRules.within(stepped, radius, distance, onBlock));
  }

  @ParameterizedTest(name = "{0} set, limit {1} -> clear {2}")
  @CsvSource({"0, 2, 0", "1, 2, 0", "2, 2, 1", "4, 2, 3", "1, 1, 1"})
  @DisplayName("setting a trap past the limit clears the oldest to make room")
  void overLimit(final int set, final int limit, final int cleared) {
    assertEquals(cleared, TrapRules.overLimit(set, limit));
  }

  @ParameterizedTest(name = "sense {0}, self {1}, ally {2} -> spared {3}")
  @CsvSource({
    "true, true, false, true",
    "true, false, true, true",
    "true, false, false, false",
    "false, true, true, false"
  })
  @DisplayName("Trap Sense spares a hunter their own and their allies' traps")
  void spares(final boolean sense, final boolean self, final boolean ally, final boolean spared) {
    assertEquals(spared, TrapRules.spares(sense, self, ally));
  }

  @ParameterizedTest(name = "same {0}, hunters {1}/{2}, team {3} -> allied {4}")
  @CsvSource({
    "true, false, false, false, true",
    "false, true, true, false, true",
    "false, true, false, false, false",
    "false, false, false, true, true",
    "false, false, true, false, false"
  })
  @DisplayName("a hunter's allies are themself, other hunters and their team")
  void allied(
      final boolean same,
      final boolean ownerHunter,
      final boolean otherHunter,
      final boolean team,
      final boolean allied) {
    assertEquals(allied, TrapRules.allied(same, ownerHunter, otherHunter, team));
  }

  @ParameterizedTest(name = "vampire {0}, distance² {1}, seen within {2} -> seen {3}")
  @CsvSource({
    "false, 400, 2, true",
    "true, 4, 2, true",
    "true, 4.1, 2, false",
    "true, 36, 6, true",
    "true, 37, 6, false"
  })
  @DisplayName("a vampire is sent a hunter's trap only from close by, and anyone else always is")
  void seen(
      final boolean vampire, final double distance, final float seenWithin, final boolean seen) {
    assertEquals(seen, TrapRules.seen(vampire, distance, seenWithin));
  }
}
