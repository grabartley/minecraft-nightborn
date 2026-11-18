package com.grahambartley.nightborn.blood;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.batform.BatEffort;
import com.grahambartley.nightborn.config.NightbornConfig;
import net.minecraft.world.Difficulty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodMetabolismTest {

  private static BloodMetabolism.State state(
      final int blood,
      final float exhaustion,
      final float health,
      final Difficulty difficulty,
      final int age) {
    return new BloodMetabolism.State(
        blood, exhaustion, health, 40.0F, difficulty, true, BloodMetabolism.GROUNDED, age, 0.0);
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
                  blood,
                  exhaustion,
                  40.0F,
                  40.0F,
                  Difficulty.NORMAL,
                  true,
                  BloodMetabolism.GROUNDED,
                  tick,
                  0.0),
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
    "20, 30, 200, true",
    "11, 30, 400, true",
    "10, 30, 200, false",
    "0, 30, 200, false",
    "20, 40, 200, false",
    "20, 30, 80, false",
    "20, 30, 201, false"
  })
  @DisplayName("a blood bar above half heals every 200 ticks while hurt")
  void heals(final int blood, final float health, final int age, final boolean heals) {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(state(blood, 0.0F, health, Difficulty.NORMAL, age), 600);
    assertEquals(heals, outcome.heals());
  }

  @ParameterizedTest(name = "exhaustion {0}")
  @CsvSource({"0.0", "1.5", "3.9"})
  @DisplayName("healing costs no blood and adds no exhaustion")
  void healingIsFree(final float exhaustion) {
    final BloodMetabolism.Outcome healing =
        BloodMetabolism.tick(state(20, exhaustion, 30.0F, Difficulty.NORMAL, 200), 600);
    final BloodMetabolism.Outcome idle =
        BloodMetabolism.tick(state(20, exhaustion, 30.0F, Difficulty.NORMAL, 201), 600);
    assertTrue(healing.heals());
    assertEquals(idle.blood(), healing.blood());
    assertEquals(idle.exhaustion(), healing.exhaustion(), 0.0001F);
  }

  @Test
  @DisplayName("natural regeneration off stops blood healing")
  void noRegenNoHeal() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(
                20,
                0.0F,
                30.0F,
                40.0F,
                Difficulty.NORMAL,
                false,
                BloodMetabolism.GROUNDED,
                200,
                0.0),
            600);
    assertFalse(outcome.heals());
  }

  @ParameterizedTest(name = "flight drain every {0} s")
  @CsvSource({"10", "20", "60"})
  @DisplayName("levitating or flying as a bat drains blood on top of the resting drain")
  void flightDrains(final int seconds) {
    int blood = 20;
    float exhaustion = 0.0F;
    for (int tick = 1; tick <= seconds * 20 + 1; tick++) {
      final BloodMetabolism.Outcome outcome =
          BloodMetabolism.tick(
              new BloodMetabolism.State(
                  blood, exhaustion, 40.0F, 40.0F, Difficulty.NORMAL, true, seconds, tick, 0.0),
              600);
      blood = outcome.blood();
      exhaustion = outcome.exhaustion();
    }
    assertEquals(19, blood);
  }

  @Test
  @DisplayName("flight costs nothing in peaceful")
  void peacefulFlightIsFree() {
    final BloodMetabolism.Outcome outcome =
        BloodMetabolism.tick(
            new BloodMetabolism.State(
                10, 3.99F, 40.0F, 40.0F, Difficulty.PEACEFUL, true, 1, 1, 0.0),
            5);
    assertEquals(10, outcome.blood());
  }

  @ParameterizedTest(name = "exhaustion {0} becomes {1}")
  @CsvSource({"3.0, 3.0", "43.9, 39.9", "44.0, 40.0", "60.0, 40.0"})
  @DisplayName("exhaustion is capped at vanilla's 40")
  void exhaustionCapped(final float given, final float kept) {
    assertEquals(
        kept,
        BloodMetabolism.tick(state(20, given, 30.0F, Difficulty.PEACEFUL, 1), 600).exhaustion(),
        0.0001F);
  }

  @ParameterizedTest(name = "bat {0}, levitating {1} -> {2} s")
  @CsvSource({
    "FLYING, false, 30",
    "FLYING, true, 30",
    "SPRINTING, false, 15",
    "RESTING, false, 0",
    "RESTING, true, 0",
    "NONE, true, 120",
    "NONE, false, 0"
  })
  @DisplayName(
      "a flying bat drains at the bat rate, faster at a sprint and not at all while it rests, and"
          + " a levitating vampire at the levitation rate")
  void flightDrainSeconds(final BatEffort bat, final boolean levitating, final int expected) {
    assertEquals(
        expected,
        BloodMetabolism.flightDrainSeconds(bat, levitating, NightbornConfig.DEFAULTS.core()));
  }

  @ParameterizedTest(name = "healing speed {0}: a pulse every {1} ticks")
  @CsvSource({"0.0, 200", "0.5, 133", "1.0, 100", "3.0, 50", "-1.0, 200"})
  @DisplayName("a healing gift brings the healing pulse sooner, twice as often when fresh")
  void healingSpeed(final double speed, final int pulse) {
    assertEquals(pulse, BloodMetabolism.pulseTicks(speed));
    assertTrue(
        BloodMetabolism.tick(
                new BloodMetabolism.State(
                    20,
                    0.0F,
                    30.0F,
                    40.0F,
                    Difficulty.NORMAL,
                    true,
                    BloodMetabolism.GROUNDED,
                    pulse,
                    speed),
                600)
            .heals());
  }

  @ParameterizedTest(name = "{0}s per blood at rate {1}: {2}s")
  @CsvSource({
    "60, 1.0, 60",
    "60, 0.95, 63",
    "60, 0.85, 71",
    "60, 1.25, 48",
    "60, 0.2, 300",
    "0, 0.5, 0",
    "1, 3.0, 1"
  })
  @DisplayName("Thrift slows the drain and Thirst speeds it, and grounded stays grounded")
  void atRate(final int seconds, final double rate, final int expected) {
    assertEquals(expected, BloodMetabolism.atRate(seconds, rate));
  }
}
