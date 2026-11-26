package com.grahambartley.nightborn.smoke;

import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;

public final class SmokePoof {

  static final int SMOKE_COUNT = 30;
  static final double SMOKE_SPEED = 0.02;

  private SmokePoof() {}

  public static void burst(final LivingEntity vampire, final SoundEvent sound) {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return;
    }
    puff(world, vampire);
    world.playSound(
        null,
        vampire.getX(),
        vampire.getY(),
        vampire.getZ(),
        sound,
        vampire.getSoundCategory(),
        1.0F,
        1.0F);
  }

  public static void puff(final LivingEntity vampire) {
    if (vampire.getWorld() instanceof ServerWorld world) {
      puff(world, vampire);
    }
  }

  private static void puff(final ServerWorld world, final LivingEntity vampire) {
    final EntityDimensions shape = vampire.getDimensions(vampire.getPose());
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE,
        vampire.getX(),
        vampire.getBodyY(0.5),
        vampire.getZ(),
        SMOKE_COUNT,
        shape.width() / 2.0,
        shape.height() / 2.0,
        shape.width() / 2.0,
        SMOKE_SPEED);
  }
}
