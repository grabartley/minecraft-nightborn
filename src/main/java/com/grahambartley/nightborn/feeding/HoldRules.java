package com.grahambartley.nightborn.feeding;

import net.minecraft.util.math.Vec3d;

final class HoldRules {

  static final int HOLD_TICKS = 4;
  static final int REFRESH_BELOW_TICKS = 2;
  static final int SLOWNESS_AMPLIFIER = 6;
  static final double DRIFT_TOLERANCE = 0.05;

  private HoldRules() {}

  static Vec3d pinned(final Vec3d velocity, final boolean holdsHeight) {
    return new Vec3d(0.0, holdsHeight ? 0.0 : velocity.y, 0.0);
  }

  static Vec3d anchored(final Vec3d anchor, final Vec3d position, final boolean holdsHeight) {
    return new Vec3d(anchor.x, holdsHeight ? anchor.y : position.y, anchor.z);
  }

  static boolean drifted(final Vec3d anchor, final Vec3d position, final boolean holdsHeight) {
    return anchored(anchor, position, holdsHeight).squaredDistanceTo(position)
        > DRIFT_TOLERANCE * DRIFT_TOLERANCE;
  }

  static boolean holdsHeight(final boolean noGravity, final boolean tagged, final boolean flying) {
    return noGravity || tagged || flying;
  }

  static boolean slows(final boolean player) {
    return !player;
  }

  static boolean needsRefresh(final int remainingTicks, final int amplifier) {
    return amplifier < SLOWNESS_AMPLIFIER || remainingTicks < REFRESH_BELOW_TICKS;
  }
}
