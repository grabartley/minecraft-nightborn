package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class Gloom {

  public static final TagKey<StatusEffect> EFFECTS =
      TagKey.of(RegistryKeys.STATUS_EFFECT, Identifier.of(Nightborn.MOD_ID, "gloom"));

  private Gloom() {}

  public static StatusEffectInstance lessen(
      final LivingEntity entity, final StatusEffectInstance effect) {
    final double resistance = VampireAttributes.value(entity, VampireAttributes.GLOOM_RESISTANCE);
    if (resistance <= 0.0 || effect.isInfinite() || !effect.getEffectType().isIn(EFFECTS)) {
      return effect;
    }
    return new StatusEffectInstance(
        effect.getEffectType(),
        shortened(effect.getDuration(), resistance),
        effect.getAmplifier(),
        effect.isAmbient(),
        effect.shouldShowParticles(),
        effect.shouldShowIcon());
  }

  static int shortened(final int ticks, final double resistance) {
    return (int) Math.max(1L, Math.round(ticks * (1.0 - Math.max(0.0, Math.min(1.0, resistance)))));
  }
}
