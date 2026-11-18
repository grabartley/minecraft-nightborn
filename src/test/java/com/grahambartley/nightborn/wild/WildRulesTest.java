package com.grahambartley.nightborn.wild;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WildRulesTest {

  @ParameterizedTest(name = "roll {0} against {1}%: {2}")
  @CsvSource({"0, 2, true", "1, 2, true", "2, 2, false", "99, 100, true", "0, 0, false"})
  @DisplayName("a roll from 0 to 99 succeeds below the chance")
  void rolls(final int roll, final int chance, final boolean rolls) {
    assertEquals(rolls, WildRules.rolls(roll, chance));
  }

  @ParameterizedTest(name = "spot at {0} from players at 0 and 300: {1}")
  @CsvSource({"128, true", "200, false", "127, false", "-200, true", "172, true"})
  @DisplayName("a wild vampire appears only at least the distance from every player")
  void farFromEvery(final int x, final boolean far) {
    assertEquals(
        far,
        WildRules.farFromEvery(
            new BlockPos(x, 64, 0),
            List.of(new BlockPos(0, 64, 0), new BlockPos(300, 70, 0)),
            128));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "far at night, true, 400, 12, true, false, false",
    "player came within reach, true, 144, 12, true, false, true",
    "player gone, false, 0, 12, true, false, true",
    "dawn came, true, 400, 12, false, false, true",
    "struck, true, 400, 12, true, true, true"
  })
  @DisplayName("the watcher leaves when approached, struck, at dawn or when its player is gone")
  void watcherLeaves(
      final String name,
      final boolean present,
      final double distanceSquared,
      final int leave,
      final boolean night,
      final boolean struck,
      final boolean leaves) {
    assertEquals(leaves, WildRules.watcherLeaves(present, distanceSquared, leave, night, struck));
  }

  @ParameterizedTest(name = "{0} blocks squared, keeping {1}: {2}")
  @CsvSource({"575, 24, true", "576, 24, false", "900, 24, false"})
  @DisplayName("the watcher steps back only when a player is nearer than it keeps")
  void backsOff(final double distanceSquared, final int keep, final boolean backs) {
    assertEquals(backs, WildRules.backsOff(distanceSquared, keep));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "new human by day, false, true, false, false, true, 20, true",
    "party already met, true, true, false, false, true, 20, false",
    "no longer new, false, false, false, false, true, 20, false",
    "at night, false, true, true, false, true, 20, false",
    "a vampire, false, true, false, true, true, 20, false",
    "party spawning off, false, true, false, false, false, 20, false",
    "party chance set to 0, false, true, false, false, true, 0, false"
  })
  @DisplayName("a new human meets one hunter party by day, while parties are on")
  void meetsParty(
      final String name,
      final boolean partyMet,
      final boolean isNew,
      final boolean night,
      final boolean vampire,
      final boolean spawnsParties,
      final int partyChance,
      final boolean meets) {
    assertEquals(
        meets, WildRules.meetsParty(partyMet, isNew, night, vampire, spawnsParties, partyChance));
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({
    "alive and near, true, 100, false, true",
    "at the edge of reach, true, 256, false, true",
    "too far, true, 257, false, false",
    "master dead, false, 100, false, false",
    "player turned, true, 100, true, false"
  })
  @DisplayName("the guild master greets a human player who comes within reach")
  void greets(
      final String name,
      final boolean alive,
      final double distanceSquared,
      final boolean vampire,
      final boolean greets) {
    assertEquals(greets, WildRules.greets(alive, distanceSquared, vampire));
  }
}
