package com.grahambartley.nightborn.levitation;

import java.util.OptionalDouble;

public final class LevitationLift {

  public static final double HOVER_HEIGHT = 1.0;
  public static final float AIR_SPEED_FACTOR = (1.0F - 0.91F) / (1.0F - 0.6F * 0.91F);

  static final double MAX_RISE = 0.42;
  static final double RISE_SHARE = 0.3;
  static final double MIN_RISE = 0.05;
  static final double SETTLE_SHARE = 0.3;
  static final double SINK_SPEED = 0.05;

  private LevitationLift() {}

  public static double verticalVelocity(final double feetY, final OptionalDouble surface) {
    if (surface.isEmpty()) {
      return -SINK_SPEED;
    }
    final double gap = surface.getAsDouble() + HOVER_HEIGHT - feetY;
    if (gap > 0.0) {
      return Math.min(gap, Math.min(MAX_RISE, Math.max(MIN_RISE, gap * RISE_SHARE)));
    }
    return Math.max(-SINK_SPEED, gap * SETTLE_SHARE);
  }
}
