package com.grahambartley.nightborn.vanish;

import com.grahambartley.nightborn.skill.PowerHeading;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public final class VanishSpots {

  private VanishSpots() {}

  public static List<PowerHeading> headings(
      final boolean dodging, final PowerHeading held, final boolean leftFirst) {
    if (dodging) {
      return leftFirst
          ? List.of(PowerHeading.LEFT, PowerHeading.RIGHT, PowerHeading.BACK)
          : List.of(PowerHeading.RIGHT, PowerHeading.LEFT, PowerHeading.BACK);
    }
    return List.of(held == PowerHeading.NONE ? PowerHeading.BACK : held);
  }

  public static Vec3d facing(final float yaw) {
    final double radians = Math.toRadians(yaw);
    return new Vec3d(-Math.sin(radians), 0.0, Math.cos(radians));
  }

  public static Vec3d toward(final Vec3d from, final Vec3d to, final Vec3d fallback) {
    final Vec3d flat = new Vec3d(to.x - from.x, 0.0, to.z - from.z);
    return flat.lengthSquared() < 1.0E-6 ? fallback : flat.normalize();
  }

  public static Vec3d wanted(
      final Vec3d eye, final Vec3d facing, final PowerHeading heading, final double distance) {
    final Vec3d way =
        switch (heading) {
          case FORWARD -> facing;
          case LEFT -> new Vec3d(facing.z, 0.0, -facing.x);
          case RIGHT -> new Vec3d(-facing.z, 0.0, facing.x);
          case BACK, NONE -> facing.multiply(-1.0);
        };
    return eye.add(way.multiply(distance));
  }
}
