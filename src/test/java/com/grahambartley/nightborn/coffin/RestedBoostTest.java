package com.grahambartley.nightborn.coffin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.CoffinSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RestedBoostTest {

  private static final CoffinSettings DEFAULTS = new CoffinSettings(50, 50, 10, 1, 15, 2);

  @ParameterizedTest(name = "level {0}: speed {1}, damage {2}")
  @CsvSource({"-1, 0.0, 0.0", "0, 0.10, 1.0", "1, 0.15, 2.0", "2, 0.15, 2.0"})
  @DisplayName("rested gives a little speed and strength, more in a coffin of one's house")
  void boost(final int level, final double speed, final double damage) {
    final RestedBoost boost = RestedBoost.of(DEFAULTS, level);
    assertEquals(speed, boost.speedMultiplier(), 1.0E-9);
    assertEquals(damage, boost.attackDamage(), 1.0E-9);
  }

  @ParameterizedTest(name = "set to {0}% and {1}: speed {2}, damage {3}")
  @CsvSource({"0, 0, 0.0, 0.0", "25, 4, 0.25, 4.0", "100, 20, 1.0, 20.0"})
  @DisplayName("the server's settings decide what being rested gives")
  void readsSettings(
      final int speed, final int damage, final double gotSpeed, final double gotDamage) {
    final RestedBoost boost = RestedBoost.of(new CoffinSettings(50, 50, speed, damage, 0, 0), 0);
    assertEquals(gotSpeed, boost.speedMultiplier(), 1.0E-9);
    assertEquals(gotDamage, boost.attackDamage(), 1.0E-9);
  }
}
