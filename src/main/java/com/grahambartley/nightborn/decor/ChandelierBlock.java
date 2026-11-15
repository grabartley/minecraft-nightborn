package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class ChandelierBlock extends LightableFurnitureBlock {

  public static final MapCodec<ChandelierBlock> CODEC = createCodec(ChandelierBlock::new);
  public static final int LIGHT = 15;

  private static final VoxelShape SHAPE =
      VoxelShapes.union(
          Block.createCuboidShape(6, 11, 6, 10, 16, 10),
          Block.createCuboidShape(1, 5, 1, 15, 13, 15));
  private static final List<Vec3d> WICKS =
      List.of(
          new Vec3d(2.0 / 16, 13.0 / 16, 8.0 / 16),
          new Vec3d(14.0 / 16, 13.0 / 16, 8.0 / 16),
          new Vec3d(8.0 / 16, 13.0 / 16, 2.0 / 16),
          new Vec3d(8.0 / 16, 13.0 / 16, 14.0 / 16));

  public ChandelierBlock(final Settings settings) {
    super(settings);
  }

  @Override
  protected MapCodec<? extends BlockWithEntity> getCodec() {
    return CODEC;
  }

  @Override
  public String artModel() {
    return "chandelier";
  }

  @Override
  protected VoxelShape shapeFor(final BlockState state) {
    return SHAPE;
  }

  @Override
  protected List<Vec3d> wicks() {
    return WICKS;
  }

  @Override
  protected Direction supportSide() {
    return Direction.UP;
  }
}
