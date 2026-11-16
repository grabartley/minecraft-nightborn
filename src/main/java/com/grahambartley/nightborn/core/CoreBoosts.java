package com.grahambartley.nightborn.core;

import com.grahambartley.nightborn.blood.Starvation;
import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.config.CoreSettings;

public record CoreBoosts(
    double maxHealthMultiplier,
    double bareHandDamage,
    double speedMultiplier,
    double jumpMultiplier) {

  public static final double DOUBLE_HEALTH = 1.0;

  public static CoreBoosts of(
      final CoreSettings settings,
      final BloodSettings blood,
      final boolean emptyMainHand,
      final boolean night,
      final int starving,
      final double skillBareHandDamage) {
    final double nightShare = night ? Starvation.nightGiftShare(starving, blood) : 0.0;
    return new CoreBoosts(
        DOUBLE_HEALTH,
        emptyMainHand
            ? (settings.bareHandDamage() + skillBareHandDamage)
                * Starvation.bareHandShare(starving, blood)
            : 0.0,
        nightShare * settings.nightSpeed() / 100.0,
        nightShare * settings.nightJump() / 100.0);
  }
}
