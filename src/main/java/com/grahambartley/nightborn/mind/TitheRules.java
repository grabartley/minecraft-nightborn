package com.grahambartley.nightborn.mind;

public final class TitheRules {

  static final double SECONDS_PER_MINUTE = 60.0;

  private TitheRules() {}

  public static float kept(final float amount, final double share) {
    return amount * (float) (1.0 - clampShare(share));
  }

  public static float passed(final float amount, final double share) {
    return amount * (float) clampShare(share);
  }

  public static double clampShare(final double share) {
    return Math.max(0.0, Math.min(MindAttributes.MOST_TITHE_SHARE, share));
  }

  public static float spared(final float formed, final float taken) {
    return formed <= 0.0F ? 0.0F : Math.max(0.0F, (formed - taken) / formed);
  }

  public static float owed(final float taken, final double share) {
    final double clamped = clampShare(share);
    return taken <= 0.0F || clamped >= 1.0 ? 0.0F : (float) (taken * clamped / (1.0 - clamped));
  }

  public static double gathered(final double progress, final double perMinute) {
    return progress + Math.max(0.0, perMinute) / SECONDS_PER_MINUTE;
  }
}
