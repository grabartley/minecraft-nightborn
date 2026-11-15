package com.grahambartley.nightborn.client.particle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DropLandingTest {

  @ParameterizedTest(name = "ground {0}, x {1}->{2}, z {3}->{4}: {5}")
  @CsvSource({
    "true, 0.1, 0.1, 0.1, 0.1, UP",
    "true, 0.1, 0.0, 0.0, 0.0, UP",
    "false, 0.1, 0.0, 0.0, 0.0, WEST",
    "false, -0.1, 0.0, 0.0, 0.0, EAST",
    "false, 0.0, 0.0, 0.1, 0.0, NORTH",
    "false, 0.0, 0.0, -0.1, 0.0, SOUTH",
    "false, 0.1, 0.09, 0.1, 0.09, ",
    "false, 0.0, 0.0, 0.0, 0.0, "
  })
  @DisplayName("a drop lands on the floor, or on the wall face that stopped it")
  void face(
      final boolean onGround,
      final double beforeX,
      final double afterX,
      final double beforeZ,
      final double afterZ,
      final Direction expected) {
    assertEquals(
        Optional.ofNullable(expected),
        DropLanding.face(onGround, beforeX, afterX, beforeZ, afterZ));
  }

  @ParameterizedTest(name = "{0}: ({1}, {2}, {3})")
  @CsvSource({
    "UP, 0.5, 1.0, 0.5",
    "WEST, 0.6, 1.0, 0.5",
    "EAST, 0.4, 1.0, 0.5",
    "NORTH, 0.5, 1.0, 0.6",
    "SOUTH, 0.5, 1.0, 0.4"
  })
  @DisplayName("a wall splat sits on the wall, not half a drop away from it")
  void contact(final Direction face, final double x, final double y, final double z) {
    final Vec3d contact = DropLanding.contact(new Vec3d(0.5, 1.0, 0.5), face, 0.1);
    assertEquals(x, contact.x, 1.0e-9);
    assertEquals(y, contact.y, 1.0e-9);
    assertEquals(z, contact.z, 1.0e-9);
  }
}
