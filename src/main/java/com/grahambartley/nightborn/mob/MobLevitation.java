package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.MobSettings;

public final class MobLevitation {

  static final int TICKS_PER_SECOND = 20;

  private MobLevitation() {}

  public static boolean wants(
      final boolean levitating,
      final boolean fighting,
      final float roll,
      final MobSettings settings) {
    if (fighting && settings.vampireLevitateSeconds() > 0) {
      return true;
    }
    return levitating
        ? roll >= chancePerTick(settings.vampireLandSeconds())
        : roll < chancePerTick(settings.vampireLevitateSeconds());
  }

  static float chancePerTick(final int seconds) {
    return seconds <= 0 ? 0.0F : 1.0F / (seconds * TICKS_PER_SECOND);
  }
}
