package com.grahambartley.nightborn.mob.household;

public final class Rescan {

  private Rescan() {}

  public static boolean due(final long scannedAt, final long now, final long interval) {
    return scannedAt == Long.MIN_VALUE || now < scannedAt || now - scannedAt >= interval;
  }
}
