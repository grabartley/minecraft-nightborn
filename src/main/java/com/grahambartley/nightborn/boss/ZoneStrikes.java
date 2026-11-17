package com.grahambartley.nightborn.boss;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

public final class ZoneStrikes {

  static final double ZONE_HEIGHT = 3.0;
  static final int FLOOR_SEARCH = 6;

  private ZoneStrikes() {}

  public static void harm(
      final MobEntity boss,
      final ServerWorld world,
      final ZoneShape shape,
      final double y,
      final float damage,
      final float knock) {
    final double reach = Math.max(shape.radius(), shape.length()) + 1.0;
    final Box box =
        new Box(
            shape.midX() - reach,
            y - 1.0,
            shape.midZ() - reach,
            shape.midX() + reach,
            y + ZONE_HEIGHT,
            shape.midZ() + reach);
    for (final LivingEntity victim :
        world.getEntitiesByClass(
            LivingEntity.class,
            box,
            entity ->
                entity != boss
                    && entity.isAlive()
                    && boss.canTarget(entity)
                    && shape.contains(entity.getX(), entity.getZ()))) {
      if (victim.damage(boss.getDamageSources().mobAttack(boss), damage) && knock > 0.0F) {
        victim.takeKnockback(knock, boss.getX() - victim.getX(), boss.getZ() - victim.getZ());
      }
    }
  }

  public static void spray(final ServerWorld world, final ZoneShape shape, final double y) {
    world.spawnParticles(
        ParticleTypes.CRIMSON_SPORE,
        shape.midX(),
        y + 0.3,
        shape.midZ(),
        24,
        shape.radius() / 2.0,
        0.2,
        shape.radius() / 2.0,
        0.02);
  }

  public static double floor(final ServerWorld world, final LivingEntity entity) {
    BlockPos pos = entity.getBlockPos();
    for (int i = 0; i < FLOOR_SEARCH; i++) {
      if (!world.getBlockState(pos.down()).getCollisionShape(world, pos.down()).isEmpty()) {
        break;
      }
      pos = pos.down();
    }
    return pos.getY();
  }
}
