package com.grahambartley.nightborn.decor;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class CandelabraBlock extends LightableFurnitureBlock {

  public static final MapCodec<CandelabraBlock> CODEC = createCodec(CandelabraBlock::new);
  public static final int LIGHT = 12;

  private static final Map<Direction, VoxelShape> SHAPES =
      FacingShapes.fromNorth(
          VoxelShapes.union(
              Block.createCuboidShape(5, 0, 5, 11, 2, 11),
              Block.createCuboidShape(7, 2, 7, 9, 8, 9),
              Block.createCuboidShape(1, 7, 6, 15, 15, 10)));
  private static final List<Vec3d> WICKS =
      List.of(
          new Vec3d(3.0 / 16, 14.0 / 16, 8.0 / 16),
          new Vec3d(8.0 / 16, 15.0 / 16, 8.0 / 16),
          new Vec3d(13.0 / 16, 14.0 / 16, 8.0 / 16));

  public CandelabraBlock(final Settings settings) {
    super(settings);
  }

  @Override
  protected MapCodec<? extends BlockWithEntity> getCodec() {
    return CODEC;
  }

  @Override
  public String artModel() {
    return "candelabra";
  }

  @Override
  protected VoxelShape shapeFor(final BlockState state) {
    return SHAPES.get(state.get(FACING));
  }

  @Override
  protected List<Vec3d> wicks() {
    return WICKS;
  }

  @Override
  protected Direction supportSide() {
    return Direction.DOWN;
  }
}
