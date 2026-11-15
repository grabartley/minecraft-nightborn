package com.grahambartley.nightborn.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.CoreSettings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CoreBoostsTest {

  private static final CoreSettings SETTINGS = new CoreSettings(4, 20, 30, 20);
  private static final BloodSettings BLOOD =
      new BloodSettings(30, 50, 100, 150, 60, 60, 10, 300, 10, 50, 50, 4);

  @ParameterizedTest(name = "empty hand {0}, night {1}, starving {2}")
  @CsvSource({
    "true, true, 0, 4, 0.2, 0.3",
    "true, false, 0, 4, 0, 0",
    "false, true, 0, 0, 0.2, 0.3",
    "false, false, 0, 0, 0, 0",
    "true, true, 25, 3.5, 0.1, 0.15",
    "true, true, 50, 3, 0, 0",
    "true, true, 100, 2, 0, 0"
  })
  void boosts(
      final boolean emptyHand,
      final boolean night,
      final int starving,
      final double hands,
      final double speed,
      final double jump) {
    final CoreBoosts boosts = CoreBoosts.of(SETTINGS, BLOOD, emptyHand, night, starving);
    assertEquals(1.0, boosts.maxHealthMultiplier(), 0.0001);
    assertEquals(hands, boosts.bareHandDamage(), 0.0001);
    assertEquals(speed, boosts.speedMultiplier(), 0.0001);
    assertEquals(jump, boosts.jumpMultiplier(), 0.0001);
  }
}
