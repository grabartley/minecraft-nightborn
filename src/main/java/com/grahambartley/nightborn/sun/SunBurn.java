package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.config.SunSettings;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;

public final class SunBurn {

  static final int BURN_INTERVAL_TICKS = 20;
  static final float FIRE_SECONDS = 8.0F;
  static final int SMOKE_COUNT = 8;

  private SunBurn() {}

  public static boolean tick(final LivingEntity vampire, final SunSettings settings) {
    if (!(vampire.getWorld() instanceof ServerWorld world) || !DirectSun.on(vampire)) {
      return false;
    }
    if (vampire.age % BURN_INTERVAL_TICKS != 0) {
      return true;
    }
    vampire.setOnFireFor(FIRE_SECONDS);
    world.spawnParticles(
        ParticleTypes.LARGE_SMOKE,
        vampire.getX(),
        vampire.getBodyY(0.6),
        vampire.getZ(),
        SMOKE_COUNT,
        vampire.getWidth() / 2.0,
        vampire.getHeight() / 3.0,
        vampire.getWidth() / 2.0,
        0.01);
    world.playSound(
        null,
        vampire.getBlockPos(),
        SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
        vampire.getSoundCategory(),
        0.7F,
        1.6F);
    if (vampire.damage(NightbornDamage.sun(world), settings.sunDamage()) && vampire.isAlive()) {
      MilestoneCriterion.reach(vampire, Milestone.SUN_SURVIVED);
    }
    return true;
  }
}
