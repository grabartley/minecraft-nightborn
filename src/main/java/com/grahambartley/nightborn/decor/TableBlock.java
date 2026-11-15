package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class TableBlock extends Block {

  public static final MapCodec<TableBlock> CODEC = createCodec(TableBlock::new);

  private static final VoxelShape SHAPE =
      VoxelShapes.union(
          Block.createCuboidShape(0, 13, 0, 16, 16, 16),
          Block.createCuboidShape(5, 0, 5, 11, 13, 11));

  public TableBlock(final Settings settings) {
    super(settings);
  }

  @Override
  protected MapCodec<? extends Block> getCodec() {
    return CODEC;
  }

  @Override
  protected VoxelShape getOutlineShape(
      final BlockState state,
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context) {
    return SHAPE;
  }
}
