package com.grahambartley.nightborn.faith;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PrayerRulesTest {

  @ParameterizedTest(name = "{0} health, boost {1} -> {2}")
  @CsvSource({"6, 1.0, 6", "6, 1.25, 7.5", "6, 1.95, 11.7", "2, 0.5, 2", "-1, 2.0, 0"})
  @DisplayName("prayer healing grows with the healer's boost and never shrinks below its base")
  void boosted(final float health, final double boost, final float expected) {
    assertEquals(expected, PrayerRules.boosted(health, boost), 1.0E-4);
  }

  @ParameterizedTest(name = "started {0}, now {1} -> {2}")
  @CsvSource({"100, 99, false", "100, 100, false", "100, 119, false", "100, 120, true"})
  @DisplayName("after the pulse it gives when cast, a lasting prayer pulses once a second")
  void pulses(final long started, final long now, final boolean pulses) {
    assertEquals(pulses, PrayerRules.pulses(started, now));
  }

  @ParameterizedTest(name = "{0} healed -> {1} counted")
  @CsvSource({"0, 0", "0.4, 0", "0.6, 1", "7.5, 8", "-1, 0"})
  @DisplayName("healing counts toward a trial in whole points")
  void counted(final float healed, final int counted) {
    assertEquals(counted, PrayerRules.counted(healed));
  }

  @ParameterizedTest(name = "lasting {0} ticks -> {1} pulses")
  @CsvSource({"100, 5", "200, 10", "20, 1", "21, 2"})
  @DisplayName("counting the pulse it gives when cast, a lasting prayer pulses once a second")
  void pulsesOverItsWholeTime(final int ticks, final int expected) {
    final long started = 1000L;
    int pulses = 1;
    for (long now = started; now < started + ticks; now++) {
      if (PrayerRules.pulses(started, now)) {
        pulses++;
      }
    }
    assertEquals(expected, pulses);
  }
}
