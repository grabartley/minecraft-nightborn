package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import net.minecraft.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GrudgeTest {

  @ParameterizedTest(name = "attacker is a vampire {0}: fights back {1}")
  @CsvSource({"false, true", "true, false"})
  @DisplayName("a vampire fights back against anyone but another vampire")
  void fightsBack(final boolean vampire, final boolean fightsBack) {
    assertEquals(fightsBack, Grudge.fightsBack(vampire));
  }

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
          + " when the attacker dies, turns creative or spectator, gets out of reach, or the"
          + " vampire is downed")
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

  @ParameterizedTest(name = "hit by a vampire {0}: held {1}, against attacker {2}")
  @CsvSource({"false, true, true", "true, false, false"})
  @DisplayName("being hit sets a grudge against that attacker unless the attacker is a vampire")
  void provoke(final boolean vampire, final boolean held, final boolean againstAttacker) {
    final LivingEntity attacker = mock(LivingEntity.class);
    final LivingEntity bystander = mock(LivingEntity.class);
    final Grudge grudge = new Grudge();
    grudge.provoke(attacker, vampire);
    assertEquals(held, grudge.held());
    assertEquals(againstAttacker, grudge.against(attacker));
    assertEquals(false, grudge.against(bystander));
    assertEquals(false, grudge.against(null));
  }
}
