package com.grahambartley.nightborn.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.mob.VampireBatPose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatFormAnimatableTest {

  @ParameterizedTest(name = "swing {0} -> {1}")
  @CsvSource({"0.0, FLY", "0.5, FLY", "0.6, SWOOP", "1.0, SWOOP"})
  @DisplayName("a drifting bat beats its wings and a fast one swoops")
  void clipForSwing(final float swing, final VampireBatPose expected) {
    assertEquals(expected, BatFormAnimatable.pose(swing));
  }
}
