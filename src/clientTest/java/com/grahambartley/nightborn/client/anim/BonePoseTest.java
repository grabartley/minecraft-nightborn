package com.grahambartley.nightborn.client.anim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BonePoseTest {

  private static final BonePose BENT = new BonePose(new Vec3d(40, 0, 10), new Vec3d(0, 2, 0));

  @ParameterizedTest(name = "weight {0}")
  @CsvSource({"0, 0, 0", "0.5, 20, 1", "1, 40, 2"})
  @DisplayName("weighting scales rotation and position together")
  void weights(final double weight, final double pitch, final double lift) {
    final BonePose scaled = BENT.times(weight);
    assertEquals(pitch, scaled.rotation().x, 1.0E-9);
    assertEquals(lift, scaled.position().y, 1.0E-9);
  }

  @ParameterizedTest(name = "towards {0}")
  @CsvSource({"0, 40", "0.25, 30", "1, 0"})
  @DisplayName("blending moves part of the way to the other pose")
  void blends(final double weight, final double pitch) {
    assertEquals(pitch, BENT.towards(BonePose.REST, weight).rotation().x, 1.0E-9);
  }

  @Test
  @DisplayName("layered poses add up")
  void adds() {
    assertEquals(80, BENT.plus(BENT).rotation().x, 1.0E-9);
  }
}
