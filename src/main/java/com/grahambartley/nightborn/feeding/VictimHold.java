package com.grahambartley.nightborn.feeding;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;

final class VictimHold {

  private VictimHold() {}

  static void apply(final LivingEntity victim) {
    victim.setVelocity(HoldRules.pinned(victim.getVelocity()));
    if (victim instanceof MobEntity mob) {
      mob.getNavigation().stop();
    }
    final StatusEffectInstance current = victim.getStatusEffect(StatusEffects.SLOWNESS);
    if (current == null || HoldRules.needsRefresh(current.getDuration(), current.getAmplifier())) {
      victim.addStatusEffect(
          new StatusEffectInstance(
              StatusEffects.SLOWNESS,
              HoldRules.HOLD_TICKS,
              HoldRules.SLOWNESS_AMPLIFIER,
              false,
              false,
              false));
    }
  }
}
