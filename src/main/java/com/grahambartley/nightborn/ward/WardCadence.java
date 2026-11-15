package com.grahambartley.nightborn.ward;

public final class WardCadence {

  private WardCadence() {}

  public static boolean due(final long time, final int entityId, final int interval) {
    return interval <= 1 || Math.floorMod(time + entityId, (long) interval) == 0;
  }
}
