package com.grahambartley.nightborn.skill;

import java.util.Collection;

public final class PowerGifts {

  public static final double MOST_COOLDOWN_CUT = PowerGift.MOST_COOLDOWN_CUT;

  private PowerGifts() {}

  public static int blood(final int base, final boolean batForm, final double cut) {
    return blood(base, batForm, cut, 0);
  }

  public static int blood(
      final int base, final boolean batForm, final double batCut, final int branchCut) {
    if (base <= 0) {
      return base;
    }
    final int batOff = batForm ? (int) Math.round(Math.min(0.0, batCut)) : 0;
    if (batOff == 0 && branchCut <= 0) {
      return base;
    }
    return Math.max(1, base + batOff - Math.max(0, branchCut));
  }

  public static double cooldown(
      final double general, final boolean tracking, final double trackingShare) {
    return tracking ? general * trackingShare : general;
  }

  public static double cooldown(
      final double general,
      final boolean tracking,
      final double trackingShare,
      final double branchCut) {
    return Math.max(
        1.0 - MOST_COOLDOWN_CUT,
        cooldown(general, tracking, trackingShare) * (1.0 - Math.max(0.0, branchCut)));
  }

  public static int bloodCut(final Collection<PowerGift> gifts) {
    return gifts.stream().mapToInt(PowerGift::bloodCut).sum();
  }

  public static double branchCooldown(final double branchCut) {
    return cooldown(1.0, false, 1.0, branchCut);
  }

  public static boolean refundsOnDodge(final Collection<PowerGift> gifts) {
    return gifts.stream().anyMatch(PowerGift::dodgeRefund);
  }

  public static double cooldownCut(final Collection<PowerGift> gifts) {
    return Math.min(MOST_COOLDOWN_CUT, gifts.stream().mapToDouble(PowerGift::cooldownCut).sum());
  }
}
