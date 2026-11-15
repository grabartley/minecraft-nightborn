package com.grahambartley.nightborn.feeding;

import net.minecraft.util.math.Vec3d;

final class HoldRules {

  static final int HOLD_TICKS = 4;
  static final int REFRESH_BELOW_TICKS = 2;
  static final int SLOWNESS_AMPLIFIER = 6;

  private HoldRules() {}

  static Vec3d pinned(final Vec3d velocity) {
    return new Vec3d(0.0, velocity.y, 0.0);
  }

  static boolean needsRefresh(final int remainingTicks, final int amplifier) {
    return amplifier < SLOWNESS_AMPLIFIER || remainingTicks < REFRESH_BELOW_TICKS;
  }
}
