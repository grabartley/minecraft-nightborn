package com.grahambartley.nightborn.boss;

import java.util.Set;
import java.util.UUID;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public final class ArenaSeal {

  static final double INSET = 1.0;

  private ArenaSeal() {}

  public static boolean turnsBack(
      final boolean wasInside, final boolean inside, final boolean creative) {
    return wasInside && !inside && !creative;
  }

  public static void hold(final ServerWorld world, final Arena arena, final Set<UUID> wasInside) {
    for (final ServerPlayerEntity player :
        world.getPlayers(player -> player.isAlive() && !player.isSpectator())) {
      final Vec3d pos = player.getPos();
      if (!turnsBack(
          wasInside.contains(player.getUuid()),
          arena.holds(pos.x, pos.y, pos.z),
          player.isCreative())) {
        continue;
      }
      final Vec3d back = arena.pullIn(pos, INSET);
      player.requestTeleport(back.x, back.y, back.z);
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
}
