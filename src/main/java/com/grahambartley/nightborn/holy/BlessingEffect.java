package com.grahambartley.nightborn.holy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import org.jetbrains.annotations.Nullable;

public final class BlessingEffect extends InstantStatusEffect {

  static final int COLOUR = 0xFFFFFF;

  BlessingEffect() {
    super(StatusEffectCategory.NEUTRAL, COLOUR);
  }

  @Override
  public void applyInstantEffect(
      @Nullable final Entity source,
      @Nullable final Entity attacker,
      final LivingEntity target,
      final int amplifier,
      final double proximity) {
    HolyWater.bless(source, attacker, target, proximity);
  }

  @Override
  public boolean applyUpdateEffect(final LivingEntity entity, final int amplifier) {
    HolyWater.bless(null, null, entity, 1.0);
    return true;
  }
}
