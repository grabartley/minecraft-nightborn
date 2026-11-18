package com.grahambartley.nightborn.servant.job;

public final class WatchRules {

  private WatchRules() {}

  public static boolean warns(final boolean seesHunters, final boolean sawHuntersBefore) {
    return seesHunters && !sawHuntersBefore;
  }

  public static boolean intrudes(
      final boolean monster, final boolean hunter, final boolean spared) {
    return hunter || (monster && !spared);
  }

  public static boolean watched(final double distanceSquared, final int watchRange) {
    return distanceSquared <= (double) watchRange * watchRange;
  }
}
