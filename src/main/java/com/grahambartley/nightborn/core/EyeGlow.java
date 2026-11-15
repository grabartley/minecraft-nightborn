package com.grahambartley.nightborn.core;

public final class EyeGlow {

  public static final int AFTER_FEEDING_TICKS = 100;
  public static final float LOW_HEALTH_SHARE = 0.3F;

  private EyeGlow() {}

  public static boolean glowing(
      final boolean feeding, final long ticksSinceFed, final float health, final float maxHealth) {
    return feeding
        || (ticksSinceFed >= 0 && ticksSinceFed < AFTER_FEEDING_TICKS)
        || (maxHealth > 0.0F && health < maxHealth * LOW_HEALTH_SHARE);
  }
}
