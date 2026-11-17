package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.gore.GoreParticles;
import com.grahambartley.nightborn.smoke.SmokeParticles;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class ArenaSeal {

  private final Map<Integer, Vec3d> held = new HashMap<>();

  public static boolean turnsBack(
      final boolean wasInside, final boolean inside, final boolean foe) {
    return wasInside && !inside && foe;
  }

  public void hold(
      final ServerWorld world, final Arena arena, final Predicate<ServerPlayerEntity> foe) {
    final Map<Integer, Vec3d> now = new HashMap<>();
    for (final ServerPlayerEntity player :
        world.getPlayers(player -> player.isAlive() && !player.isSpectator())) {
      final Vec3d pos = player.getPos();
      final boolean inside = arena.holds(pos.x, pos.y, pos.z);
      final Vec3d last = held.get(player.getId());
      if (turnsBack(last != null, inside, foe.test(player))) {
        putBack(world, player, last);
        now.put(player.getId(), last);
      } else if (inside) {
        now.put(player.getId(), pos);
      }
    }
    held.clear();
    held.putAll(now);
  }

  public void release() {
    held.clear();
  }

  private static void putBack(
      final ServerWorld world, final ServerPlayerEntity player, final Vec3d to) {
    final Vec3d from = player.getPos();
    world.spawnParticles(
        SmokeParticles.VAMPIRE_SMOKE, from.x, from.y + 1.0, from.z, 24, 0.3, 0.6, 0.3, 0.02);
    world.spawnParticles(GoreParticles.DROP, to.x, to.y + 1.0, to.z, 16, 0.3, 0.5, 0.3, 0.05);
    player.stopRiding();
    player.requestTeleport(to.x, to.y, to.z);
    player.setVelocity(Vec3d.ZERO);
    player.velocityModified = true;
    world.playSound(
        null,
        player.getBlockPos(),
        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE.value(),
        SoundCategory.HOSTILE,
        1.0F,
        0.6F);
  }
}
