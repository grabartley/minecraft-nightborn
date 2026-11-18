package com.grahambartley.nightborn.ward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WardStrengthTest {

  @ParameterizedTest(name = "{0} strings at {1} each, cap {2}: {3}")
  @CsvSource({
    "0, 15, 45, 0",
    "1, 15, 45, 15",
    "2, 15, 45, 30",
    "3, 15, 45, 45",
    "4, 15, 45, 45",
    "10, 15, 200, 100",
    "-2, 15, 45, 0",
    "3, 0, 45, 0",
    "2147483647, 100, 100, 100"
  })
  @DisplayName("more strings weaken more, up to the cap")
  void strings(final int count, final int perString, final int cap, final int expected) {
    assertEquals(expected, WardStrength.strings(count, perString, cap));
  }

  @ParameterizedTest(name = "base {0}, faith {1}, bonus {2}%: {3}")
  @CsvSource({
    "60, 0, 25, 60",
    "60, 1, 25, 75",
    "60, 2, 25, 90",
    "60, 3, 25, 100",
    "60, 2, 0, 60",
    "60, -1, 25, 60",
    "0, 2, 25, 0",
    "40, 0.5, 50, 50"
  })
  @DisplayName("faith makes a held crucifix stronger, up to the cap")
  void crucifix(final int base, final double faith, final int bonus, final int expected) {
    assertEquals(expected, WardStrength.crucifix(base, faith, bonus));
  }

  @ParameterizedTest(name = "strength {0} at {1} resistance: {2}")
  @CsvSource({"10, 0.0, 10", "10, 0.3, 7", "10, 0.8, 2", "1, 0.8, 1", "0, 0.3, 0", "4, 0.1, 4"})
  @DisplayName("Warding dulls garlic and crucifixes but never stops them")
  void dulled(final int strength, final double resistance, final int expected) {
    assertEquals(expected, WardStrength.dulled(strength, resistance));
  }
}
