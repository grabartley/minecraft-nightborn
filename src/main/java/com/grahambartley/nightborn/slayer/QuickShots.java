package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.skill.Powers;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public final class QuickShots {

  private QuickShots() {}

  public static Optional<Hand> loaded(final LivingEntity caster) {
    for (final Hand hand : Hand.values()) {
      final ItemStack stack = caster.getStackInHand(hand);
      if (stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
        return Optional.of(hand);
      }
    }
    return Optional.empty();
  }

  public static void fire(final LivingEntity caster, final QuickShotPower power) {
    if (caster.getWorld().isClient()) {
      return;
    }
    loaded(caster)
        .ifPresent(
            hand -> {
              final ItemStack stack = caster.getStackInHand(hand);
              caster.swingHand(hand, true);
              ((CrossbowItem) stack.getItem())
                  .shootAll(
                      caster.getWorld(),
                      caster,
                      hand,
                      stack,
                      power.speed(),
                      power.divergence(),
                      Powers.foe(caster));
            });
  }
}
