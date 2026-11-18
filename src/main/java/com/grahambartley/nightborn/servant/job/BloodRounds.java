package com.grahambartley.nightborn.servant.job;

public final class BloodRounds {

  public static final int DRAW_TICKS = 20;

  private BloodRounds() {}

  public static boolean prey(final boolean animal, final boolean tame, final boolean spared) {
    return animal && !tame && !spared;
  }

  public static boolean drawsNow(final int ticksSinceLastDraw) {
    return ticksSinceLastDraw >= DRAW_TICKS;
  }
}
