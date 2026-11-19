package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.config.CoffinSettings;

public record RestedBoost(double speedMultiplier, double attackDamage) {

  public static final RestedBoost NONE = new RestedBoost(0.0, 0.0);

  public static RestedBoost of(final CoffinSettings settings, final int level) {
    if (level < RestedRules.RESTED) {
      return NONE;
    }
    final boolean atHome = level >= RestedRules.RESTED_AT_HOME;
    return new RestedBoost(
        (atHome ? settings.houseRestedSpeed() : settings.restedSpeed()) / 100.0,
        atHome ? settings.houseRestedDamage() : settings.restedDamage());
  }
}
