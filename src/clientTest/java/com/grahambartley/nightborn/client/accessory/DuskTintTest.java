package com.grahambartley.nightborn.client.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DuskTintTest {

  @ParameterizedTest(name = "sky light {0}, darkness {1}: alpha {2}")
  @CsvSource({"15, 0, 120", "0, 0, 0", "15, 11, 0", "8, 0, 64", "15, 4, 76", "20, -2, 120"})
  @DisplayName("the lenses darken the open day most, shade less, and the night not at all")
  void alpha(final int skyLight, final int darkness, final int alpha) {
    assertEquals(alpha, DuskTint.alpha(skyLight, darkness));
  }
}
