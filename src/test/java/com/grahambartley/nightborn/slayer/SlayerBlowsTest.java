package com.grahambartley.nightborn.slayer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlayerBlowsTest {

  @ParameterizedTest(name = "{0} with {1} more and {2} share: {3}")
  @CsvSource({"6, 0, 0, 6", "6, 3, 0, 9", "6, 0, 0.5, 9", "6, 2, 0.5, 12", "6, -1, -1, 6"})
  @DisplayName("a hunter's blow on a vampire gains its flat bonus, then its share")
  void raise(final float amount, final double flat, final double share, final float raised) {
    assertEquals(raised, SlayerBlows.raise(amount, flat, share), 1.0E-6F);
  }
}
