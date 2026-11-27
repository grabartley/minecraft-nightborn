package com.grahambartley.nightborn.guild.rite;

public final class RiteAccess {

  static final int TICKS_PER_SECOND = 20;

  private RiteAccess() {}

  public static boolean offered(
      final boolean vampire, final boolean hunter, final boolean needsOath) {
    return !vampire && (hunter || !needsOath);
  }

  public static long waitSeconds(final long now, final long readyAt) {
    return Math.max(0L, Math.ceilDiv(readyAt - now, TICKS_PER_SECOND));
  }

  public static long readyAt(final long now, final int cooldownSeconds) {
    return now + (long) cooldownSeconds * TICKS_PER_SECOND;
  }
}
