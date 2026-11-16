package com.grahambartley.nightborn.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HudSignsTest {

  @ParameterizedTest(name = "sign {0} sits at y {1}")
  @CsvSource({"0, 4", "1, 24", "2, 44"})
  @DisplayName("signs stack down from the top left without overlapping")
  void stacks(final int index, final int y) {
    assertEquals(y, HudSigns.y(index));
  }
}
