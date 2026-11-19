package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DeathHoldTest {

  @ParameterizedTest(name = "held {0} ticks")
  @ValueSource(ints = {0, 1, 48, 60})
  @DisplayName("the body lingers for exactly the ticks its death clip holds, then goes")
  void lingers(final int hold) {
    final DeathHold held = new DeathHold();
    int lingered = 0;
    while (held.lingers(hold)) {
      lingered++;
    }
    assertEquals(hold, lingered);
    assertFalse(held.lingers(hold));
  }

  @ParameterizedTest(name = "death time {0}, held {1}: {2}")
  @CsvSource({"0, 60, 19", "5, 60, 19", "19, 60, 19", "0, 0, 0", "7, 0, 7"})
  @DisplayName("a held death ends on vanilla's last death tick, and an unheld one runs as vanilla")
  void deathTime(final int deathTime, final int hold, final int shown) {
    assertEquals(shown, DeathHold.deathTime(deathTime, hold));
  }
}
