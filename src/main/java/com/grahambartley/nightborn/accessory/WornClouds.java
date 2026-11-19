package com.grahambartley.nightborn.accessory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class WornClouds {

  private WornClouds() {}

  public static StatusEffectInstance shorten(
      final LivingEntity target, final StatusEffectInstance effect) {
    return shorten(effect, GiftAttributes.value(target, GiftAttributes.CLOUD_TIME));
  }

  public static StatusEffectInstance shorten(
      final StatusEffectInstance effect, final double share) {
    if (share >= 1.0 || effect.isInfinite()) {
      return effect;
    }
    return new StatusEffectInstance(
        effect.getEffectType(),
        GiftScale.ticks(effect.getDuration(), share),
        effect.getAmplifier(),
        effect.isAmbient(),
        effect.shouldShowParticles(),
        effect.shouldShowIcon());
  }
}
