package com.grahambartley.nightborn.downed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.nightborn.config.DownedSettings;
import com.grahambartley.nightborn.downed.DownedState.Phase;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DownedRulesTest {

  private static final DownedSettings SETTINGS = new DownedSettings(30, 60, 20, false);

  @ParameterizedTest(name = "state {0}, killer {1}: hurts {2}")
  @CsvSource({
    "STANDING, false, true",
    "STANDING, true, true",
    "DOWNED, false, false",
    "DOWNED, true, true",
    "ESCAPING, false, false",
    "ESCAPING, true, true"
  })
  @DisplayName("while downed or escaping only sun and a stake hurt")
  void damage(final String state, final boolean killer, final boolean hurts) {
    final DownedState current =
        state.equals("STANDING") ? null : new DownedState(Phase.valueOf(state), 10);
    assertEquals(hurts, DownedRules.allowsDamage(current, killer));
  }

  @ParameterizedTest(name = "killer {0}: downed {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("anything but a killer downs instead of killing")
  void downsInsteadOfDying(final boolean killer, final boolean downs) {
    assertEquals(downs, DownedRules.downsInsteadOfDying(killer));
  }

  @ParameterizedTest(name = "state {0}: helpless {1}, flees {2}")
  @CsvSource({"STANDING, false, false", "DOWNED, true, false", "ESCAPING, false, true"})
  @DisplayName("a downed vampire lies helpless and an escaping one flees")
  void behaviour(final String state, final boolean helpless, final boolean flees) {
    final DownedState current =
        state.equals("STANDING") ? null : new DownedState(Phase.valueOf(state), 20);
    assertEquals(helpless, DownedRules.helpless(current));
    assertEquals(flees, DownedRules.flees(current));
  }

  @ParameterizedTest(name = "helpless {0}, mob {1}, hunter {2}: spared {3}")
  @CsvSource({
    "true, true, false, true",
    "true, true, true, false",
    "true, false, false, false",
    "true, false, true, false",
    "false, true, false, false",
    "false, true, true, false",
    "false, false, false, false",
    "false, false, true, false"
  })
  @DisplayName("only a mob that is no hunter spares a helpless vampire")
  void spares(
      final boolean helpless, final boolean mob, final boolean hunter, final boolean spared) {
    assertEquals(spared, DownedRules.spares(helpless, mob, hunter));
  }

  @ParameterizedTest(name = "state {0}, stake blow {1}: staked {2}")
  @CsvSource({
    "STANDING, true, false",
    "DOWNED, true, true",
    "DOWNED, false, false",
    "ESCAPING, true, false"
  })
  @DisplayName("a stake blow stakes a downed vampire outright")
  void stakesOnHit(final String state, final boolean stakeBlow, final boolean staked) {
    final DownedState current =
        state.equals("STANDING") ? null : new DownedState(Phase.valueOf(state), 10);
    assertEquals(staked, DownedRules.stakesOnHit(current, stakeBlow));
  }

  @Test
  @DisplayName("a vampire lies downed for the set time, gets up, escapes, then is free")
  void fullCycle() {
    DownedState state = DownedRules.downed(SETTINGS, Optional.empty());
    assertEquals(new DownedState(Phase.DOWNED, 600), state);
    DownedRules.Change change = DownedRules.Change.NONE;
    int ticks = 0;
    while (change != DownedRules.Change.STOOD_UP) {
      final DownedRules.Step step = DownedRules.tick(state, SETTINGS);
      state = step.state();
      change = step.change();
      ticks++;
    }
    assertEquals(600, ticks);
    assertEquals(new DownedState(Phase.ESCAPING, 1200), state);
    ticks = 0;
    DownedRules.Step step;
    do {
      step = DownedRules.tick(state, SETTINGS);
      state = step.state();
      ticks++;
    } while (step.change() != DownedRules.Change.ESCAPED);
    assertEquals(1200, ticks);
    assertNull(state);
  }

  @ParameterizedTest(name = "max {0} at {1}%: {2}")
  @CsvSource({"40, 20, 8", "20, 20, 4", "40, 1, 1", "2, 20, 1", "40, 100, 40"})
  @DisplayName("a vampire gets up with its share of health, never less than one")
  void recovery(final float max, final int percent, final float health) {
    assertEquals(
        health,
        DownedRules.recoveredHealth(max, new DownedSettings(30, 60, percent, false)),
        0.0001F);
  }

  @ParameterizedTest(name = "human attacker {0}: remembered {1}")
  @CsvSource({"true, true", "false, false"})
  @DisplayName("only a human player who downs a vampire is remembered as its hunter")
  void hunter(final boolean human, final boolean remembered) {
    final UUID attacker = UUID.fromString("00000000-0000-0000-0000-000000000047");
    assertEquals(
        remembered ? Optional.of(attacker) : Optional.empty(), DownedRules.hunter(human, attacker));
  }

  @Test
  @DisplayName("the hunter who downed a vampire is remembered while it lies and escapes")
  void remembersTheHunter() {
    final Optional<UUID> hunter =
        Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000047"));
    DownedState state = DownedRules.downed(SETTINGS, hunter);
    assertEquals(new DownedState(Phase.DOWNED, 600, hunter), state);
    while (state != null && state.isDowned()) {
      state = DownedRules.tick(state, SETTINGS).state();
    }
    assertEquals(new DownedState(Phase.ESCAPING, 1200, hunter), state);
    assertEquals(hunter, DownedRules.tick(state, SETTINGS).state().downedBy());
  }
}
