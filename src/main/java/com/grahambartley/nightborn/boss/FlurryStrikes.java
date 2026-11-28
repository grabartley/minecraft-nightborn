package com.grahambartley.nightborn.boss;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public final class FlurryStrikes {

  private FlurryStrikes() {}

  public static void hit(final MobEntity boss, final ServerWorld world, final FlurryAttack flurry) {
    final Vec3d facing = Vec3d.fromPolar(0.0F, boss.getYaw());
    final LivingEntity target = boss.getTarget();
    if (target != null) {
      boss.lookAtEntity(target, 60.0F, 60.0F);
      boss.setVelocity(
          boss.getVelocity()
              .add(target.getPos().subtract(boss.getPos()).normalize().multiply(flurry.lunge())));
      boss.velocityModified = true;
    }
    boss.swingHand(Hand.MAIN_HAND);
    world.playSoundFromEntity(
        null, boss, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, boss.getSoundCategory(), 1.0F, 0.8F);
    for (final LivingEntity victim :
        world.getEntitiesByClass(
            LivingEntity.class,
            boss.getBoundingBox().expand(flurry.reach()),
            entity ->
                entity != boss
                    && entity.isAlive()
                    && boss.canTarget(entity)
                    && flurry.reaches(
                        facing.x,
                        facing.z,
                        entity.getX() - boss.getX(),
                        entity.getZ() - boss.getZ()))) {
      victim.damage(StrikeSource.of(world, boss, flurry.damageType()), flurry.damage());
    }
  }
}
