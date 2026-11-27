package com.grahambartley.nightborn.dismember;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class HitLocationTest {

  private static final Box BODY = new Box(-0.3, 0.0, -0.3, 0.3, 1.8, 0.3);

  @ParameterizedTest(name = "facing {0}, struck at x {1} y {2}: {3}")
  @CsvSource({
    "0, -0.25, 1.1, RIGHT_ARM",
    "0, 0.25, 1.1, LEFT_ARM",
    "0, 0.0, 1.1, ",
    "0, 0.0, 1.6, HEAD",
    "0, -0.1, 0.4, RIGHT_LEG",
    "0, 0.1, 0.4, LEFT_LEG",
    "180, -0.25, 1.1, LEFT_ARM",
    "180, 0.1, 0.4, RIGHT_LEG"
  })
  @DisplayName("a blow from the front lands on the part at that height and side")
  void fromFront(final float yaw, final double x, final double y, final BodyPart part) {
    final double front = yaw == 0.0F ? 2.0 : -2.0;
    assertEquals(
        Optional.ofNullable(part),
        HitLocation.part(
            BodyZones.DEFAULT, BODY, new Vec3d(x, y, front), new Vec3d(x, y, -front), yaw));
  }

  @ParameterizedTest(name = "yaw {0}, point x {1} z {2}: side {3}")
  @CsvSource({"0, -0.3, 0, 1", "0, 0.3, 0, -1", "90, 0, -0.3, 1", "90, 0, 0.3, -1", "0, 0, 0.3, 0"})
  @DisplayName("the side is measured across the body from the way it faces, right positive")
  void side(final float yaw, final double x, final double z, final double side) {
    assertEquals(side, HitLocation.side(BODY, new Vec3d(x, 1.0, z), yaw), 1.0E-9);
  }

  @ParameterizedTest(name = "y {0}: {1}")
  @CsvSource({"0.0, 0.0", "0.9, 0.5", "1.8, 1.0", "-1.0, 0.0", "3.0, 1.0"})
  @DisplayName("height is the share of the body below the point")
  void height(final double y, final double share) {
    assertEquals(share, HitLocation.height(BODY, new Vec3d(0.0, y, 0.0)), 1.0E-9);
  }

  @ParameterizedTest(name = "a line at y {0} that misses the body meets it at y {1}")
  @CsvSource({"1.1, 1.1", "2.5, 1.8", "-0.5, 0.0"})
  @DisplayName("a line that just misses the body still lands on its nearest point")
  void nearestWhenMissed(final double y, final double landed) {
    final Vec3d point = HitLocation.meet(BODY, new Vec3d(-2.0, y, 1.0), new Vec3d(2.0, y, 1.0));
    assertEquals(landed, point.y, 1.0E-9);
    assertEquals(0.3, point.z, 1.0E-9);
  }
}
