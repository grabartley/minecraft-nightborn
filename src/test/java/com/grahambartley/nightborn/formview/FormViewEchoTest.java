package com.grahambartley.nightborn.formview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FormViewEchoTest {

  static FormViewEcho echo(
      final float nearRange, final float maxRange, final float speed, final int pulseTicks) {
    return new FormViewEcho(
        nearRange, 0.3F, speed, 2.0F, 16.0F, maxRange, 1.5F, 1.0F, 0.1F, 0.6F, pulseTicks, 0.6F);
  }

  @ParameterizedTest(name = "volume {0} reaches {1}")
  @CsvSource({"0.0, 0.0", "0.5, 8.0", "1.0, 16.0", "2.0, 32.0", "3.0, 40.0", "-1.0, 0.0"})
  @DisplayName("a louder sound reaches further, up to the farthest range")
  void rangeGrowsWithVolume(final float volume, final float range) {
    assertEquals(range, echo(5.0F, 40.0F, 24.0F, 30).rippleRange(volume), 1.0E-6F);
  }

  @ParameterizedTest(name = "every {0} ticks, tick {1}: {2}")
  @CsvSource({
    "30, 0, true",
    "30, 1, false",
    "30, 29, false",
    "30, 30, true",
    "30, 60, true",
    "0, 0, false",
    "0, 30, false"
  })
  @DisplayName("the bat's own call sends a ripple on a steady beat, or never when set to 0")
  void pulsesOnBeat(final int every, final int tick, final boolean pulses) {
    assertEquals(pulses, echo(5.0F, 40.0F, 24.0F, every).pulsesOn(tick));
  }

  @ParameterizedTest(name = "near {0}, max {1}, speed {2}")
  @CsvSource({"5.0, 4.0, 24.0, max_range", "5.0, 40.0, 0.0, ripple_speed"})
  @DisplayName("an echo whose ripples could never be seen is refused with a reason")
  void refusesUselessEcho(
      final float near, final float max, final float speed, final String named) {
    final String message =
        FormViewEcho.check(echo(near, max, speed, 30)).error().orElseThrow().message();
    assertTrue(message.contains(named), message);
  }

  @Test
  @DisplayName("a sensible echo passes the check")
  void acceptsSensibleEcho() {
    assertTrue(FormViewEcho.check(echo(5.0F, 40.0F, 24.0F, 30)).isSuccess());
  }
}
