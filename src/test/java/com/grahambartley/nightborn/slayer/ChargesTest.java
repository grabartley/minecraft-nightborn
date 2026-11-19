package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ChargesTest {

  @ParameterizedTest(name = "started {0}, now {1}, window {2}: {3}")
  @CsvSource({
    "100, 100, 10, true",
    "100, 110, 10, true",
    "100, 111, 10, false",
    "100, 99, 10, false",
    ", 100, 10, false"
  })
  @DisplayName("a vampire is charging only within the window after its charge began")
  void within(final Long started, final long now, final int window, final boolean charging) {
    assertEquals(charging, Charges.within(started, now, window));
  }
}
