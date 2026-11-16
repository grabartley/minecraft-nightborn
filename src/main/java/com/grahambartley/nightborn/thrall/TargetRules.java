package com.grahambartley.nightborn.thrall;

public final class TargetRules {

  private TargetRules() {}

  public static boolean mayAttack(
      final boolean alive,
      final boolean isOwner,
      final boolean servesSameOwner,
      final boolean untouchablePlayer) {
    return alive && !isOwner && !servesSameOwner && !untouchablePlayer;
  }
}
