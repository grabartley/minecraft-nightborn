package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.config.CombatSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.TridentItem;

public record Threat(boolean swinging, boolean aiming) {

  public static Threat of(final LivingEntity target) {
    return new Threat(target.handSwinging, draws(target));
  }

  public boolean at(final double distance, final CombatSettings settings) {
    return (swinging && distance <= settings.swingThreatRange())
        || (aiming && distance >= settings.aimThreatRange());
  }

  public static boolean draws(final LivingEntity target) {
    return target.isUsingItem() && ranged(target.getActiveItem());
  }

  static boolean ranged(final ItemStack stack) {
    return stack.getItem() instanceof RangedWeaponItem || stack.getItem() instanceof TridentItem;
  }
}
