package com.grahambartley.nightborn.decor;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

public abstract class FurnitureBlock extends BlockWithEntity implements AnimatedFurniture {

  public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

  protected FurnitureBlock(final Settings settings) {
    super(settings);
  }

  protected BlockState facingPlacer(final ItemPlacementContext context) {
    return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
  }

  @Override
  protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
    return state.with(FACING, rotation.rotate(state.get(FACING)));
  }

  @Override
  protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
    return state.rotate(mirror.getRotation(state.get(FACING)));
  }

  @Override
  protected BlockRenderType getRenderType(final BlockState state) {
    return BlockRenderType.ENTITYBLOCK_ANIMATED;
  }

  @Override
  public BlockEntity createBlockEntity(final BlockPos pos, final BlockState state) {
    return new FurnitureBlockEntity(pos, state);
  }
}
