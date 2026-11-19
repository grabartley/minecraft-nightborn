package com.grahambartley.nightborn.inquisitor;

public final class InquisitorDrain {

  static final float SIP = 1.0F;
  static final float LEAST = 2.0F;
  static final long FED_TICKS = 30L;

  private InquisitorDrain() {}

  public static float downedHealth(final int drainSeconds) {
    return Math.max(LEAST, drainSeconds);
  }

  public static float taken(final float health, final float bite) {
    return bite >= health ? health : Math.min(SIP, Math.max(0.0F, bite));
  }

  public static boolean ends(
      final boolean bitten,
      final boolean byVampire,
      final boolean byTurnedHuman,
      final boolean fromOutside) {
    return !fromOutside && (bitten && byVampire || byTurnedHuman);
  }

  public static boolean fed(final long now, final long bittenAt) {
    return bittenAt <= now && bittenAt > now - FED_TICKS;
  }
}
