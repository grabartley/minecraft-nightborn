package com.grahambartley.nightborn.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.world.World;

public final class NightSight {

  static final int TICKS = 400;
  static final int REFRESH_BELOW = 220;

  private NightSight() {}

  public static boolean isNight(final World world) {
    return NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
  }

  public static void keep(final LivingEntity seer) {
    final StatusEffectInstance current = seer.getStatusEffect(StatusEffects.NIGHT_VISION);
    if (needsRefresh(current == null ? 0 : current.getDuration())) {
      seer.addStatusEffect(
          new StatusEffectInstance(StatusEffects.NIGHT_VISION, TICKS, 0, true, false, false));
    }
  }

  static boolean needsRefresh(final int ticksLeft) {
    return ticksLeft < REFRESH_BELOW;
  }
}
