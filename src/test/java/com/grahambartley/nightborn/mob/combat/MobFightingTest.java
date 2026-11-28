package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class MobFightingTest {

  @ParameterizedTest(name = "target {0}, downed {1}, asleep {2}, escaping {3}, weak {4}: {5}")
  @CsvSource({
    "true, false, false, false, false, FIGHTING",
    "false, false, false, false, false, STANDING_BY",
    "true, true, false, false, false, NONE",
    "true, false, true, false, false, NONE",
    "true, false, false, true, false, ESCAPING",
    "false, false, false, true, false, ESCAPING",
    "true, false, false, false, true, NONE",
    "false, false, false, true, true, NONE",
    "true, true, false, true, false, NONE"
  })
  @DisplayName(
      "a vampire fights only with a target, escapes while falling back or seeking shade with or"
          + " without one, stands by without one, and does none of these while downed, asleep or"
          + " weakened")
  void engagement(
      final boolean target,
      final boolean downed,
      final boolean asleep,
      final boolean escaping,
      final boolean weakened,
      final MobFighting.Engagement engagement) {
    assertEquals(engagement, MobFighting.engagement(target, downed, asleep, escaping, weakened));
  }

  @ParameterizedTest(name = "moves first {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("out of a fight a mob rests its moves, does not move, and keeps no tell")
  void restsOutOfAFight(final boolean movesFirst) {
    final AtomicInteger moved = new AtomicInteger();
    final AtomicInteger rested = new AtomicInteger();
    final MobFighting.Moves moves =
        new MobFighting.Moves() {
          @Override
          public void tick() {
            moved.incrementAndGet();
          }

          @Override
          public void rest() {
            rested.incrementAndGet();
          }
        };
    final MobFighting fighting =
        movesFirst
            ? MobFighting.movingFirst(new PowerCasting(), moves)
            : MobFighting.castingFirst(new PowerCasting(), moves);
    fighting.tick(null, MobFighting.Engagement.NONE, 40, 0);
    assertEquals(0, moved.get());
    assertEquals(1, rested.get());
    assertFalse(fighting.telling());
    assertEquals(Optional.empty(), fighting.lastThink());
    assertFalse(fighting.hold().holds(0));
    assertEquals(Optional.empty(), fighting.hold().course());
  }
}
