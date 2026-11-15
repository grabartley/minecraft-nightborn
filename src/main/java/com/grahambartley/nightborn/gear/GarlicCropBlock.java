package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.ward.Wards;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class GarlicCropBlock extends CropBlock {

  public static final MapCodec<GarlicCropBlock> CODEC = createCodec(GarlicCropBlock::new);

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
    return super.canPlantOnTop(floor, world, pos) || floor.isIn(BlockTags.DIRT);
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
