package com.grahambartley.nightborn.boss;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public final class BossWard {

  private final List<Entity> keepers = new ArrayList<>();
  private long raisedAt;
  private int ticks;

  public void raise(
      final MobEntity boss, final ServerWorld world, final WardAttack ward, final long now) {
    drop();
    raisedAt = now;
    ticks = ward.ticks();
    for (int i = 0; i < ward.count(); i++) {
      final Entity keeper = ward.entity().value().create(world);
      if (keeper == null) {
        continue;
      }
      keeper.refreshPositionAndAngles(
          boss.getX() + ward.offsetX(i),
          boss.getY() + 1.5,
          boss.getZ() + ward.offsetZ(i),
          boss.getYaw(),
          0.0F);
      if (keeper instanceof MobEntity mob) {
        mob.initialize(
            world, world.getLocalDifficulty(mob.getBlockPos()), SpawnReason.MOB_SUMMONED, null);
        mob.setPersistent();
        if (boss.getTarget() != null) {
          mob.setTarget(boss.getTarget());
        }
      }
      keeper.setGlowing(true);
      world.spawnEntityAndPassengers(keeper);
      keepers.add(keeper);
    }
  }

  public boolean holds(final long now) {
    keepers.removeIf(keeper -> !(keeper instanceof LivingEntity living) || !living.isAlive());
    return WardAttack.holds(keepers.size(), now - raisedAt, ticks);
  }

  public void drop() {
    keepers.forEach(Entity::discard);
    keepers.clear();
  }
}
