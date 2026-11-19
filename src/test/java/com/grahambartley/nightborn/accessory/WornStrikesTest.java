package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WornStrikesTest {

  @ParameterizedTest(name = "{0} damage, bare {1} +{2}, stake {3} x{4}, rally x{5}: {6}")
  @CsvSource({
    "1.0, false, 1.0, false, 1.0, 1.0, 1.0",
    "1.0, true, 1.0, false, 1.0, 1.0, 2.0",
    "1.0, true, 0.0, false, 1.0, 1.0, 1.0",
    "5.0, false, 1.0, true, 1.1, 1.0, 5.5",
    "5.0, false, 0.0, true, 1.0, 1.0, 5.0",
    "4.0, false, 0.0, false, 1.0, 1.1, 4.4",
    "2.0, true, 1.0, false, 1.0, 1.1, 3.3",
    "2.0, true, -3.0, true, -1.0, -1.0, 0.0"
  })
  @DisplayName("bare hands add, a stake and a rallying house multiply, and none takes damage away")
  void strike(
      final float amount,
      final boolean bare,
      final double extra,
      final boolean stake,
      final double stakeFactor,
      final double rally,
      final float dealt) {
    assertEquals(dealt, WornStrikes.strike(amount, bare, extra, stake, stakeFactor, rally), 1.0E-5);
  }
}
