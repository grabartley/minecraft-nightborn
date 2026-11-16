package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinProtection;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
abstract class ExplosionMixin {

  @Shadow @Final private World world;

  @WrapOperation(
      method = "collectBlocksAndDamageEntities",
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/world/explosion/ExplosionBehavior;canDestroyBlock(Lnet/minecraft/world/explosion/Explosion;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;F)Z"))
  private boolean nightborn$anAssignedCoffinWithstandsTheBlast(
      final ExplosionBehavior behavior,
      final Explosion explosion,
      final BlockView view,
      final BlockPos pos,
      final BlockState state,
      final float power,
      final Operation<Boolean> original) {
    return original.call(behavior, explosion, view, pos, state, power)
        && !CoffinProtection.shields(this.world, pos, null);
  }
}
