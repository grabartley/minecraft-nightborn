package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.occult.MistForm;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
abstract class BlockStateCollisionMixin {

  @Inject(
      method =
          "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$mistDriftsThrough(
      final BlockView world,
      final BlockPos pos,
      final ShapeContext context,
      final CallbackInfoReturnable<VoxelShape> cir) {
    if (context instanceof EntityShapeContext entityContext
        && entityContext.getEntity() instanceof LivingEntity
        && MistForm.passes(entityContext.getEntity(), (BlockState) (Object) this)) {
      cir.setReturnValue(VoxelShapes.empty());
    }
  }
}
