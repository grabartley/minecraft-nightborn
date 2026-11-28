package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.skill.PowerUse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PowerCastingTest {

  @ParameterizedTest(name = "engaged {0}, now {1}, next think {2}, next aid {3} -> thinks {4}")
  @CsvSource({
    "true, 100, 100, 500, true",
    "true, 101, 100, 500, true",
    "true, 99, 100, 0, false",
    "false, 100, 500, 100, true",
    "false, 99, 0, 100, false",
    "false, 500, 100, 600, false"
  })
  @DisplayName(
      "a caster fighting or escaping thinks once its pause is over, and one standing by thinks"
          + " to help an ally on its own pause")
  void thinksAfterItsPause(
      final boolean engaged,
      final long now,
      final long next,
      final long nextAid,
      final boolean thinks) {
    assertEquals(thinks, PowerCasting.thinks(engaged, now, next, nextAid));
  }

  @ParameterizedTest(name = "aims at an ally {0}, verdict on the ally {1}: tells {2}")
  @CsvSource({"false, '', true", "true, READY, true", "true, NO_WEAPON, false", "true, '', false"})
  @DisplayName(
      "a mob winds up to help an ally only when the power is ready for that very ally, such as a"
          + " blessing for an armed one")
  void tellable(final boolean ally, final String verdict, final boolean tells) {
    assertEquals(
        tells,
        PowerCasting.tellable(ally, verdict.isEmpty() ? null : PowerUse.Verdict.valueOf(verdict)));
  }
}
