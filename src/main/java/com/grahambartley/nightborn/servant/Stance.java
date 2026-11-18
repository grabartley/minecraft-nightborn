package com.grahambartley.nightborn.servant;

import java.util.Locale;
import net.minecraft.util.Identifier;

public enum Stance {
  FOLLOW("minecraft:lead"),
  STAY("minecraft:campfire"),
  GUARD("minecraft:iron_sword");

  private final Identifier icon;

  Stance(final String icon) {
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

  public static Stance byIndex(final int index) {
    return index >= 0 && index < values().length ? values()[index] : FOLLOW;
  }

  public static Stance byName(final String name) {
    for (final Stance mode : values()) {
      if (mode.name().equals(name)) {
        return mode;
      }
    }
    return FOLLOW;
  }
}
