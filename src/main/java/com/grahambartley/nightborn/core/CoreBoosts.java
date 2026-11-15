package com.grahambartley.nightborn.core;

import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.config.CoreSettings;

public record CoreBoosts(
    double maxHealthMultiplier,
    double bareHandDamage,
    double speedMultiplier,
    double jumpMultiplier) {

  public static final double DOUBLE_HEALTH = 1.0;

  public static CoreBoosts of(
      final CoreSettings settings,
      final boolean emptyMainHand,
      final boolean night,
      final int starving) {
    final double nightShare = night ? Starvation.nightGiftShare(starving) : 0.0;
    return new CoreBoosts(
        DOUBLE_HEALTH,
        emptyMainHand ? settings.bareHandDamage() * Starvation.bareHandShare(starving) : 0.0,
        nightShare * settings.nightSpeed() / 100.0,
        nightShare * settings.nightJump() / 100.0);
  }
}
