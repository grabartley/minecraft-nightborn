package com.grahambartley.nightborn.servant;

public final class TargetRules {

  static final int OWNER_FIGHT_TICKS = 100;

  private TargetRules() {}

  public static boolean ownerStillFighting(final int ownerAge, final int lastAttackAge) {
    final int since = ownerAge - lastAttackAge;
    return since >= 0 && since <= OWNER_FIGHT_TICKS;
  }

  public static boolean mayAttack(
      final boolean alive,
      final boolean isOwner,
      final boolean servesSameOwner,
      final boolean untouchablePlayer) {
    return alive && !isOwner && !servesSameOwner && !untouchablePlayer;
  }
}
