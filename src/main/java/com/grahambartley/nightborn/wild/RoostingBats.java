package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.core.SurfaceSpots;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireBatEntity;
import java.util.Optional;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

public final class RoostingBats {

  static final int NEAR = 8;
  static final int FAR = 32;
  static final int TRIES = 24;
  static final int SEARCH = 24;

  private RoostingBats() {}

  static boolean roost(
      final ServerWorld world, final ServerPlayerEntity player, final Random random) {
    for (int i = 0; i < TRIES; i++) {
      final Optional<BlockPos> under =
          under(
              world,
              SurfaceSpots.offset(
                  player.getBlockPos(),
                  random.nextFloat() * MathHelper.TAU,
                  random.nextBetween(NEAR, FAR)));
      if (under.isPresent()) {
        final VampireBatEntity bat = NightbornMobs.VAMPIRE_BAT.create(world);
        if (bat == null) {
          return false;
        }
        bat.refreshPositionAndAngles(under.get(), random.nextFloat() * 360.0F, 0.0F);
        bat.initialize(world, world.getLocalDifficulty(under.get()), SpawnReason.EVENT, null);
        bat.setRoosting(true);
        world.spawnEntity(bat);
        return true;
      }
    }
    return false;
  }

  static boolean hangs(final boolean airBelow, final boolean airTwoBelow) {
    return airBelow && airTwoBelow;
  }

  static Optional<BlockPos> under(final ServerWorld world, final BlockPos column) {
    if (!SurfaceSpots.loaded(world, column.getX(), column.getZ())) {
      return Optional.empty();
    }
    final int top = world.getTopY(Heightmap.Type.MOTION_BLOCKING, column.getX(), column.getZ());
    for (int y = top - 1; y > top - SEARCH; y--) {
      final BlockPos branch = new BlockPos(column.getX(), y, column.getZ());
      final BlockPos below = branch.down();
      if (world.getBlockState(branch).isSolidBlock(world, branch)) {
        return hangs(world.isAir(below), world.isAir(below.down()))
            ? Optional.of(below)
            : Optional.empty();
      }
    }
    return Optional.empty();
  }
}
