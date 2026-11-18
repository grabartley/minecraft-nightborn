package com.grahambartley.nightborn.holy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SplashReachTest {

  @ParameterizedTest(name = "holy {0}, extra {1}: {2}")
  @CsvSource({"true, 1.0, 5.0", "false, 1.0, 4.0", "true, 0.0, 4.0", "true, -2.0, 4.0"})
  @DisplayName("only holy water splashes wider for the thrower's gift")
  void reach(final boolean holy, final double extra, final double reach) {
    assertEquals(reach, SplashReach.reach(4.0, holy, extra), 1.0E-9);
  }

  @ParameterizedTest(name = "holy {0}, extra {1}: {2}")
  @CsvSource({"true, 1.0, 25.0", "false, 1.0, 16.0", "true, 0.0, 16.0"})
  @DisplayName("the squared reach used for distance grows the same way")
  void reachSquared(final boolean holy, final double extra, final double squared) {
    assertEquals(squared, SplashReach.reachSquared(16.0, holy, extra), 1.0E-9);
  }
}
