package com.grahambartley.nightborn.raid;

import com.grahambartley.nightborn.core.NightTime;

public final class TrailRules {

  private TrailRules() {}

  public static long until(final long time, final long timeOfDay) {
    final long toDawn = Math.floorMod(NightTime.DAWN - timeOfDay, NightTime.DAY_LENGTH);
    return time + (toDawn == 0 ? NightTime.DAY_LENGTH : toDawn);
  }

  public static boolean over(final long now, final long until) {
    return now >= until;
  }
}
