package com.grahambartley.nightborn.mob.household;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CountdownTest {

  @ParameterizedTest(name = "{0} ticks run {1} times")
  @CsvSource({"0, 0", "1, 0", "2, 1", "40, 39"})
  @DisplayName("a pastime lasts the ticks it was given")
  void runs(final int ticks, final int running) {
    final Countdown countdown = new Countdown(ticks);
    int count = 0;
    while (countdown.running()) {
      count++;
    }
    assertEquals(running, count);
  }
}
