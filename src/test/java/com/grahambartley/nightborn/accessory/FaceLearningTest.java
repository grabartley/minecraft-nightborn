package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FaceLearningTest {

  @ParameterizedTest(name = "roll {0}, share {1}: learns {2}")
  @CsvSource({
    "0.0, 1.0, true",
    "0.99, 1.0, true",
    "0.49, 0.5, true",
    "0.5, 0.5, false",
    "0.9, 0.5, false",
    "0.0, 0.0, false"
  })
  @DisplayName("a village learns a face as often as the share allows")
  void learns(final double roll, final double share, final boolean learns) {
    assertEquals(learns, FaceLearning.learns(roll, share));
  }
}
