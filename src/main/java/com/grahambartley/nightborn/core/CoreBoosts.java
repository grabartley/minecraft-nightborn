package com.grahambartley.nightborn.core;

import com.grahambartley.nightborn.config.CoreSettings;

public record CoreBoosts(
    double maxHealthMultiplier,
    double bareHandDamage,
    double speedMultiplier,
    double jumpMultiplier) {

  public static final double DOUBLE_HEALTH = 1.0;

  public static CoreBoosts of(
      final CoreSettings settings, final boolean emptyMainHand, final boolean night) {
    return new CoreBoosts(
        DOUBLE_HEALTH,
        emptyMainHand ? settings.bareHandDamage() : 0.0,
        night ? settings.nightSpeed() / 100.0 : 0.0,
        night ? settings.nightJump() / 100.0 : 0.0);
  }
}
