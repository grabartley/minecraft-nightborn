package com.grahambartley.nightborn.mob.household;

import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;

public final class Walk {

  static final int REPATH_TICKS = 20;
  static final double SPEED = 0.7;

  private final PathAwareEntity walker;
  private int repath;

  public Walk(final PathAwareEntity walker) {
    this.walker = walker;
  }

  public static boolean arrived(final double distanceSquared, final double reach) {
    return distanceSquared <= reach * reach;
  }

  public boolean toward(final Vec3d spot, final double reach) {
    if (arrived(walker.getPos().squaredDistanceTo(spot), reach)) {
      walker.getNavigation().stop();
      return true;
    }
    if (--repath <= 0) {
      repath = REPATH_TICKS;
      walker.getNavigation().startMovingTo(spot.x, spot.y, spot.z, SPEED);
    }
    return false;
  }
}
