package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.downed.DownedState;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FightRulesTest {

  @ParameterizedTest(name = "a {0} tick downed timer becomes {1}")
  @CsvSource({"600, 300", "40, 300", "300, 300"})
  @DisplayName("a downed boss gets the stake window, whatever the usual downed time is")
  void window(final int ticksLeft, final int window) {
    final Optional<UUID> hunter = Optional.of(new UUID(1L, 2L));
    final DownedState shortened =
        new FightRules(100, window, 30)
            .window(new DownedState(DownedState.Phase.DOWNED, ticksLeft, hunter));
    assertEquals(new DownedState(DownedState.Phase.DOWNED, window, hunter), shortened);
  }

  @ParameterizedTest(name = "{0} max health, last share {1}, rising at {2}%: {3}")
  @CsvSource({
    "200, 0.35, 30, 21.0",
    "200, 0.35, 100, 70.0",
    "200, 1.0, 50, 100.0",
    "10, 0.35, 1, 1.0",
    "0, 0.35, 30, 1.0"
  })
  @DisplayName("a boss nobody staked rises with a share of its last phase, never below one point")
  void risen(final float max, final double share, final int percent, final float health) {
    assertEquals(health, new FightRules(100, 300, percent).risen(max, share), 1.0E-4F);
  }

  @ParameterizedTest(name = "target {0}, phase {1}, hurt {2}, downed {3}: {4}")
  @CsvSource({
    "false, 0, false, false, false",
    "true, 0, false, false, true",
    "false, 1, false, false, true",
    "false, 0, true, false, true",
    "false, 0, false, true, true"
  })
  @DisplayName("a fight is on once the boss has a target, a wound, a later phase or is downed")
  void engaged(
      final boolean target,
      final int phase,
      final boolean hurt,
      final boolean downed,
      final boolean engaged) {
    assertEquals(engaged, FightRules.engaged(target, phase, hurt, downed));
  }

  @ParameterizedTest(name = "engaged {0}, empty for {1} ticks, downed {2}: resets {3}")
  @CsvSource({
    "true, 100, false, true",
    "true, 5000000, false, true",
    "true, 99, false, false",
    "true, 0, false, false",
    "false, 100, false, false",
    "true, 100, true, false"
  })
  @DisplayName(
      "an arena empty for a hundred ticks resets a fight that is on, however long the players were"
          + " away, but not while the boss lies downed")
  void resets(
      final boolean engaged, final long emptyFor, final boolean downed, final boolean resets) {
    assertEquals(resets, new FightRules(100, 300, 30).resets(engaged, emptyFor, downed));
  }

  @ParameterizedTest(name = "empty for {0} ticks")
  @CsvSource({"0", "100", "5000000"})
  @DisplayName("a boss with no empty-arena rule never resets its fight")
  void neverResets(final long emptyFor) {
    assertEquals(false, new FightRules(0, 300, 30).resets(true, emptyFor, false));
  }
}
