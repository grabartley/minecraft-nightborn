package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.hunter.FaithBoost;
import com.grahambartley.nightborn.weakened.WeakenedSources;

public final class WardStrength {

  private WardStrength() {}

  public static int strings(final int count, final int perString, final int cap) {
    return clamp((long) Math.max(0, count) * Math.max(0, perString), Math.max(0, cap));
  }

  public static int crucifix(final int base, final double faith, final int faithBonusPercent) {
    return clamp(
        Math.round(Math.max(0, base) * FaithBoost.multiplier(faith, faithBonusPercent)),
        WeakenedSources.CAP);
  }

  private static int clamp(final long value, final int cap) {
    return WeakenedSources.clampStrength(Math.min(cap, value));
  }
}
