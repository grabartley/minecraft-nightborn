package com.grahambartley.nightborn.familiar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LoyaltyTest {

  @ParameterizedTest(name = "{0} is kept as {1}")
  @CsvSource({"-1, 0", "0, 0", "60, 60", "100, 100", "101, 100"})
  @DisplayName("loyalty stays within 0 to 100")
  void clamp(final int given, final int kept) {
    assertEquals(kept, Loyalty.clamp(given));
  }

  @ParameterizedTest(name = "{0} changed by {1} is {2}")
  @CsvSource({"60, 5, 65", "98, 5, 100", "60, -25, 35", "10, -25, 0", "0, 0, 0"})
  @DisplayName("a change never takes loyalty out of range")
  void change(final int loyalty, final int delta, final int after) {
    assertEquals(after, Loyalty.change(loyalty, delta));
  }
}
