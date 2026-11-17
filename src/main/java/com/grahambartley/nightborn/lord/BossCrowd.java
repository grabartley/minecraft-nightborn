package com.grahambartley.nightborn.lord;

public final class BossCrowd {

  static final double PERCENT = 100.0;

  private BossCrowd() {}

  public static int size(final int held, final int near, final boolean fighting) {
    return fighting ? Math.max(held, near) : near;
  }

  public static double bonus(final int players, final int percentPerExtraPlayer) {
    return Math.max(0, players - 1) * percentPerExtraPlayer / PERCENT;
  }

  public static float healed(final float health, final float maxBefore, final float maxAfter) {
    return maxAfter > maxBefore ? Math.min(maxAfter, health + (maxAfter - maxBefore)) : health;
  }
}
