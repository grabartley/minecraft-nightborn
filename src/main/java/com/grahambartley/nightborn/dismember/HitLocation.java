package com.grahambartley.nightborn.dismember;

import java.util.Optional;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class HitLocation {

  private HitLocation() {}

  public static Optional<BodyPart> part(
      final BodyZones zones,
      final Box body,
      final Vec3d from,
      final Vec3d to,
      final float bodyYaw) {
    final Vec3d point = meet(body, from, to);
    return zones.at(height(body, point), side(body, point, bodyYaw));
  }

  static Vec3d meet(final Box body, final Vec3d from, final Vec3d to) {
    return body.raycast(from, to).orElseGet(() -> nearest(body, from, to));
  }

  static Vec3d nearest(final Box body, final Vec3d from, final Vec3d to) {
    final Vec3d line = to.subtract(from);
    final double length = line.lengthSquared();
    final double t =
        length == 0.0
            ? 0.0
            : MathHelper.clamp(body.getCenter().subtract(from).dotProduct(line) / length, 0.0, 1.0);
    final Vec3d onLine = from.add(line.multiply(t));
    return new Vec3d(
        MathHelper.clamp(onLine.x, body.minX, body.maxX),
        MathHelper.clamp(onLine.y, body.minY, body.maxY),
        MathHelper.clamp(onLine.z, body.minZ, body.maxZ));
  }

  static double height(final Box body, final Vec3d point) {
    final double tall = body.getLengthY();
    return tall <= 0.0 ? 0.5 : MathHelper.clamp((point.y - body.minY) / tall, 0.0, 1.0);
  }

  static double side(final Box body, final Vec3d point, final float bodyYaw) {
    final double halfWidth = body.getLengthX() / 2.0;
    if (halfWidth <= 0.0) {
      return 0.0;
    }
    final double radians = Math.toRadians(bodyYaw);
    final Vec3d center = body.getCenter();
    final double rightX = -Math.cos(radians);
    final double rightZ = -Math.sin(radians);
    return ((point.x - center.x) * rightX + (point.z - center.z) * rightZ) / halfWidth;
  }
}
