package com.grahambartley.nightborn.guild.party;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface StrikesWithCamp {

  void strikeWithCamp(ServerWorld world, BlockPos pos, BlockState state);
}
