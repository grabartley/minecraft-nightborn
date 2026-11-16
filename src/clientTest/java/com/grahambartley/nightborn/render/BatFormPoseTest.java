package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFormPoseTest {

  @ParameterizedTest(name = "swing {0} -> swoops {1}")
  @CsvSource({"0.0, false", "0.3, false", "0.59, false", "0.6, true", "1.0, true"})
  @DisplayName("a bat that moves fast swoops, and one that drifts beats its wings")
  void swoops(final float swing, final boolean expected) {
    assertEquals(expected, BatFormPose.swoops(swing));
  }
}
