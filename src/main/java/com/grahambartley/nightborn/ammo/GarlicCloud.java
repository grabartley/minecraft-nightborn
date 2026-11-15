package com.grahambartley.nightborn.ammo;

import com.grahambartley.nightborn.config.GearSettings;

public record GarlicCloud(
    float radius, int durationTicks, float radiusGrowth, int strength, int weakenTicks) {

  static final int TICKS_PER_SECOND = 20;
  static final float SHRINK_TO = 0.5F;

  public static GarlicCloud of(final GearSettings settings) {
    final float radius = settings.garlicBombRadius();
    final int duration = settings.garlicBombCloudSeconds() * TICKS_PER_SECOND;
    return new GarlicCloud(
        radius,
        duration,
        -radius * (1.0F - SHRINK_TO) / duration,
        settings.garlicBombStrength(),
        settings.garlicBombSeconds() * TICKS_PER_SECOND);
  }
}
