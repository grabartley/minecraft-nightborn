package com.grahambartley.nightborn.mob;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BatSteeringTest {

  @ParameterizedTest(name = "offset ({0}, {1}, {2})")
  @CsvSource({"5, 0, 0", "0, 3, 0", "0, 0, -8", "-2, 1, 2"})
  @DisplayName("from rest a bat speeds up straight toward its prey")
  void steersToward(final double x, final double y, final double z) {
    final Vec3d offset = new Vec3d(x, y, z);
    final Vec3d velocity = BatSteering.toward(Vec3d.ZERO, offset);
    assertTrue(velocity.dotProduct(offset) > 0.0, "moves toward the prey");
    assertEquals(0.0, velocity.crossProduct(offset).length(), 1.0E-9);
    assertEquals(BatSteering.SWOOP_SPEED * BatSteering.TURN, velocity.length(), 1.0E-9);
  }

  @ParameterizedTest(name = "after {0} ticks")
  @CsvSource({"40", "80"})
  @DisplayName("held on course a bat settles at its swoop speed")
  void settles(final int ticks) {
    Vec3d velocity = new Vec3d(0.0, -0.3, 0.0);
    for (int i = 0; i < ticks; i++) {
      velocity = BatSteering.toward(velocity, new Vec3d(1.0, 0.0, 0.0));
    }
    assertEquals(BatSteering.SWOOP_SPEED, velocity.x, 0.01);
    assertEquals(0.0, velocity.y, 0.01);
  }

  @ParameterizedTest(name = "velocity ({0}, {1}): yaw {2}")
  @CsvSource({"0, 1, 0", "-1, 0, 90", "0, -1, 180", "1, 0, -90"})
  @DisplayName("a bat faces where it flies, in Minecraft's yaw")
  void yaw(final double x, final double z, final float yaw) {
    final float got = BatSteering.yaw(new Vec3d(x, 0.0, z));
    assertEquals(0.0, Math.floorMod(Math.round(got - yaw), 360), 1.0E-3);
  }
}
