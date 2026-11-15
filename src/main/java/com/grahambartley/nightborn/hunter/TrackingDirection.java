package com.grahambartley.nightborn.hunter;

import java.util.List;
import net.minecraft.util.math.MathHelper;

public final class TrackingDirection {

  static final List<String> ARROWS = List.of("↓", "↙", "←", "↖", "↑", "↗", "→", "↘", "↓");
  static final float STEP = 45.0F;

  private TrackingDirection() {}

  public static float yawTowards(final double dx, final double dz) {
    return (float) Math.toDegrees(Math.atan2(-dx, dz));
  }

  public static float turn(final double dx, final double dz, final float yaw) {
    return MathHelper.wrapDegrees(yawTowards(dx, dz) - yaw);
  }

  public static String arrow(final double dx, final double dz, final float yaw) {
    return ARROWS.get(Math.round(turn(dx, dz, yaw) / STEP) + 4);
  }
}
