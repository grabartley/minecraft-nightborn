package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.config.FamiliarSettings;

public final class Slights {

  public static final int COOLING_TICKS = 100;

  private Slights() {}

  public static boolean retaliates(final boolean byOwner) {
    return !byOwner;
  }

  public static int loyaltyLost(
      final boolean byOwner,
      final boolean bite,
      final long ticksSinceLast,
      final FamiliarSettings settings) {
    if (!byOwner || ticksSinceLast < COOLING_TICKS) {
      return 0;
    }
    return bite ? settings.bittenLoyalty() : settings.hurtLoyalty();
  }
}
