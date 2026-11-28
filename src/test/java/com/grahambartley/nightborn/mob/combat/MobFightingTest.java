package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.mob.brain.Disposition;
import com.grahambartley.nightborn.mob.brain.Mind;
import com.grahambartley.nightborn.mob.brain.Sharpness;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.Identifier;
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

  @ParameterizedTest(name = "{0} ticks")
  @ValueSource(ints = {1, 3})
  @DisplayName("out of a fight a mob rests its moves, does not move, and keeps no tell")
  void restsOutOfAFight(final int ticks) {
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
    final MobFighting fighting = new MobFighting(new PowerCasting(), moves);
    for (int tick = 0; tick < ticks; tick++) {
      fighting.tick(
          null,
          MobFighting.Engagement.NONE,
          () -> new Mind(40, Sharpness.KEEN, Disposition.EVEN),
          0);
    }
    assertEquals(0, moved.get());
    assertEquals(ticks, rested.get());
    assertFalse(fighting.telling());
    assertEquals(Optional.empty(), fighting.lastThink());
    assertFalse(fighting.hold().holds(0));
    assertEquals(Optional.empty(), fighting.hold().course());
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"nightborn:leap", "nightborn:holy_water", "nightborn:vanish"})
  @DisplayName("a mob with no moves of its own offers the brain nothing and does nothing asked")
  void noMoves(final String action) {
    assertEquals(List.of(), MobFighting.Moves.NONE.actions());
    assertFalse(MobFighting.Moves.NONE.act(Identifier.of(action)));
    assertFalse(MobFighting.Moves.NONE.reacting());
    assertFalse(MobFighting.Moves.NONE.busy());
  }

  @ParameterizedTest(name = "{0} ticks")
  @ValueSource(ints = {1, 4})
  @DisplayName(
      "while a move of its own is under way a mob keeps that move going and never thinks of a"
          + " power at the same moment")
  void busyMovesKeepTheBrainQuiet(final int ticks) {
    final AtomicInteger moved = new AtomicInteger();
    final MobFighting.Moves moves =
        new MobFighting.Moves() {
          @Override
          public void tick() {
            moved.incrementAndGet();
          }

          @Override
          public boolean busy() {
            return true;
          }
        };
    final MobFighting fighting = new MobFighting(new PowerCasting(), moves);
    for (int tick = 0; tick < ticks; tick++) {
      fighting.tick(
          null,
          MobFighting.Engagement.FIGHTING,
          () -> {
            throw new AssertionError("a busy mob does not think");
          },
          0);
    }
    assertEquals(ticks, moved.get());
    assertFalse(fighting.telling());
    assertEquals(Optional.empty(), fighting.lastThink());
  }
}
