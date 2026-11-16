package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PictureBlock extends WallDecorBlock {

  public static final int PORTRAITS = 3;
  public static final IntProperty PORTRAIT = IntProperty.of("portrait", 0, PORTRAITS - 1);

  private final MapCodec<PictureBlock> codec;

  public PictureBlock(final Settings settings, final VoxelShape northShape) {
    super(settings, northShape);
    this.codec = createCodec(copy -> new PictureBlock(copy, northShape));
    this.setDefaultState(this.getDefaultState().with(PORTRAIT, 0));
  }

  @Override
  protected MapCodec<PictureBlock> getCodec() {
    return this.codec;
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
