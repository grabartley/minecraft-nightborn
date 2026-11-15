package com.grahambartley.nightborn.decor;

import java.util.Optional;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

public enum FlameToggle {
  LIGHT_WITH_FLINT(Cost.WEAR, SoundEvents.ITEM_FLINTANDSTEEL_USE),
  LIGHT_WITH_CHARGE(Cost.USE_UP, SoundEvents.ITEM_FIRECHARGE_USE),
  DOUSE(Cost.NONE, SoundEvents.BLOCK_CANDLE_EXTINGUISH),
  PASS(Cost.NONE, null);

  public enum Cost {
    WEAR,
    USE_UP,
    NONE
  }

  private final Cost cost;
  private final SoundEvent sound;

  FlameToggle(final Cost cost, final SoundEvent sound) {
    this.cost = cost;
    this.sound = sound;
  }

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

  public Cost cost() {
    return this.cost;
  }

  public Optional<SoundEvent> sound() {
    return Optional.ofNullable(this.sound);
  }
}
