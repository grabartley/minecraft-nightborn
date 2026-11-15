package com.grahambartley.nightborn.decor;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public enum FlameToggle {
  LIGHT_WITH_FLINT,
  LIGHT_WITH_CHARGE,
  DOUSE,
  PASS;

  public static FlameToggle decide(final boolean lit, final ItemStack held) {
    if (lit) {
      return held.isEmpty() ? DOUSE : PASS;
    }
    if (held.isOf(Items.FLINT_AND_STEEL)) {
      return LIGHT_WITH_FLINT;
    }
    if (held.isOf(Items.FIRE_CHARGE)) {
      return LIGHT_WITH_CHARGE;
    }
    return PASS;
  }

  public boolean lights() {
    return this == LIGHT_WITH_FLINT || this == LIGHT_WITH_CHARGE;
  }
}
