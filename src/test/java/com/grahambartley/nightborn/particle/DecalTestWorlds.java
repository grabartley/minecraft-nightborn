package com.grahambartley.nightborn.particle;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

final class DecalTestWorlds {

  static final BlockPos BLOCK = new BlockPos(4, 63, 9);

  private DecalTestWorlds() {}

  static BlockView ledge(final int width) {
    final Map<BlockPos, BlockState> blocks = new HashMap<>();
    for (int i = 0; i < width; i++) {
      blocks.put(BLOCK.east(i), Blocks.STONE.getDefaultState());
    }
    return worldOf(blocks);
  }

  static BlockView worldOf(final Map<BlockPos, BlockState> blocks) {
    final BlockView world = mock(BlockView.class);
    when(world.getBlockState(any()))
        .thenAnswer(call -> blocks.getOrDefault(call.getArgument(0), Blocks.AIR.getDefaultState()));
    when(world.getFluidState(any()))
        .thenAnswer(call -> world.getBlockState(call.getArgument(0)).getFluidState());
    return world;
  }
}
