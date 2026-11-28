package com.grahambartley.nightborn.dismember;

import com.grahambartley.nightborn.config.DismembermentSettings;

public final class BleedRules {

  static final int TICKS_PER_SECOND = 20;
  static final float HEALTH_PER_PULSE = 1.0F;
  static final float HEALTH_FLOOR = 2.0F;
  static final int BLOOD_PER_PULSE = 1;

  private BleedRules() {}

  public static int bleedTicks(final DismembermentSettings settings, final boolean vampire) {
    return (vampire ? settings.vampireBleedSeconds() : settings.humanBleedSeconds())
        * TICKS_PER_SECOND;
  }

  public static boolean pulses(final long cutAt, final long now, final int pulseSeconds) {
    final long since = now - cutAt;
    return since > 0 && since % ((long) pulseSeconds * TICKS_PER_SECOND) == 0;
  }

  public static float healthLoss(final float health) {
    return Math.max(0.0F, Math.min(HEALTH_PER_PULSE, health - HEALTH_FLOOR));
  }

  public static int bloodLeft(final int blood) {
    return Math.max(0, blood - BLOOD_PER_PULSE);
  }
}
