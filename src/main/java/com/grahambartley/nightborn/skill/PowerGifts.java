package com.grahambartley.nightborn.skill;

public final class PowerGifts {

  private PowerGifts() {}

  public static int blood(final int base, final boolean batForm, final double cut) {
    if (!batForm || base <= 0) {
      return base;
    }
    return Math.max(1, base + (int) Math.round(Math.min(0.0, cut)));
  }

  public static double cooldown(
      final double general, final boolean tracking, final double trackingShare) {
    return tracking ? general * trackingShare : general;
  }
}
