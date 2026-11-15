package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinProtection;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
abstract class WorldMixin {

  @Inject(
      method = "breakBlock(Lnet/minecraft/util/math/BlockPos;ZLnet/minecraft/entity/Entity;I)Z",
      at = @At("HEAD"),
      cancellable = true)
  private void nightborn$anAssignedCoffinWithstandsOthers(
      final BlockPos pos,
      final boolean drop,
      final Entity breakingEntity,
      final int maxUpdateDepth,
      final CallbackInfoReturnable<Boolean> cir) {
    if (breakingEntity != null
        && CoffinProtection.shields((World) (Object) this, pos, breakingEntity)) {
      cir.setReturnValue(false);
    }
  }
}
