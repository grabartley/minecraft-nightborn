package com.grahambartley.nightborn.boss;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

public final class BossWard {

  static final String KEEPER = "nightborn.ward_keeper";

  private final List<Entity> keepers = new ArrayList<>();
  private long raisedAt;
  private int ticks;

  public static void register() {
    ServerEntityEvents.ENTITY_LOAD.register(
        (entity, world) -> {
          if (orphaned(entity.getCommandTags().contains(KEEPER))) {
            world.getServer().execute(entity::discard);
          }
        });
  }

  public static boolean broke(
      final boolean wasHeld, final boolean held, final boolean busy, final boolean silent) {
    return wasHeld && !held && !busy && !silent;
  }

  public static boolean orphaned(final boolean taggedOnLoad) {
    return taggedOnLoad;
  }

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
        if (boss.getTarget() != null) {
          mob.setAttacker(boss.getTarget());
          mob.setTarget(boss.getTarget());
        }
      }
      keeper.setGlowing(ward.shields());
      world.spawnEntityAndPassengers(keeper);
      keeper.addCommandTag(KEEPER);
      keepers.add(keeper);
    }
  }

  public boolean holds(final long now) {
    keepers.removeIf(keeper -> !(keeper instanceof LivingEntity living) || !living.isAlive());
    return WardAttack.holds(keepers.size(), now - raisedAt, ticks);
  }

  public void expire(final long now) {
    if (WardAttack.spent(keepers.size(), now - raisedAt, ticks)) {
      drop();
    }
  }

  public void drop() {
    keepers.forEach(Entity::discard);
    keepers.clear();
  }
}
