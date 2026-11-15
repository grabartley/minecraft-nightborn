package com.grahambartley.nightborn.weakened;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.WeakenedSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeakenedEffectsTest {

  @ParameterizedTest(name = "level {0}: speed {1}, blows {2}")
  @CsvSource({"0, 0, 0", "50, -0.2, -0.25", "100, -0.4, -0.5", "250, -0.4, -0.5", "-10, 0, 0"})
  @DisplayName("slower and weaker blows scale with strength, capped at the settings")
  void scales(final int level, final double speed, final double blows) {
    final WeakenedEffects effects = WeakenedEffects.of(level, new WeakenedSettings(40, 50));
    assertEquals(speed, effects.speedMultiplier(), 0.0001);
    assertEquals(blows, effects.damageMultiplier(), 0.0001);
  }
}
