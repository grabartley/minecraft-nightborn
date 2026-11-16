package com.grahambartley.nightborn.occult;

import java.util.Locale;

public enum FamiliarMode {
  FIGHT,
  CARRY,
  SCOUT;

  public FamiliarMode next() {
    return values()[(ordinal() + 1) % values().length];
  }

  public boolean passesThroughBlocks() {
    return this == SCOUT;
  }

  public String translationKey() {
    return "nightborn.familiar.mode." + name().toLowerCase(Locale.ROOT);
  }

  public static FamiliarMode byIndex(final int index) {
    return index >= 0 && index < values().length ? values()[index] : FIGHT;
  }

  public static FamiliarMode byName(final String name) {
    for (final FamiliarMode mode : values()) {
      if (mode.name().equals(name)) {
        return mode;
      }
    }
    return FIGHT;
  }
}
