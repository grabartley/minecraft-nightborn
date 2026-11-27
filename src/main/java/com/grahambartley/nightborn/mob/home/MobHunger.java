package com.grahambartley.nightborn.mob.home;

import com.grahambartley.nightborn.blood.BloodAttachment;

public final class MobHunger {

  private MobHunger() {}

  public static boolean hungry(final int blood, final int threshold) {
    return blood < threshold;
  }

  public static int startingBlood(final int threshold, final int roll) {
    final int lowest = Math.max(0, Math.min(BloodAttachment.FULL, threshold));
    return lowest + Math.floorMod(roll, BloodAttachment.FULL - lowest + 1);
  }

  public static int hungryBlood(final int threshold, final int roll) {
    if (threshold <= 0) {
      return startingBlood(threshold, roll);
    }
    final int highest = Math.min(threshold, BloodAttachment.FULL + 1) - 1;
    final int lowest = highest / 2;
    return lowest + Math.floorMod(roll, highest - lowest + 1);
  }
}
