package com.grahambartley.nightborn.seeker;

public final class MarkShare {

  private MarkShare() {}

  public static double of(
      final boolean byHunter, final boolean byAlly, final double own, final double allied) {
    if (byHunter) {
      return Math.max(0.0, own);
    }
    return byAlly ? Math.max(0.0, allied) : 0.0;
  }

  public static float raise(final float amount, final double share) {
    return (float) (amount * (1.0 + Math.max(0.0, share)));
  }
}
