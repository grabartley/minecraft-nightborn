package com.grahambartley.nightborn.servant.job;

public final class BloodRounds {

  public static final int DRAW_TICKS = 20;

  private BloodRounds() {}

  public static boolean equipped(final boolean lancet, final boolean emptyBottle) {
    return lancet && emptyBottle;
  }

  public static boolean drawsNow(final int ticksSinceLastDraw) {
    return ticksSinceLastDraw >= DRAW_TICKS;
  }
}
