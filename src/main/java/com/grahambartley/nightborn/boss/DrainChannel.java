package com.grahambartley.nightborn.boss;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class DrainChannel {

  static final float LOUD = 2.0F;

  private DrainChannel() {}

  public static void pulse(
      final MobEntity boss,
      final ServerWorld world,
      final LivingEntity foe,
      final DrainAttack drain,
      final float healCeiling) {
    if (foe.damage(boss.getDamageSources().magic(), drain.damage())) {
      boss.setHealth(DrainAttack.healed(boss.getHealth(), drain.heal(), healCeiling));
    }
    final Vec3d from = foe.getEyePos();
    final Vec3d span = boss.getEyePos().subtract(from);
    final int steps = Math.max(1, (int) (span.length() * 2.0));
    for (int i = 0; i <= steps; i++) {
      final Vec3d at = from.add(span.multiply((double) i / steps));
      world.spawnParticles(ParticleTypes.DAMAGE_INDICATOR, at.x, at.y, at.z, 1, 0.0, 0.0, 0.0, 0.0);
    }
    world.playSoundFromEntity(
        null, boss, SoundEvents.ENTITY_WARDEN_HEARTBEAT, boss.getSoundCategory(), LOUD, 1.2F);
  }

  public static void snap(final MobEntity boss, final ServerWorld world) {
    world.playSoundFromEntity(
        null, boss, SoundEvents.BLOCK_GLASS_BREAK, boss.getSoundCategory(), LOUD, 0.6F);
  }
}
