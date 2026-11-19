package com.grahambartley.nightborn.faith;

import net.minecraft.util.math.Vec3d;

public final class PrayerRules {

  public static final int PULSE_TICKS = 20;

  private PrayerRules() {}

  public static float boosted(final float health, final double boost) {
    return (float) (Math.max(0.0F, health) * Math.max(1.0, boost));
  }

  public static boolean inside(final double squaredDistance, final float radius) {
    return squaredDistance <= (double) radius * radius;
  }

  public static double flat(final Vec3d at, final Vec3d centre) {
    final double dx = at.x - centre.x;
    final double dz = at.z - centre.z;
    return dx * dx + dz * dz;
  }

  public static boolean pulses(final long startedAt, final long now) {
    return now > startedAt && (now - startedAt) % PULSE_TICKS == 0;
  }

  public static boolean lasts(final long now, final long until) {
    return now < until;
  }

  public static int counted(final float healed) {
    return Math.max(0, Math.round(healed));
  }
}
