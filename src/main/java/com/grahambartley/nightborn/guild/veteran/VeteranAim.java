package com.grahambartley.nightborn.guild.veteran;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.Vec3d;

public final class VeteranAim {

  public static final double ARC_SPACING = 2.0;
  public static final double DRAG_GAP = 1.5;

  private VeteranAim() {}

  public static List<Vec3d> line(final Vec3d from, final Vec3d to, final double step) {
    final Vec3d flat = new Vec3d(to.x - from.x, 0.0, to.z - from.z);
    final double length = flat.length();
    final List<Vec3d> points = new ArrayList<>();
    if (length <= 0.0 || step <= 0.0) {
      return points;
    }
    final Vec3d along = flat.multiply(1.0 / length);
    for (double at = step; at <= length; at += step) {
      points.add(new Vec3d(from.x + along.x * at, from.y, from.z + along.z * at));
    }
    return points;
  }

  public static List<Vec3d> arc(
      final Vec3d from, final Vec3d target, final int count, final double spacing) {
    final Vec3d flat = new Vec3d(target.x - from.x, 0.0, target.z - from.z);
    final Vec3d across =
        flat.lengthSquared() <= 0.0
            ? new Vec3d(1.0, 0.0, 0.0)
            : new Vec3d(-flat.z, 0.0, flat.x).normalize();
    final List<Vec3d> spots = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      final double offset = (i - (count - 1) / 2.0) * spacing;
      spots.add(target.add(across.multiply(offset)));
    }
    return spots;
  }

  public static Vec3d dragSpot(final Vec3d veteran, final Vec3d target, final double gap) {
    final Vec3d flat = new Vec3d(target.x - veteran.x, 0.0, target.z - veteran.z);
    if (flat.lengthSquared() <= gap * gap) {
      return target;
    }
    return veteran.add(flat.normalize().multiply(gap));
  }
}
