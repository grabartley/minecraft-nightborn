package com.grahambartley.nightborn.skill;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MendTest {

  @ParameterizedTest(name = "{0} health over {1} ticks")
  @CsvSource({"6, 60", "1, 1", "10, 3", "0, 20"})
  @DisplayName("a mend heals its whole amount in equal steps and then ends")
  void healsItsWholeAmount(final float health, final int ticks) {
    Mend mend = new HealPower(health, ticks).mend();
    float healed = 0.0F;
    int steps = 0;
    while (mend != null) {
      healed += mend.perTick();
      steps++;
      mend = mend.next(false);
    }
    assertEquals(ticks, steps);
    assertEquals(health, healed, 0.001F);
  }

  @ParameterizedTest(name = "{0} ticks left, helpless {1}: carries on {2}")
  @CsvSource({"5, false, true", "5, true, false", "1, false, false", "1, true, false"})
  @DisplayName("a mend stops at once when its caster is downed or dead")
  void stopsWhenHelpless(final int ticksLeft, final boolean helpless, final boolean carriesOn) {
    assertEquals(carriesOn, new Mend(0.1F, ticksLeft).next(helpless) != null);
  }
}
