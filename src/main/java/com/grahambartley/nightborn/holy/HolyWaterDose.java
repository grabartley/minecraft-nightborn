package com.grahambartley.nightborn.holy;

import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.weakened.WeakenedSources;

public record HolyWaterDose(float damage, int strength, int ticks) {

  static final int TICKS_PER_SECOND = 20;

  public static HolyWaterDose of(
      final GearSettings settings, final double proximity, final double faithMultiplier) {
    final double scale = Math.max(0.0, Math.min(1.0, proximity)) * Math.max(0.0, faithMultiplier);
    final double reach = Math.max(0.0, Math.min(1.0, proximity));
    return new HolyWaterDose(
        (float) (settings.holyWaterDamage() * scale),
        WeakenedSources.clampStrength(Math.round(settings.holyWaterStrength() * scale)),
        (int) Math.round(settings.holyWaterSeconds() * TICKS_PER_SECOND * reach));
  }

  public boolean isEmpty() {
    return damage <= 0.0F && (strength <= 0 || ticks <= 0);
  }
}
