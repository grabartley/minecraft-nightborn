package com.grahambartley.nightborn.thrall;

import java.util.Locale;
import net.minecraft.util.Identifier;

public enum ThrallMode {
  FOLLOW("minecraft:lead"),
  STAY("minecraft:campfire"),
  GUARD("minecraft:iron_sword");

  private final Identifier icon;

  ThrallMode(final String icon) {
    this.icon = Identifier.of(icon);
  }

  public Identifier icon() {
    return icon;
  }

  public String translationKey() {
    return "nightborn.thrall.mode." + name().toLowerCase(Locale.ROOT);
  }

  public String labelKey() {
    return "nightborn.thrall.order." + name().toLowerCase(Locale.ROOT);
  }

  public static ThrallMode byIndex(final int index) {
    return index >= 0 && index < values().length ? values()[index] : FOLLOW;
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
