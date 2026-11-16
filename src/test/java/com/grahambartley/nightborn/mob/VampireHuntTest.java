package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class VampireHuntTest {

  @ParameterizedTest(
      name = "downed {0}, asleep {1}, prey {2}, spared {3}, drops pursuit {4}: hunts {5}")
  @CsvSource({
    "false, false, true, false, false, true",
    "true, false, true, false, false, false",
    "false, true, true, false, false, false",
    "false, false, false, false, false, false",
    "false, false, true, true, false, false",
    "false, false, true, false, true, false"
  })
  @DisplayName("a vampire hunts only standing, awake prey it is not sparing and would chase")
  void hunts(
      final boolean downed,
      final boolean sleeping,
      final boolean prey,
      final boolean spared,
      final boolean dropsPursuit,
      final boolean hunts) {
    assertEquals(hunts, VampireHunt.hunts(downed, sleeping, prey, spared, () -> dropsPursuit));
  }

  @ParameterizedTest(name = "downed {0}, asleep {1}, prey {2}, spared {3}")
  @CsvSource({
    "true, false, true, false",
    "false, true, true, false",
    "false, false, false, false",
    "false, false, true, true"
  })
  @DisplayName("the pursuit is only weighed once every cheaper check has passed")
  void huntsWeighsThePursuitLast(
      final boolean downed, final boolean sleeping, final boolean prey, final boolean spared) {
    final AtomicBoolean weighed = new AtomicBoolean();
    VampireHunt.hunts(
        downed,
        sleeping,
        prey,
        spared,
        () -> {
          weighed.set(true);
          return false;
        });
    assertFalse(weighed.get());
  }

  @ParameterizedTest(name = "downed {0}, drops pursuit {1}: lets go {2}")
  @CsvSource({"false, false, false", "true, false, true", "false, true, true", "true, true, true"})
  @DisplayName("a vampire lets its target go when downed or when the pursuit is over")
  void letsGo(final boolean downed, final boolean dropsPursuit, final boolean letsGo) {
    assertEquals(letsGo, VampireHunt.letsGo(downed, () -> dropsPursuit));
  }

  @ParameterizedTest(name = "attacker is a vampire {0}: grudge {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("a vampire holds a grudge against any attacker but another vampire")
  void bearsGrudge(final boolean attackerIsVampire, final boolean grudge) {
    assertEquals(grudge, VampireHunt.bearsGrudge(attackerIsVampire));
  }
}
