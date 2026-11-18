package com.grahambartley.nightborn.servant;

public final class FollowRules {

  static final double START_DISTANCE = 6.0;
  static final double STOP_DISTANCE = 3.0;
  static final double TELEPORT_DISTANCE = 24.0;
  static final double GUARD_RADIUS = 8.0;

  private FollowRules() {}

  public static boolean follows(final Stance mode, final boolean ownerHere, final double distance) {
    return mode == Stance.FOLLOW && ownerHere && distance > START_DISTANCE;
  }

  public static boolean keepsFollowing(
      final Stance mode, final boolean ownerHere, final double distance) {
    return mode == Stance.FOLLOW && ownerHere && distance > STOP_DISTANCE;
  }

  public static boolean teleports(
      final double distance, final boolean ownerOnGround, final boolean ownerInDanger) {
    return distance > TELEPORT_DISTANCE && ownerOnGround && !ownerInDanger;
  }

  public static boolean returnsToPost(final Stance mode, final double distanceFromPost) {
    return switch (mode) {
      case FOLLOW -> false;
      case STAY -> distanceFromPost > 1.0;
      case GUARD -> distanceFromPost > GUARD_RADIUS;
    };
  }

  public static boolean guards(final Stance mode, final double targetFromPost) {
    return mode == Stance.GUARD && targetFromPost <= GUARD_RADIUS;
  }
}
