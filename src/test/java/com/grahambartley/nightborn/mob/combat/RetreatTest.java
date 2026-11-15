package com.grahambartley.nightborn.mob.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.CombatSettings;
import com.grahambartley.nightborn.config.NightbornConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RetreatTest {

  @ParameterizedTest(name = "{0}/{1}: {2}%")
  @CsvSource({"30, 60, 50", "0, 60, 0", "60, 60, 100", "5, 0, 100"})
  @DisplayName("health reads as a whole percent")
  void healthPercent(final float health, final float max, final int percent) {
    assertEquals(percent, Retreat.healthPercent(health, max));
  }

  @ParameterizedTest(name = "{0}% at threshold {1}, resting {2}: {3}")
  @CsvSource({
    "20, 25, false, true",
    "25, 25, false, false",
    "20, 25, true, false",
    "5, 0, false, false"
  })
  @DisplayName("a hurt vampire pulls back below its stage's threshold, then rests from it")
  void starts(final int health, final int threshold, final boolean resting, final boolean starts) {
    assertEquals(starts, Retreat.starts(health, threshold, resting));
  }

  @ParameterizedTest(name = "{0}% at threshold {1} after {2} ticks: {3}")
  @CsvSource({
    "30, 25, 10, false",
    "55, 25, 10, true",
    "30, 25, 160, true",
    "99, 80, 10, false",
    "100, 80, 10, true"
  })
  @DisplayName("it comes again once healed enough or after a while")
  void ends(final int health, final int threshold, final int ticks, final boolean ends) {
    assertEquals(ends, Retreat.ends(health, threshold, ticks, NightbornConfig.DEFAULTS.combat()));
  }

  @ParameterizedTest(name = "rest {0} s, speed {1}%: {2} ticks at {3}x")
  @CsvSource({"15, 130, 300, 1.3", "0, 50, 0, 0.5", "300, 300, 6000, 3.0"})
  @DisplayName("rest and speed come from the server settings")
  void fromSettings(final int rest, final int speed, final int ticks, final double factor) {
    final CombatSettings settings =
        NightbornConfig.DEFAULTS
            .with(CombatSettings.RETREAT_REST_SECONDS, rest)
            .with(CombatSettings.RETREAT_SPEED, speed)
            .combat();
    assertEquals(ticks, Retreat.restTicks(settings));
    assertEquals(factor, Retreat.speed(settings), 1.0E-9);
  }
}
