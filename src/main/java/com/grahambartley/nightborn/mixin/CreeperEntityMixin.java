package com.grahambartley.nightborn.mixin;

import com.grahambartley.nightborn.mind.MindFeatures;
import net.minecraft.entity.mob.CreeperEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
abstract class CreeperEntityMixin {

  @Inject(method = "tick", at = @At("HEAD"))
  private void nightborn$noFuseWhileMesmerised(final CallbackInfo ci) {
    final CreeperEntity self = (CreeperEntity) (Object) this;
    if (!self.isIgnited() && MindFeatures.stilled(self)) {
      self.setFuseSpeed(-1);
    }
  }
}
