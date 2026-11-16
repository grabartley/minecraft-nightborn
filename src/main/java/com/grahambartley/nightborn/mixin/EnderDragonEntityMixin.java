package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinProtection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderDragonEntity.class)
abstract class EnderDragonEntityMixin {

  @WrapOperation(
      method = "destroyBlocks",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
  private boolean nightborn$anAssignedCoffinWithstandsTheDragon(
      final World world,
      final BlockPos pos,
      final boolean move,
      final Operation<Boolean> original) {
    return !CoffinProtection.shields(world, pos, (EnderDragonEntity) (Object) this)
        && original.call(world, pos, move);
  }
}
