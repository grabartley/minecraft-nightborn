package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.coffin.CoffinWood;
import com.grahambartley.nightborn.coffin.Coffins;
import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.BedPart;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

final class CoffinStage {

  static final String YARD = "nightborn:vampire_yard";

  private CoffinStage() {}

  static BlockPos coffin(final TestContext context, final BlockPos foot, final Direction facing) {
    return pair(context, Coffins.get(CoffinWood.DARK_OAK), foot, facing);
  }

  static BlockPos bed(final TestContext context, final BlockPos foot, final Direction facing) {
    return pair(context, Blocks.RED_BED, foot, facing);
  }

  static void roof(final TestContext context) {
    for (int x = 0; x <= 8; x++) {
      for (int z = 0; z <= 8; z++) {
        context.setBlockState(new BlockPos(x, 4, z), Blocks.STONE);
      }
    }
  }

  private static BlockPos pair(
      final TestContext context, final Block block, final BlockPos foot, final Direction facing) {
    final BlockState footState =
        block.getDefaultState().with(BedBlock.FACING, facing).with(BedBlock.PART, BedPart.FOOT);
    final BlockPos head = foot.offset(facing);
    context.setBlockState(foot, footState);
    context.setBlockState(head, footState.with(BedBlock.PART, BedPart.HEAD));
    return head;
  }
}
