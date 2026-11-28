package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.skill.PowerUse;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

  @ParameterizedTest(name = "tick {0}")
  @ValueSource(longs = {0, 100, 24000})
  @DisplayName("a caster that has taken no form holds none and is headed nowhere")
  void holdsNothingAtFirst(final long now) {
    final PowerCasting casting = new PowerCasting();
    assertFalse(casting.hold().holds(now));
    assertEquals(Optional.empty(), casting.hold().course());
  }

  @ParameterizedTest(name = "now {0}, next {1}, think {2}, price {3}, chains {4}: next {5}")
  @CsvSource({
    "100, 100, 20, 0, false, 120",
    "100, 150, 20, 0, false, 150",
    "100, 100, 20, 40, false, 160",
    "100, 150, 20, 40, false, 190",
    "100, 100, 20, 40, true, 100",
    "100, 150, 20, 0, true, 100"
  })
  @DisplayName(
      "after a cast a mind pauses for its think and the intent's price, and one that chains"
          + " thinks again the moment the power lands")
  void afterCast(
      final long now,
      final long nextThink,
      final int thinkTicks,
      final int price,
      final boolean chains,
      final long next) {
    assertEquals(next, PowerCasting.afterCast(now, nextThink, thinkTicks, price, chains));
  }
}
