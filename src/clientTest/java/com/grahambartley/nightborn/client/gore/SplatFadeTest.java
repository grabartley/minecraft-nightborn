package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SplatFadeTest {

  @ParameterizedTest(name = "age {0}: shade {1}")
  @CsvSource({"-5, 1.0", "0, 1.0", "600, 1.0", "1200, 0.775", "1800, 0.55", "6000, 0.55"})
  @DisplayName("blood is bright for half a minute, then darkens and stays dark")
  void shade(final int age, final float expected) {
    assertEquals(expected, SplatFade.shade(age), 1.0e-4F);
  }
}
