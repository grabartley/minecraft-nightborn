package com.grahambartley.nightborn.boss;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodShareTest {

  @ParameterizedTest(name = "carry {0}, {1} dealt at {2}%: {3} blood, carry {4}")
  @CsvSource({
    "0, 10, 10, 1, 0",
    "0, 11, 20, 2, 0.2",
    "0, 4, 10, 0, 0.4",
    "0.8, 4, 10, 1, 0.2",
    "0, 0, 10, 0, 0",
    "0.5, -3, 10, 0, 0.5",
    "0.5, 10, 0, 0, 0.5",
    "-1, 10, 10, 1, 0"
  })
  @DisplayName("a share of each blow turns into whole blood, and what is left carries to the next")
  void after(
      final float carry, final float dealt, final int percent, final int blood, final float left) {
    final BloodShare share = BloodShare.after(carry, dealt, percent);
    assertEquals(blood, share.blood());
    assertEquals(left, share.carry(), 1.0E-5F);
  }
}
