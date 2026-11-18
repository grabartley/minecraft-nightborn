package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.Locale;
import net.minecraft.util.Identifier;

public enum FamiliarMode {
  FIGHT("fight"),
  CARRY("carry"),
  SCOUT("scout");

  private final Identifier icon;

  FamiliarMode(final String icon) {
    this.icon = WheelEntry.icon(icon);
  }

  public Identifier icon() {
    return icon;
  }

  public boolean passesThroughBlocks() {
    return this == SCOUT;
  }

  public String translationKey() {
    return "nightborn.familiar.mode." + name().toLowerCase(Locale.ROOT);
  }

  public String labelKey() {
    return "nightborn.familiar.order." + name().toLowerCase(Locale.ROOT);
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
