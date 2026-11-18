package com.grahambartley.nightborn.wild;

import com.grahambartley.nightborn.guild.GuildRumour;
import com.grahambartley.nightborn.servant.job.ScoutChoices;
import com.grahambartley.nightborn.structure.NearestStructures;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class StructureSigns {

  private StructureSigns() {}

  static void show(
      final ServerWorld world, final ServerPlayerEntity player, final int chance, final int range) {
    final Random random = world.getRandom();
    if (!WildRules.rolls(random.nextInt(WildRules.PERCENT), chance)) {
      return;
    }
    final BlockPos from = player.getBlockPos();
    final boolean bat = random.nextBoolean();
    NearestStructures.eachOf(world, GuildRumour.VAMPIRE_HOUSES, from, range)
        .thenAcceptAsync(
            found -> {
              if (nearOne(
                      ScoutChoices.nearest(from, found, range, 1).size(), player.isDisconnected())
                  && !(bat && RoostingBats.roost(world, player, random))) {
                DrainedAnimals.fall(world, player, random);
              }
            },
            world.getServer());
  }

  static boolean nearOne(final int structuresInRange, final boolean disconnected) {
    return structuresInRange > 0 && !disconnected;
  }
}
