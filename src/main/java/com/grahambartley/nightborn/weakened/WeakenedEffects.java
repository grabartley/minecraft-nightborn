package com.grahambartley.nightborn.weakened;

import com.grahambartley.nightborn.config.WeakenedSettings;

public record WeakenedEffects(double speedMultiplier, double damageMultiplier) {

  public static WeakenedEffects of(final int level, final WeakenedSettings settings) {
    final double share =
        Math.max(0, Math.min(WeakenedSources.CAP, level)) / (double) WeakenedSources.CAP;
    return new WeakenedEffects(
        -settings.slowness() / 100.0 * share, -settings.weakerBlows() / 100.0 * share);
  }
}
