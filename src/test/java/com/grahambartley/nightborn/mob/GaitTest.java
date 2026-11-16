package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GaitTest {

  @ParameterizedTest(name = "limb swing {0}: {1}")
  @CsvSource({
    "0.0, STILL",
    "0.05, STILL",
    "0.1, STILL",
    "0.11, WALK",
    "0.35, WALK",
    "0.6, WALK",
    "0.61, RUN",
    "1.0, RUN"
  })
  void readsTheGaitFromTheLimbSwing(final float limbSwing, final Gait gait) {
    assertEquals(gait, Gait.of(limbSwing));
  }
}
