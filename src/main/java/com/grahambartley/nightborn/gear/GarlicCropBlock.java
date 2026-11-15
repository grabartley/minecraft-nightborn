package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class GarlicCropBlock extends CropBlock {

  public static final MapCodec<GarlicCropBlock> CODEC = createCodec(GarlicCropBlock::new);
  static final int MIN_LIGHT = 8;

  public GarlicCropBlock(final Settings settings) {
    super(settings);
  }

  @Override
  public MapCodec<GarlicCropBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected ItemConvertible getSeedsItem() {
    return GearItems.GARLIC;
  }

  @Override
  protected boolean canPlantOnTop(
      final BlockState floor, final BlockView world, final BlockPos pos) {
    return super.canPlantOnTop(floor, world, pos)
        || floor.isOf(Blocks.DIRT)
        || floor.isOf(Blocks.COARSE_DIRT)
        || floor.isOf(Blocks.GRASS_BLOCK);
  }

  @Override
  protected boolean canPlaceAt(final BlockState state, final WorldView world, final BlockPos pos) {
    return lightEnough(
            world.getBaseLightLevel(pos, 0),
            world.getTopY(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY())
        && canPlantOnTop(world.getBlockState(pos.down()), world, pos.down());
  }

  static boolean lightEnough(final int baseLight, final boolean openSky) {
    return baseLight >= MIN_LIGHT || openSky;
  }

  @Override
  protected void onEntityCollision(
      final BlockState state, final World world, final BlockPos pos, final Entity entity) {
    super.onEntityCollision(state, world, pos, entity);
    if (!world.isClient) {
      Wards.touch(entity, ServerConfigService.current().wards());
    }
  }
}
