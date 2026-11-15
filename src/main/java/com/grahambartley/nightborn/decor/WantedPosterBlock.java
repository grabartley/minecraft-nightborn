package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class WantedPosterBlock extends WallDecorBlock {

  public static final int PORTRAITS = 3;
  public static final IntProperty PORTRAIT = IntProperty.of("portrait", 0, PORTRAITS - 1);
  public static final MapCodec<WantedPosterBlock> CODEC = createCodec(WantedPosterBlock::new);

  private static final VoxelShape NORTH = Block.createCuboidShape(3, 1, 15, 13, 15, 16);

  public WantedPosterBlock(final Settings settings) {
    super(settings, NORTH);
    this.setDefaultState(this.getDefaultState().with(PORTRAIT, 0));
  }

  @Override
  protected MapCodec<WantedPosterBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(final StateManager.Builder<Block, BlockState> builder) {
    super.appendProperties(builder);
    builder.add(PORTRAIT);
  }

  @Override
  @Nullable
  public BlockState getPlacementState(final ItemPlacementContext context) {
    final BlockState state = super.getPlacementState(context);
    return state == null
        ? null
        : state.with(PORTRAIT, DecorVariants.pick(context.getBlockPos(), PORTRAITS));
  }
}
