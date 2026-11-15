package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SplatFadeTest {

  @ParameterizedTest(name = "age {0} of {1}: shade {2}")
  @CsvSource({
    "0, 100, 1.0",
    "35, 100, 1.0",
    "60, 100, 0.75",
    "80, 100, 0.55",
    "100, 100, 0.55",
    "150, 100, 0.55",
    "-5, 100, 1.0",
    "0, 0, 0.55"
  })
  @DisplayName("a splat stays fresh, then darkens as it dries")
  void shade(final int age, final int maxAge, final float expected) {
    assertEquals(expected, SplatFade.shade(age, maxAge), 1.0e-4F);
  }

  @ParameterizedTest(name = "age {0} of {1}: alpha {2}")
  @CsvSource({
    "0, 100, 1.0",
    "80, 100, 1.0",
    "90, 100, 0.5",
    "100, 100, 0.0",
    "150, 100, 0.0",
    "0, 0, 0.0"
  })
  @DisplayName("a splat only fades out at the end of its life")
  void alpha(final int age, final int maxAge, final float expected) {
    assertEquals(expected, SplatFade.alpha(age, maxAge), 1.0e-4F);
  }
}
