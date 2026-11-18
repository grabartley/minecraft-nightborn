package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.downed.DownedState;

public record FightRules(int emptyTicks, int stakeWindowTicks, int risePercent) {

  public DownedState window(final DownedState downed) {
    return downed.next(DownedState.Phase.DOWNED, stakeWindowTicks);
  }

  public float risen(final float maxHealth, final double lastShare) {
    return (float) Math.max(1.0, maxHealth * lastShare * risePercent / 100.0);
  }

  public static boolean engaged(
      final boolean hasTarget, final int phase, final boolean hurt, final boolean downed) {
    return hasTarget || phase > 0 || hurt || downed;
  }

  public boolean resets(final boolean engaged, final long emptyFor, final boolean downed) {
    return emptyTicks > 0 && engaged && emptyFor >= emptyTicks && !downed;
  }
}
