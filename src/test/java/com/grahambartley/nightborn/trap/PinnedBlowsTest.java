package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PinnedBlowsTest {

  @ParameterizedTest(name = "{0} on pinned vampire {1}, bonus {2} -> {3}")
  @CsvSource({"8, true, 0.25, 10", "8, false, 0.25, 8", "8, true, 0, 8", "4, true, 0.5, 6"})
  @DisplayName("Master Trapper's blows hurt a pinned vampire a quarter more")
  void raises(final float amount, final boolean pinned, final double bonus, final float raised) {
    assertEquals(raised, PinnedBlows.raise(amount, pinned, bonus), 1.0E-5);
  }
}
