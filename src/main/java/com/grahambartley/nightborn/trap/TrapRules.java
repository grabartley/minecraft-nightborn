package com.grahambartley.nightborn.trap;

public final class TrapRules {

  public enum Outcome {
    NOTHING,
    CATCH,
    SPRING
  }

  private TrapRules() {}

  public static Outcome meets(
      final boolean vampire,
      final boolean bat,
      final boolean mist,
      final boolean catchesBatsAndMist,
      final boolean reaches,
      final boolean player,
      final boolean spared,
      final boolean stepped) {
    if (vampire && reaches && (catchesBatsAndMist || !bat && !mist)) {
      return Outcome.CATCH;
    }
    return stepped && player && !vampire && !spared ? Outcome.SPRING : Outcome.NOTHING;
  }

  public static boolean within(
      final boolean stepped,
      final float triggerRadius,
      final double squaredDistance,
      final boolean onTrapBlock) {
    return stepped ? onTrapBlock : squaredDistance <= (double) triggerRadius * triggerRadius;
  }

  public static int overLimit(final int set, final int limit) {
    return Math.max(0, set - limit + 1);
  }

  public static boolean spares(final boolean ownSense, final boolean self, final boolean ally) {
    return ownSense && (self || ally);
  }

  public static boolean allied(
      final boolean same,
      final boolean ownerHunter,
      final boolean otherHunter,
      final boolean sameTeam) {
    return same || sameTeam || ownerHunter && otherHunter;
  }
}
