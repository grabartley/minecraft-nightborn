package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinBlock;
import com.grahambartley.nightborn.coffin.CoffinStepOut;
import java.util.Optional;
import net.minecraft.block.BedBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.CollisionView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BedBlock.class)
abstract class BedBlockMixin {

  @Inject(
      method =
          "findWakeUpPosition(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/CollisionView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;F)Ljava/util/Optional;",
      at = @At("HEAD"),
      cancellable = true)
  private static void nightborn$stepOutInFrontOfAStandingCoffin(
      final EntityType<?> type,
      final CollisionView world,
      final BlockPos pos,
      final Direction bedDirection,
      final float spawnAngle,
      final CallbackInfoReturnable<Optional<Vec3d>> cir) {
    if (CoffinBlock.isStanding(world.getBlockState(pos))) {
      cir.setReturnValue(CoffinStepOut.spot(type, world, pos));
    }
  }
}
