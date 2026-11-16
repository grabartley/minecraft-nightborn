package com.grahambartley.nightborn.occult;

public final class FogRules {

  static final int PULSE_TICKS = 10;

  private FogRules() {}

  public static boolean pulses(final long now, final long startedAt) {
    return (now - startedAt) % PULSE_TICKS == 0;
  }

  public static boolean lasts(final long now, final long endsAt) {
    return now < endsAt;
  }

  public static boolean covers(final double distanceSquared, final float radius) {
    return distanceSquared <= (double) radius * radius;
  }

  public static boolean touches(
      final boolean alive,
      final boolean caster,
      final boolean servesCaster,
      final boolean spectating) {
    return alive && !caster && !servesCaster && !spectating;
  }
}
