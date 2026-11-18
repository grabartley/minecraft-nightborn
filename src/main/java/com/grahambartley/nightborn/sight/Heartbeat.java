package com.grahambartley.nightborn.sight;

public final class Heartbeat {

  static final int PALE = 0xE0B4B4;
  static final int DEEP = 0x8A0303;

  private Heartbeat() {}

  public static double weight(
      final int worthPercent, final int mostWorthPercent, final float health, final float most) {
    if (worthPercent <= 0 || mostWorthPercent <= 0 || most <= 0.0F) {
      return 0.0;
    }
    final double worth = Math.min(1.0, (double) worthPercent / mostWorthPercent);
    final double fill = Math.max(0.0, Math.min(1.0, health / most));
    return worth * fill;
  }

  public static int colour(final double weight) {
    final double t = Math.max(0.0, Math.min(1.0, weight));
    return channel(PALE >> 16, DEEP >> 16, t) << 16
        | channel(PALE >> 8 & 0xFF, DEEP >> 8 & 0xFF, t) << 8
        | channel(PALE & 0xFF, DEEP & 0xFF, t);
  }

  private static int channel(final int from, final int to, final double t) {
    return (int) Math.round(from + (to - from) * t);
  }
}
