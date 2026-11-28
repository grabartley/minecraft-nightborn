package com.grahambartley.nightborn.holy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.config.GearSettings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HolyWaterDoseTest {

  private static final GearSettings SETTINGS = new GearSettings(4, 6, 40, 10, 60, 5, 3, 10);

  @ParameterizedTest(name = "proximity {0}, faith x{1}: {2} damage, {3}% for {4} ticks")
  @CsvSource({
    "1.0, 1.0, 6.0, 40, 200",
    "0.5, 1.0, 3.0, 20, 100",
    "0.25, 1.0, 1.5, 10, 50",
    "1.0, 1.5, 9.0, 60, 200",
    "1.0, 3.0, 18.0, 100, 200",
    "2.0, 1.0, 6.0, 40, 200",
    "-1.0, 1.0, 0.0, 0, 0",
    "1.0, -1.0, 0.0, 0, 200"
  })
  @DisplayName("a dose shrinks with distance from the splash and grows with the thrower's faith")
  void scales(
      final double proximity,
      final double faith,
      final float damage,
      final int strength,
      final int ticks) {
    assertEquals(
        new HolyWaterDose(damage, strength, ticks), HolyWaterDose.of(SETTINGS, proximity, faith));
  }

  @ParameterizedTest(name = "{0} damage, {1}% for {2} ticks: empty {3}")
  @CsvSource({
    "0.0, 0, 0, true",
    "0.0, 40, 0, true",
    "0.0, 0, 200, true",
    "0.0, 40, 200, false",
    "3.0, 0, 0, false"
  })
  @DisplayName("a dose with no damage and no lasting weakness does nothing")
  void empty(final float damage, final int strength, final int ticks, final boolean empty) {
    assertEquals(empty, new HolyWaterDose(damage, strength, ticks).isEmpty());
  }
}
