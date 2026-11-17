package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.vampire.VampireAttributes;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class PotionWard {

  private PotionWard() {}

  public static StatusEffectInstance ward(
      final LivingEntity target, final StatusEffectInstance effect) {
    final int duration =
        duration(
            effect.getDuration(),
            effect.isInfinite(),
            effect.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL,
            VampireAttributes.value(target, VampireAttributes.POTION_RESISTANCE));
    return duration == effect.getDuration()
        ? effect
        : new StatusEffectInstance(
            effect.getEffectType(),
            duration,
            effect.getAmplifier(),
            effect.isAmbient(),
            effect.shouldShowParticles(),
            effect.shouldShowIcon());
  }

  public static int duration(
      final int duration, final boolean infinite, final boolean harmful, final double resistance) {
    if (infinite || !harmful || resistance <= 0.0) {
      return duration;
    }
    return Math.max(1, (int) Math.round(duration * (1.0 - Math.min(1.0, resistance))));
  }
}
