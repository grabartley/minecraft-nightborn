package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MobFightingTest {

  @ParameterizedTest(name = "target {0}, downed {1}, asleep {2}, retreating {3}, weak {4}: {5}")
  @CsvSource({
    "true, false, false, false, false, true",
    "false, false, false, false, false, false",
    "true, true, false, false, false, false",
    "true, false, true, false, false, false",
    "true, false, false, true, false, false",
    "true, false, false, false, true, false"
  })
  @DisplayName(
      "a vampire casts and moves only with a target, and never while downed, asleep,"
          + " falling back or weakened")
  void fights(
      final boolean target,
      final boolean downed,
      final boolean asleep,
      final boolean retreating,
      final boolean weakened,
      final boolean fights) {
    assertEquals(fights, MobFighting.fights(target, downed, asleep, retreating, weakened));
  }

  @Test
  @DisplayName("out of a fight a mob rests its moves, does not move, and keeps no tell")
  void restsOutOfAFight() {
    final AtomicInteger moved = new AtomicInteger();
    final AtomicInteger rested = new AtomicInteger();
    final MobFighting fighting =
        new MobFighting(
            new PowerCasting(),
            new MobFighting.Moves() {
              @Override
              public void tick() {
                moved.incrementAndGet();
              }

              @Override
              public void rest() {
                rested.incrementAndGet();
              }
            });
    fighting.tick(null, false, 40, 0);
    assertEquals(0, moved.get());
    assertEquals(1, rested.get());
    assertFalse(fighting.telling());
    assertEquals(Optional.empty(), fighting.lastThink());
  }
}
