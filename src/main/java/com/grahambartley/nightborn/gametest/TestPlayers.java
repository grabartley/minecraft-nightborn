package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.vampire.VampireTransitions;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

final class TestPlayers {

  private TestPlayers() {}

  static ServerPlayerEntity human(final TestContext context, final BlockPos relative) {
    final ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    player.changeGameMode(GameMode.SURVIVAL);
    final Vec3d at = context.getAbsolute(Vec3d.ofBottomCenter(relative));
    player.refreshPositionAndAngles(at.x, at.y, at.z, 0.0F, 0.0F);
    return player;
  }

  static ServerPlayerEntity vampire(final TestContext context, final BlockPos relative) {
    final ServerPlayerEntity player = human(context, relative);
    VampireTransitions.turn(player);
    return player;
  }

  static void remove(final TestContext context, final ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
  }

  static void finish(final TestContext context, final ServerPlayerEntity... players) {
    remove(context, players);
    context.complete();
  }
}
