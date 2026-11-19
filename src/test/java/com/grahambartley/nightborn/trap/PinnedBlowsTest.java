package com.grahambartley.nightborn.trap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PinnedBlowsTest {

  @ParameterizedTest(name = "{0} with bonus {1} -> {2}")
  @CsvSource({"8, 0.25, 10", "8, 0, 8", "4, 0.5, 6", "0, 0.25, 0"})
  @DisplayName("Master Trapper's blows on a pinned vampire hurt a quarter more")
  void raises(final float amount, final double bonus, final float raised) {
    assertEquals(raised, PinnedBlows.raise(amount, bonus), 1.0E-5);
  }
}
