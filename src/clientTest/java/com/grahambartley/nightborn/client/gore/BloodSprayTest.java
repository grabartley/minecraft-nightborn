package com.grahambartley.nightborn.client.gore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BloodSprayTest {

  @ParameterizedTest(name = "yaw {0}, speed {1}, lift {2} -> ({3}, {4}, {5})")
  @CsvSource({
    "0, 0.2, 0.05, 0.2, 0.05, 0",
    "90, 0.2, 0.05, 0, 0.05, 0.2",
    "180, 0.1, 0.0, -0.1, 0.0, 0",
    "-90, 0.3, 0.1, 0, 0.1, -0.3",
    "45, 1.0, 0.0, 0.70711, 0.0, 0.70711"
  })
  @DisplayName("drops fly along the spray yaw at the given speed and lift")
  void velocity(
      final float yaw,
      final double speed,
      final double lift,
      final double x,
      final double y,
      final double z) {
    final Vec3d velocity = BloodSpray.velocity(yaw, speed, lift);
    assertEquals(x, velocity.x, 1.0e-3);
    assertEquals(y, velocity.y, 1.0e-9);
    assertEquals(z, velocity.z, 1.0e-3);
  }
}
