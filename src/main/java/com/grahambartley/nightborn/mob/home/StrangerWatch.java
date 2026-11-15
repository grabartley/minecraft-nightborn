package com.grahambartley.nightborn.mob.home;

public final class StrangerWatch {

  public static final double NOTICE_RANGE = 12.0;
  static final double FOLLOW_BEYOND = 6.0;
  static final double WARN_WITHIN = 3.0;

  public enum Move {
    FOLLOW,
    WATCH,
    WARN
  }

  private StrangerWatch() {}

  public static boolean stranger(
      final boolean player, final boolean vampire, final boolean creativeOrSpectator) {
    return player && !vampire && !creativeOrSpectator;
  }

  public static Move move(final double distance) {
    if (distance > FOLLOW_BEYOND) {
      return Move.FOLLOW;
    }
    return distance < WARN_WITHIN ? Move.WARN : Move.WATCH;
  }

  public static boolean keepsWatching(final double distance, final boolean nearHome) {
    return distance <= NOTICE_RANGE * 1.5 && nearHome;
  }
}
