package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class BloodMetabolismTest {

  private static BloodMetabolism.State state(
      final int blood,
      final float exhaustion,
      final float health,
      final Difficulty difficulty,
      final int age) {
    return new BloodMetabolism.State(blood, exhaustion, health, 40.0F, difficulty, true, age);
  }

  @ParameterizedTest(name = "drain every {0} s")
  @CsvSource({"5", "30", "600"})
  @DisplayName("at rest a vampire loses one blood every configured number of seconds")
  void restingDrain(final int seconds) {
    assertEquals(20, bloodAfterResting(seconds * 20 - 1, seconds));
    assertEquals(19, bloodAfterResting(seconds * 20 + 1, seconds));
  }

  private static int bloodAfterResting(final int ticks, final int seconds) {
    int blood = 20;
    float exhaustion = 0.0F;
    for (int tick = 1; tick <= ticks; tick++) {
      final BloodMetabolism.Outcome outcome =
          BloodMetabolism.tick(
              new BloodMetabolism.State(
                  blood, exhaustion, 40.0F, 40.0F, Difficulty.NORMAL, true, tick),
              seconds);
      blood = outcome.blood();
      exhaustion = outcome.exhaustion();
    }
    return blood;
  }

  @Test
  @DisplayName("exhaustion from running and fighting costs blood as it costs hunger")
  void exhaustionCostsBlood() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(10, 4.0F, 40.0F, Difficulty.NORMAL, 1), 600);
    assertEquals(9, outcome.blood());
  }

  @Test
  @DisplayName("blood never drains in peaceful")
  void peacefulKeepsBlood() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(10, 4.5F, 40.0F, Difficulty.PEACEFUL, 1), 5);
    assertEquals(10, outcome.blood());
  }

  @ParameterizedTest(name = "blood {0}, health {1}, age {2}: heals {3}")
  @CsvSource({
    "20, 30, 80, true",
    "18, 30, 160, true",
    "17, 30, 80, false",
    "20, 40, 80, false",
    "20, 30, 81, false"
  })
  @DisplayName("a near-full blood bar heals every 80 ticks while hurt")
  void heals(final int blood, final float health, final int age, final boolean heals) {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(blood, 0.0F, health, Difficulty.NORMAL, age), 600);
    assertEquals(heals, outcome.heals());
  }

  @Test
  @DisplayName("healing costs exhaustion, as vanilla hunger does")
  void healingCostsExhaustion() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(20, 0.0F, 30.0F, Difficulty.PEACEFUL, 80), 600);
    assertEquals(6.0F, outcome.exhaustion(), 0.0001F);
  }

  @Test
  @DisplayName("natural regeneration off stops blood healing")
  void noRegenNoHeal() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(20, 0.0F, 30.0F, 40.0F, Difficulty.NORMAL, false, 80), 600);
    assertFalse(outcome.heals());
  }

  @ParameterizedTest(name = "{1} at {0} health: starves {2}")
  @CsvSource({
    "11, EASY, true",
    "10, EASY, false",
    "2, NORMAL, true",
    "1, NORMAL, false",
    "1, HARD, true",
    "20, PEACEFUL, true",
    "5, PEACEFUL, false"
  })
  @DisplayName("an empty blood bar starves like an empty hunger bar")
  void starves(final float health, final Difficulty difficulty, final boolean starves) {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(0, 0.0F, health, difficulty, 80), 600);
    assertEquals(starves, outcome.starves());
  }

  @ParameterizedTest(name = "{0}")
  @EnumSource(Difficulty.class)
  @DisplayName("starving only happens on the 80 tick pulse")
  void starvesOnPulse(final Difficulty difficulty) {
    assertFalse(BloodMetabolism.tick(state(0, 0.0F, 20.0F, difficulty, 79), 600).starves());
    assertTrue(BloodMetabolism.canStarve(20.0F, difficulty));
  }

  @Test
  @DisplayName("exhaustion is capped at vanilla's 40")
  void exhaustionCapped() {
    assertEquals(
        40.0F,
        BloodMetabolism.tick(state(20, 39.9F, 30.0F, Difficulty.PEACEFUL, 80), 600).exhaustion(),
        0.0001F);
  }
}
