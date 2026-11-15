package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.BloodSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StarvationTest {

  private static final BloodSettings SETTINGS =
      new BloodSettings(30, 50, 100, 150, 60, 60, 10, 300, 10, 4);

  @ParameterizedTest(name = "blood {1}: {0} -> {2}")
  @CsvSource({"0, 0, 1", "40, 0, 41", "40, 5, 40", "40, 19, 40", "40, 20, 0"})
  @DisplayName("starving grows only on an empty bar, holds while refilling and ends when full")
  void ticks(final int before, final int blood, final int after) {
    assertEquals(after, Starvation.tick(before, blood, 40.0F, false, SETTINGS).ticks());
  }

  @ParameterizedTest(name = "{0} ticks starving: {1}")
  @CsvSource({"0, 0", "1, 10", "1199, 10", "1200, 20", "6000, 60", "10800, 100", "999999, 100"})
  @DisplayName("each step of starving weakens one step more, up to the cap")
  void strength(final int ticks, final int strength) {
    assertEquals(strength, Starvation.strength(ticks, SETTINGS));
  }

  @ParameterizedTest(name = "blood {0}: weakness {1}")
  @CsvSource({"0, 40", "5, 30", "10, 20", "19, 2"})
  @DisplayName("the weakness fades as the bar refills")
  void fades(final int blood, final int weakness) {
    assertEquals(weakness, Starvation.tick(4000, blood, 40.0F, false, SETTINGS).weakness());
  }

  @ParameterizedTest(name = "{0} ticks, health {1}, peaceful {2}: hurt {3}")
  @CsvSource({
    "5999, 20, false, 0",
    "6200, 20, false, 1",
    "6400, 20, false, 1",
    "6401, 20, false, 0",
    "6200, 1.5, false, 0.5",
    "6200, 1, false, 0",
    "6200, 20, true, 0"
  })
  @DisplayName("after the delay starving hurts slowly, never below half a heart, never in peaceful")
  void hurts(final int ticks, final float health, final boolean peaceful, final float hurt) {
    assertEquals(hurt, Starvation.tick(ticks - 1, 0, health, peaceful, SETTINGS).hurt(), 0.0001F);
  }

  @ParameterizedTest(name = "weakness {0}: night gifts {1}, bare hands {2}")
  @CsvSource({"0, 1, 1", "25, 0.5, 0.875", "50, 0, 0.75", "100, 0, 0.5"})
  @DisplayName("night speed and jump fade then stop, and bare-hand damage shrinks")
  void giftsFade(final int weakness, final double night, final double hands) {
    assertEquals(night, Starvation.nightGiftShare(weakness), 0.0001);
    assertEquals(hands, Starvation.bareHandShare(weakness), 0.0001);
  }

  @Test
  @DisplayName("starving without any blood never hurts when the bar has blood")
  void noHurtWithBlood() {
    assertEquals(0.0F, Starvation.tick(6199, 1, 20.0F, false, SETTINGS).hurt(), 0.0001F);
  }
}
