package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.config.VillageSettings;

public enum VillageStep {
  CALM,
  WARY,
  WARDED,
  HUNTED;

  public static VillageStep of(final int suspicion, final VillageSettings settings) {
    if (suspicion >= settings.huntedAt()) {
      return HUNTED;
    }
    if (suspicion >= settings.wardedAt()) {
      return WARDED;
    }
    return suspicion >= settings.waryAt() ? WARY : CALM;
  }

  public boolean atLeast(final VillageStep other) {
    return ordinal() >= other.ordinal();
  }
}
