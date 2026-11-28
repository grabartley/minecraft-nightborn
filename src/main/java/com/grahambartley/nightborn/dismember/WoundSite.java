package com.grahambartley.nightborn.dismember;

import net.minecraft.util.math.Vec3d;

public final class WoundSite {

  static final double LEG_SIDE = 0.5;
  static final double SHOULDER_DROP = 0.0625;

  private WoundSite() {}

  public static Vec3d offset(
      final BodyZones zones,
      final BodyPart part,
      final double width,
      final double height,
      final float bodyYaw) {
    final double halfWidth = width / 2.0;
    return switch (part) {
      case HEAD -> new Vec3d(0.0, height * (1.0 - zones.head()), 0.0);
      case RIGHT_ARM, LEFT_ARM ->
          right(bodyYaw)
              .multiply(sign(part) * (1.0 + zones.trunk()) / 2.0 * halfWidth)
              .add(0.0, height * (1.0 - zones.head() - SHOULDER_DROP), 0.0);
      case RIGHT_LEG, LEFT_LEG ->
          right(bodyYaw)
              .multiply(sign(part) * LEG_SIDE * halfWidth)
              .add(0.0, height * zones.hip(), 0.0);
    };
  }

  public static Vec3d outward(final BodyPart part, final float bodyYaw) {
    return switch (part) {
      case HEAD -> new Vec3d(0.0, 1.0, 0.0);
      case RIGHT_ARM, LEFT_ARM -> right(bodyYaw).multiply(sign(part)).add(0.0, 0.25, 0.0);
      case RIGHT_LEG, LEFT_LEG -> right(bodyYaw).multiply(sign(part) * 0.5).add(0.0, -0.5, 0.0);
    };
  }

  static Vec3d right(final float bodyYaw) {
    final double radians = Math.toRadians(bodyYaw);
    return new Vec3d(-Math.cos(radians), 0.0, -Math.sin(radians));
  }

  private static double sign(final BodyPart part) {
    return part == BodyPart.RIGHT_ARM || part == BodyPart.RIGHT_LEG ? 1.0 : -1.0;
  }
}
