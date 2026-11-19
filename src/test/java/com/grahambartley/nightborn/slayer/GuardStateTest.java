package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GuardStateTest {

  private static final GuardPower RIPOSTE = new GuardPower(10, 2.5F, 10, 3.0F, 40);

  @ParameterizedTest(name = "until {0}, now {1}: up {2}")
  @CsvSource({"110, 100, true", "110, 109, true", "110, 110, false", "110, 150, false"})
  @DisplayName("a guard is up for its time and then drops")
  void up(final long until, final long now, final boolean up) {
    assertEquals(up, new GuardState(until, RIPOSTE).up(now));
  }

  @ParameterizedTest(name = "distance {0}, charging {1}: meets {2}")
  @CsvSource({"1.0, true, true", "2.5, true, true", "2.6, true, false", "1.0, false, false"})
  @DisplayName("a guard meets only a charge that comes within its reach")
  void meets(final double distance, final boolean charging, final boolean meets) {
    assertEquals(meets, new GuardState(110L, RIPOSTE).meets(distance, charging));
  }
}
