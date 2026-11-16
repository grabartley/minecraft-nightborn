package com.grahambartley.nightborn.thrall;

public final class FollowRules {

  static final double START_DISTANCE = 6.0;
  static final double STOP_DISTANCE = 3.0;
  static final double TELEPORT_DISTANCE = 24.0;
  static final double GUARD_RADIUS = 8.0;

  private FollowRules() {}

  public static boolean follows(
      final ThrallMode mode, final boolean ownerHere, final double distance) {
    return mode == ThrallMode.FOLLOW && ownerHere && distance > START_DISTANCE;
  }

  public static boolean keepsFollowing(
      final ThrallMode mode, final boolean ownerHere, final double distance) {
    return mode == ThrallMode.FOLLOW && ownerHere && distance > STOP_DISTANCE;
  }

  public static boolean teleports(final double distance) {
    return distance > TELEPORT_DISTANCE;
  }

  public static boolean returnsToPost(final ThrallMode mode, final double distanceFromPost) {
    return switch (mode) {
      case FOLLOW -> false;
      case STAY -> distanceFromPost > 1.0;
      case GUARD -> distanceFromPost > GUARD_RADIUS;
    };
  }

  public static boolean guards(final ThrallMode mode, final double targetFromPost) {
    return mode == ThrallMode.GUARD && targetFromPost <= GUARD_RADIUS;
  }
}
