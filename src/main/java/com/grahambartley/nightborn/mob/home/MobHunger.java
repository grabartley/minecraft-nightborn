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
}
