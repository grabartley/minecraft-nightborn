package com.grahambartley.nightborn.thrall;

import java.util.Locale;

public enum ThrallMode {
  FOLLOW,
  STAY,
  GUARD;

  public ThrallMode next() {
    return values()[(ordinal() + 1) % values().length];
  }

  public String translationKey() {
    return "nightborn.thrall.mode." + name().toLowerCase(Locale.ROOT);
  }

  public static ThrallMode byName(final String name) {
    for (final ThrallMode mode : values()) {
      if (mode.name().equals(name)) {
        return mode;
      }
    }
    return FOLLOW;
  }
}
