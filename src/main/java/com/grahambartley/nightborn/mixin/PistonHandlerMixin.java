package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.coffin.CoffinProtection;
import java.util.List;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
abstract class PistonHandlerMixin {

  @Shadow @Final private World world;

  @Shadow @Final private List<BlockPos> brokenBlocks;

  @Inject(method = "calculatePush", at = @At("RETURN"), cancellable = true)
  private void nightborn$anAssignedCoffinHoldsFast(final CallbackInfoReturnable<Boolean> cir) {
    if (cir.getReturnValueZ()
        && this.brokenBlocks.stream()
            .anyMatch(pos -> CoffinProtection.shields(this.world, pos, null))) {
      cir.setReturnValue(false);
    }
  }
}
