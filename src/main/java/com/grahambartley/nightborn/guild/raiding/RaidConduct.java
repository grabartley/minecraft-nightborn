package com.grahambartley.nightborn.guild.raiding;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import com.grahambartley.nightborn.mind.MindTies;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class RaidConduct {

  static final int EVERY_TICKS = 10;
  static final int LID_REACH = 2;
  public static final int PLACE_REACH = 4;
  static final int QUARRY_REACH = 6;
  static final double CHASE = 64.0;

  private RaidConduct() {}

  public static boolean stays(final PathAwareEntity raider, final RaidOrders orders) {
    if (!(raider.getWorld() instanceof ServerWorld world)) {
      return true;
    }
    final MinecraftServer server = world.getServer();
    if (orders.over(server.getOverworld().getTimeOfDay())) {
      return false;
    }
    if (raider.age % EVERY_TICKS != 0 || MindTies.mesmerised(raider)) {
      return true;
    }
    orders
        .place()
        .ifPresentOrElse(
            place -> {
              raider.setPositionTarget(place, PLACE_REACH);
              openLids(world, raider.getBlockPos());
            },
            () -> hunt(raider, server.getPlayerManager().getPlayer(orders.quarry())));
    return true;
  }

  public static boolean chases(
      final boolean sameWorld,
      final boolean alive,
      final boolean spectator,
      final double distance) {
    return sameWorld && alive && !spectator && distance <= CHASE;
  }

  private static void hunt(final PathAwareEntity raider, final ServerPlayerEntity quarry) {
    if (quarry == null
        || !chases(
            quarry.getWorld() == raider.getWorld(),
            quarry.isAlive(),
            quarry.isSpectator(),
            raider.distanceTo(quarry))) {
      return;
    }
    raider.setPositionTarget(quarry.getBlockPos(), QUARRY_REACH);
    if (raider.getTarget() == null) {
      raider.setTarget(quarry);
    }
  }

  static void openLids(final ServerWorld world, final BlockPos center) {
    for (final BlockPos pos :
        BlockPos.iterate(
            center.add(-LID_REACH, -1, -LID_REACH), center.add(LID_REACH, LID_REACH, LID_REACH))) {
      final BlockState state = world.getBlockState(pos);
      if (CoffinSleepers.shelters(state)) {
        CoffinBlock.openLid(world, CoffinBlock.head(pos, state));
      }
    }
  }
}
