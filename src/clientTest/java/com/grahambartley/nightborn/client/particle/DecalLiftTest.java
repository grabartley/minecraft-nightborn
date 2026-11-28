package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecalLiftTest {

  @ParameterizedTest(name = "wetness {0}: rim {1}, body {2}")
  @CsvSource({
    "0.0, 0.002, 0.010",
    "0.5, 0.004, 0.015",
    "1.0, 0.006, 0.020",
    "-1.0, 0.002, 0.010",
    "2.0, 0.006, 0.020"
  })
  @DisplayName("wetter decals sit higher, so fresh blood lies on top of old")
  void lift(final float wetness, final double rim, final double body) {
    assertEquals(rim, DecalLift.rim(wetness), 1.0e-9);
    assertEquals(body, DecalLift.body(wetness), 1.0e-9);
  }

  @ParameterizedTest(name = "rim wetness {0} under body wetness {1}")
  @CsvSource({"1.0, 0.0", "1.0, 1.0", "0.0, 0.0", "0.5, 0.2"})
  @DisplayName("every rim lies under every body, so no outline shows inside a patch")
  void rimsStayUnderBodies(final float rimWetness, final float bodyWetness) {
    assertTrue(DecalLift.rim(rimWetness) < DecalLift.body(bodyWetness));
    assertTrue(DecalLift.anchor() <= DecalLift.rim(rimWetness));
  }
}
