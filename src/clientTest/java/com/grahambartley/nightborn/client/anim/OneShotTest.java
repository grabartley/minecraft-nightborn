package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OneShotTest {

  @ParameterizedTest(name = "{0} ticks in, {1}s long: playing {2}")
  @CsvSource({
    "-1, 1.0, false",
    "0, 1.0, true",
    "19, 1.0, true",
    "20, 1.0, false",
    "27, 1.4, true",
    "28, 1.4, false"
  })
  @DisplayName("a clip plays from the tick it starts until its length has passed")
  void playing(final int ticksIn, final double length, final boolean playing) {
    final OneShot shot = new OneShot();
    shot.start(100);
    assertEquals(playing, shot.playing(length, 100 + ticksIn));
    assertEquals(ticksIn / OneShot.TICKS_PER_SECOND, shot.seconds(100 + ticksIn), 1e-9);
  }

  @Test
  @DisplayName("a clip never started or cleared is not playing and has no time")
  void idle() {
    final OneShot shot = new OneShot();
    assertFalse(shot.playing(5.0, 10));
    assertTrue(Double.isNaN(shot.seconds(10)));
    shot.start(10);
    shot.clear();
    assertFalse(shot.playing(5.0, 11));
  }
}
