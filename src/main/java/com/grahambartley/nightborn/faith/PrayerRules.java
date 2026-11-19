package com.grahambartley.nightborn.faith;

public final class PrayerRules {

  public static final int PULSE_TICKS = 20;

  private PrayerRules() {}

  public static float boosted(final float health, final double boost) {
    return (float) (Math.max(0.0F, health) * Math.max(1.0, boost));
  }

  public static boolean pulses(final long startedAt, final long now) {
    return now >= startedAt && (now - startedAt) % PULSE_TICKS == 0;
  }

  public static int counted(final float healed) {
    return Math.max(0, Math.round(healed));
  }
}
