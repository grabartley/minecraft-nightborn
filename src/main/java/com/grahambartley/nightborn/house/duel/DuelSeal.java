package com.grahambartley.nightborn.house.duel;

import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class DuelSeal {

  static final double MARGIN = 0.75;

  private final Map<UUID, Vec3d> held = new HashMap<>();

  public void hold(final ServerWorld world, final HouseDuel duel) {
    final Arena arena = duel.arena();
    for (final LivingEntity entity :
        world.getEntitiesByClass(LivingEntity.class, bounds(arena), LivingEntity::isAlive)) {
      final Vec3d pos = entity.getPos();
      if (duel.involves(entity.getUuid())) {
        keepIn(world, entity, arena, pos);
      } else if (!entity.isSpectator() && arena.holds(pos.x, pos.y, pos.z)) {
        move(world, entity, DuelRules.pushOut(arena, pos));
      }
    }
  }

  private void keepIn(
      final ServerWorld world, final LivingEntity fighter, final Arena arena, final Vec3d pos) {
    if (arena.holds(pos.x, pos.y, pos.z)) {
      held.put(fighter.getUuid(), pos);
      return;
    }
    move(world, fighter, held.getOrDefault(fighter.getUuid(), arena.pullIn(pos, MARGIN)));
  }

  private static Box bounds(final Arena arena) {
    final double reach = arena.radius() + DuelRules.OUTSIDE;
    return new Box(
        arena.midX() - reach,
        arena.centre().getY() - arena.below(),
        arena.midZ() - reach,
        arena.midX() + reach,
        arena.centre().getY() + arena.above(),
        arena.midZ() + reach);
  }

  private static void move(final ServerWorld world, final LivingEntity entity, final Vec3d to) {
    final Vec3d from = entity.getPos();
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE, from.x, from.y + 1.0, from.z, 12, 0.3, 0.6, 0.3, 0.02);
    entity.stopRiding();
    if (entity instanceof ServerPlayerEntity player) {
      player.requestTeleport(to.x, to.y, to.z);
    } else {
      entity.refreshPositionAfterTeleport(to.x, to.y, to.z);
    }
    entity.setVelocity(Vec3d.ZERO);
    entity.velocityModified = true;
    world.playSound(
        null,
        entity.getBlockPos(),
        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(),
        SoundCategory.PLAYERS,
        0.6F,
        0.8F);
  }
}
