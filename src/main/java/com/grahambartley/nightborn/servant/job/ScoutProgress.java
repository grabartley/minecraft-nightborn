package com.grahambartley.nightborn.servant.job;

public record ScoutProgress(double best, int sinceBest) {

  public static final ScoutProgress START = new ScoutProgress(Double.MAX_VALUE, 0);

  public ScoutProgress step(final double distance, final int ticks) {
    return distance < best
        ? new ScoutProgress(distance, 0)
        : new ScoutProgress(best, sinceBest + ticks);
  }

  public boolean lost(final int giveUpTicks) {
    return sinceBest >= giveUpTicks;
  }

  public static boolean underway(
      final boolean returning, final boolean daytime, final boolean awayFromHome) {
    return returning || daytime || awayFromHome;
  }

  public static boolean arrived(final double distanceSquared, final int sight) {
    return distanceSquared <= (double) sight * sight;
  }
}
