package com.grahambartley.nightborn.accessory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CobwebsTest {

  @ParameterizedTest(name = "slow {0}: {1}, {2}")
  @CsvSource({"1.0, 0.25, 0.05", "0.0, 1.0, 1.0", "0.5, 0.625, 0.525", "-1.0, 1.0, 1.0"})
  @DisplayName("a cobweb slows a wearer only by their share of its grip")
  void slowing(final double slow, final double sideways, final double upwards) {
    final Vec3d out = Cobwebs.slowing(new Vec3d(0.25, 0.05, 0.25), slow);
    assertEquals(sideways, out.x, 1.0E-9);
    assertEquals(upwards, out.y, 1.0E-9);
    assertEquals(sideways, out.z, 1.0E-9);
  }
}
