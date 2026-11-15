package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.ward.CrucifixShapes;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
abstract class ServerWorldMixin {

  @Inject(method = "onBlockChanged", at = @At("HEAD"))
  private void nightborn$findCrucifixShapes(
      final BlockPos pos, final BlockState before, final BlockState after, final CallbackInfo ci) {
    final ServerWorld self = (ServerWorld) (Object) this;
    if (self.getServer().isOnThread() && CrucifixShapes.flipsMaterial(before, after)) {
      CrucifixShapes.get(self).refreshAround(self, pos);
    }
  }
}
