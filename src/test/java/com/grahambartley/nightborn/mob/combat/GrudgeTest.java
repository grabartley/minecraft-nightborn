package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GrudgeTest {

  @ParameterizedTest(
      name = "present {0}, engaged {1}, downed {2}, {3} blocks, follow {4}: holds {5}")
  @CsvSource({
    "true, true, false, 3, 32, true",
    "true, true, false, 32, 32, true",
    "true, true, false, 32.5, 32, false",
    "false, true, false, 3, 32, false",
    "true, false, false, 3, 32, false",
    "true, true, true, 3, 32, false"
  })
  @DisplayName(
      "a grudge lasts while the vampire fights its attacker or was just hit by it, and ends"
          + " when the attacker dies, leaves the world, turns creative or spectator, gets out of"
          + " reach, or the vampire is downed")
  void holds(
      final boolean present,
      final boolean engaged,
      final boolean downed,
      final double distance,
      final double followRange,
      final boolean holds) {
    assertEquals(holds, Grudge.holds(present, engaged, downed, distance, followRange));
  }

  @ParameterizedTest(name = "target is the attacker {0}, prey {1}: provoked {2}")
  @CsvSource({"true, true, true", "true, false, true", "false, false, true", "false, true, false"})
  @DisplayName("a chase is provoked when it is against the attacker or the target is not prey")
  void provoked(final boolean againstTarget, final boolean prey, final boolean provoked) {
    assertEquals(provoked, Grudge.provoked(againstTarget, prey));
  }

  @ParameterizedTest(name = "provoked {0}, forgive the attacker {1}: held {2}")
  @CsvSource({"false, false, false", "true, false, true", "true, true, false"})
  @DisplayName(
      "a grudge is held against the attacker until that attacker, and nobody else, is forgiven")
  void provokeAndForgive(final boolean provoke, final boolean forgiveAttacker, final boolean held) {
    final LivingEntity attacker = mock(LivingEntity.class);
    final LivingEntity bystander = mock(LivingEntity.class);
    final Grudge grudge = new Grudge();
    if (provoke) {
      grudge.provoke(attacker);
    }
    grudge.forgive(bystander);
    grudge.forgive(null);
    if (forgiveAttacker) {
      grudge.forgive(attacker);
    }
    assertEquals(held, grudge.held());
    assertEquals(held, grudge.against(attacker));
    assertEquals(false, grudge.against(bystander));
    assertEquals(false, grudge.against(null));
  }
}
